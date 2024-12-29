package com.dsapkl.backend.service;

import com.dsapkl.backend.controller.dto.CartForm;
import com.dsapkl.backend.controller.dto.CartOrderDto;
import com.dsapkl.backend.repository.*;
import com.dsapkl.backend.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final CartService cartService;

    public OrderService(ItemRepository itemRepository, MemberRepository memberRepository, OrderRepository orderRepository, CartService cartService) {
        this.itemRepository = itemRepository;
        this.memberRepository = memberRepository;
        this.orderRepository = orderRepository;
        this.cartService = cartService;
    }

    /**
     * 단일 주문
     */
    public Long order(Long memberId, Long itemId, int count) {

        List<OrderItem> orderItemList = new ArrayList<>();
        Item findItem = itemRepository.findById(itemId).orElseGet(() -> null);
        Member findMember = memberRepository
                .findById(memberId).orElseGet(() -> null);
        int orderPrice = findItem.getPrice() * count;

        OrderItem orderItem = OrderItem.createOrderItem(count, orderPrice, findItem);
//        orderItemList.add(orderItem);

        OrderStatus orderStatus = OrderStatus.ORDER;

        Order order = Order.createOrder(orderStatus ,findMember, orderItemList);

        Order save = orderRepository.save(order);
        return save.getId();

    }


    /**
     * 주문 목록 조회
     */
    @Transactional(readOnly = true)
    public List<OrderDto> findOrdersDetail(Long memberId, OrderStatus orderStatus) {
        return orderRepository.findOrderDetail(memberId, orderStatus);
//        return null;
    }

    /**
     * 장바구니 상품들 주문
     */
    public Long orders(Long memberId, CartOrderDto cartOrderDto) {

        List<OrderItem> orderItemList = new ArrayList<>();
        Member findMember = memberRepository.findById(memberId).orElse(null);

        List<CartForm> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();
        for (CartForm cartForm : cartOrderDtoList) {
            Item findItem = itemRepository.findById(cartForm.getItemId()).orElse(null);
            int orderPrice = findItem.getPrice() * cartForm.getCount();

            OrderItem orderItem = OrderItem.createOrderItem(cartForm.getCount(), orderPrice, findItem);
            orderItemList.add(orderItem);
        }

        OrderStatus orderStatus = OrderStatus.ORDER;

        Order order = Order.createOrder(orderStatus, findMember, orderItemList);

        Order save = orderRepository.save(order);

        //주문한 상품은 장바구니에서 제거
        deleteCartItem(cartOrderDto);

        return save.getId();

    }

    private void deleteCartItem(CartOrderDto cartOrderDto) {
        List<CartForm> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();
        for (CartForm cartForm : cartOrderDtoList) {
            cartService.deleteCartItem(cartForm.getItemId());
        }
    }

    /*
     * 주문 취소
     */
    public void cancelOrder(Long orderId) {
        Order findOrder = orderRepository.findById(orderId).orElseGet(() -> null);
        findOrder.cancelOrder();
        orderRepository.flush();
    }


}
