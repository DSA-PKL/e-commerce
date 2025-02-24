package com.dsapkl.backend.service;

import com.dsapkl.backend.dto.CartForm;
import com.dsapkl.backend.dto.CartOrderDto;
import com.dsapkl.backend.repository.*;
import com.dsapkl.backend.entity.*;
import com.stripe.exception.StripeException;
import com.stripe.model.LineItem;
import com.stripe.model.StripeCollection;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionListLineItemsParams;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class OrderService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final OrderItemRepository orderItemRepository;
    private final MemberInfoService memberInfoService;
    private final MemberInfoRepository memberInfoRepository;
    private final RecommendationService recommendationService;
    private final ClusterItemPreferenceRepository clusterItemPreferenceRepository;
    private final ClusterRepository clusterRepository;

    public OrderService(ItemRepository itemRepository, MemberRepository memberRepository, OrderRepository orderRepository, CartService cartService, OrderItemRepository orderItemRepository, MemberInfoService memberInfoService, RecommendationService recommendationService, ClusterItemPreferenceRepository clusterItemPreferenceRepository, ClusterRepository clusterRepository, MemberInfoRepository memberInfoRepository) {
        this.itemRepository = itemRepository;
        this.memberRepository = memberRepository;
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.orderItemRepository = orderItemRepository;
        this.memberInfoService = memberInfoService;
        this.recommendationService = recommendationService;
        this.clusterItemPreferenceRepository = clusterItemPreferenceRepository;
        this.clusterRepository = clusterRepository;
        this.memberInfoRepository = memberInfoRepository;
    }

    /**
     * 단일 주문
     */
    public Long order(Long memberId, Long itemId, int count, String paymentIntentId) {

        List<OrderItem> orderItemList = new ArrayList<>();
        Item findItem = itemRepository.findById(itemId).orElseGet(() -> null);
        Member findMember = memberRepository
                .findById(memberId).orElseGet(() -> null);
        int orderPrice = findItem.getPrice() * count;

        // sales_count 업데이트

        findItem.increaseSalesCount(count);

        OrderItem orderItem = OrderItem.createOrderItem(count, orderPrice, findItem);
        orderItemList.add(orderItem);

        OrderStatus orderStatus = OrderStatus.ORDER;

        Order order = Order.createOrder(orderStatus , findMember, paymentIntentId, orderItemList);

        Order save = orderRepository.save(order);

        updateMemberInfoAfterPurchase(memberId);

        // 클러스터 예측 실행
        try {
            recommendationService.getClusterPrediction(memberId);
            // 클러스터 아이템 선호도 업데이트
            updateClusterItemPreference(memberId, findItem);
        } catch (Exception e) {
            System.err.println("Error occurred during cluster processing: " + e.getMessage());
        }

        return save.getId();

    }

    private void updateMemberInfoAfterPurchase(Long memberId) {
        memberInfoService.updatePurchaseStatistics(memberId);
        memberInfoService.updateProductPreference(memberId);
        memberInfoService.updateAllStatistics(memberId);
    }

    /**
     * 주문 목록 조회
     */
    @Transactional(readOnly = true)
    public List<OrderDto> findOrdersDetail(Long memberId, OrderStatus orderStatus) {
        List<OrderDto> orders = orderRepository.findOrderDetail(memberId, orderStatus);
        return orders;
    }

    /**
     * 장바구니 상품들 주문
     */
    public Long orders(Long memberId, CartOrderDto cartOrderDto) {

        List<OrderItem> orderItemList = new ArrayList<>();

        Member findMember = memberRepository.findById(memberId)
            .orElseThrow(() -> new
                    NoSuchElementException("Member with ID " + memberId + " not found"));

        List<CartForm> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();

        String paymentIntentId = null;

        for (CartForm cartForm : cartOrderDtoList) {
            Item findItem = itemRepository.findById(cartForm.getItemId()).orElse(null);
            int orderPrice = findItem.getPrice() * cartForm.getCount();

            // sales_count 업데이트
            findItem.increaseSalesCount(cartForm.getCount());

            OrderItem orderItem = OrderItem.createOrderItem(cartForm.getCount(), orderPrice, findItem);
            orderItemList.add(orderItem);

            paymentIntentId = cartForm.getPaymentIntentId();
        }

        OrderStatus orderStatus = OrderStatus.ORDER;

        Order order = Order.createOrder(orderStatus, findMember, paymentIntentId, orderItemList);

        Order save = orderRepository.save(order);

        //주문한 상품은 장바구니에서 제거
        deleteCartItem(cartOrderDto);

        updateMemberInfoAfterPurchase(memberId);

        try {
            recommendationService.getClusterPrediction(memberId);
            // 장바구니의 모든 아이템에 대해 선호도 업데이트
            for (OrderItem orderItem : orderItemList) {
                updateClusterItemPreference(memberId, orderItem.getItem());
            }
        } catch (Exception e) {
            System.err.println("Error occurred during cluster processing: " + e.getMessage());
        }

        return save.getId();

    }

    private void deleteCartItem(CartOrderDto cartOrderDto) {
        List<CartForm> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();
        for (CartForm cartForm : cartOrderDtoList) {
            cartService.deleteCartItem(cartForm.getCartItemId());
        }
    }

    /*
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
                
        // 이미 취소된 주문인지 확인
        if (order.getStatus() == OrderStatus.CANCEL) {
            throw new IllegalStateException("Already cancelled order");
        }
        
        // 주문 취소 처리
        order.cancelOrder();
        
        // 재고 원복
        order.getOrderItems().forEach(OrderItem::cancel);
    }

    public List<LineItem> retrieveLineItems(String sessionId) throws StripeException {

        Session session = Session.retrieve(sessionId);

        //Line Items 조회
        SessionListLineItemsParams params = SessionListLineItemsParams.builder().build();
        StripeCollection<LineItem> lineItemCollection = session.listLineItems(params);

        return lineItemCollection.getData();
    }

    public List<OrderDto> findOrderById(Long orderId) {

        Optional<Order> findOrder = Optional.ofNullable(orderRepository.findById(orderId).orElseGet(() -> null));

        return findOrder.map(order -> List.of(new OrderDto(order.getId(), order.getTotalPrice(),order.getOrderDate(),order.getStatus(), order.getPaymentIntentId())))
                .orElseGet(Collections::emptyList);
    }

    private void updateClusterItemPreference(Long memberId, Item item) {
        try {
            // 입력 파라미터 로깅
            System.out.println("\n[클러스터 선호도 업데이트 시작] ============================");
            System.out.println("► 회원 ID: " + memberId);
            System.out.println("► 상품명: " + item.getName() + " (상품 ID: " + item.getId() + ")");

            // 회원의 클러스터 정보 조회
            MemberInfo memberInfo = memberInfoRepository.findById(memberId+1000)
                    .orElseThrow(() -> new IllegalArgumentException("Member information not found."));

            Cluster cluster = memberInfo.getCluster_id();
            if (cluster == null) {
                System.out.println("\n[주의] 회원 " + memberId + "번은 아직 클러스터가 할당되지 않았습니다.");
                return;
            }
            
            System.out.println("\n[클러스터 정보 확인]");
            System.out.println("► 회원 " + memberId + "번은 클러스터 " + cluster.getClusterNumber() + "번에 속해있습니다.");

            // 클러스터-상품 선호도 조회 또는 생성
            ClusterItemPreference preference = clusterItemPreferenceRepository
                    .findByClusterAndItem(cluster, item)
                    .orElseGet(() -> {
                        System.out.println("\n[신규 선호도 생성]");
                        System.out.println("► 클러스터 " + cluster.getClusterNumber() + "번의 상품 '" + item.getName() + "' 선호도를 새로 생성합니다.");
                        ClusterItemPreference newPreference = new ClusterItemPreference(cluster, item);
                        return clusterItemPreferenceRepository.save(newPreference);
                    });

            // 선호도 점수 증가
            preference.increasePreferenceScore();
            clusterItemPreferenceRepository.save(preference);
            
            System.out.println("\n[선호도 업데이트 완료]");
            System.out.println("► 클러스터: " + cluster.getClusterNumber() + "번");
            System.out.println("► 상품명: " + item.getName());
            System.out.println("► 최종 선호도 점수: " + preference.getPreferenceScore());
            System.out.println("================================================\n");

        } catch (Exception e) {
            System.err.println("\n[에러 발생] ========================================");
            System.err.println("► 회원 ID: " + memberId);
            System.err.println("► 상품 ID: " + item.getId());
            System.err.println("► 에러 내용: " + e.getMessage());
            System.err.println("================================================\n");
        }
    }
}
