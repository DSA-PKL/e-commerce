package com.dsapkl.backend.controller;

import com.dsapkl.backend.entity.*;
import com.dsapkl.backend.repository.ClusterItemPreferenceRepository;
import com.dsapkl.backend.repository.MemberInfoRepository;
import com.dsapkl.backend.repository.OrderDto;
import com.dsapkl.backend.repository.query.CartQueryDto;
import com.dsapkl.backend.service.CartService;
import com.dsapkl.backend.service.ItemService;
import com.dsapkl.backend.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

import static com.dsapkl.backend.controller.CartController.getMember;
import com.dsapkl.backend.entity.Category;

@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController {
    private final ItemService itemService;
    private final CartService cartService;
    private final OrderService orderService;
    private final MemberInfoRepository memberInfoRepository;
    private final ClusterItemPreferenceRepository clusterItemPreferenceRepository;

    @GetMapping("/")
    public String home(@RequestParam(required = false) String query,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false, defaultValue = "1") int page,
                       Model model,
                       HttpServletRequest request) {

        Member member = getMember(request);

        // 페이지는 0부터 시작하므로 1을 빼줍니다
        Pageable pageable = PageRequest.of(page - 1, 12);
        Page<Item> items;

        // 검색 조건이 있는 경우
        if ((query != null && !query.trim().isEmpty()) ||
                (category != null && !category.trim().isEmpty())) {
            items = itemService.searchItems(query, category, null, pageable);
        } else {
            items = itemService.findItemsPage(pageable);
        }

        // 페이지네이션을 위한 변수들 계산
        int totalPages = items.getTotalPages();
        int currentPage = page;
        int pageGroup = (currentPage - 1) / 10;
        int startPage = pageGroup * 10 + 1;
        int endPage = Math.min(startPage + 9, totalPages);

        // 이전/다음 그룹의 첫 페이지 계산
        int prevGroupPage = startPage - 10;
        int nextGroupPage = startPage + 10;

        // 페이지네이션 관련 모델 속성 추가
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("prevGroupPage", prevGroupPage);
        model.addAttribute("nextGroupPage", nextGroupPage);

        // 기존 모델 속성들
        model.addAttribute("items", items.getContent());
        model.addAttribute("query", query);
        model.addAttribute("category", category);

        // 카테고리 선택 상태 유지
        if (category != null && !category.trim().isEmpty()) {
            try {
                Category selectedCategory = Category.valueOf(category.toUpperCase());
                model.addAttribute("selectedCategory", selectedCategory);
            } catch (IllegalArgumentException e) {
                // 잘못된 카테고리 값은 무시
            }
        }

        // 로그인한 경우
        if (member != null) {
            System.out.println("\n[추천 시스템 시작] =====================================");
            System.out.println("► 로그인 회원 정보");
            System.out.println("  - 회원 ID: " + member.getId());
            System.out.println("  - 회원 이름: " + member.getName());

            // 회원 정보 조회
            MemberInfo memberInfo = memberInfoRepository.findByMemberId(member.getId())
                    .orElse(null);

            System.out.println("\n[회원 상세 정보 조회]");
            if (memberInfo == null) {
                System.out.println("► 회원 상세 정보가 없습니다.");
            } else {
                System.out.println("► 회원 상세 정보");
                System.out.println("  - 나이: " + memberInfo.getAge());
                System.out.println("  - 성별: " + memberInfo.getGender());
                System.out.println("  - 관심사: " + memberInfo.getInterests());
                System.out.println("  - 구매 빈도: " + memberInfo.getPurchaseFrequency());
            }

            // 선호도 기반 추천 상품 가져오기
            List<Item> recommendedItems;
            if (memberInfo != null && memberInfo.getCluster_id() != null) {
                Cluster cluster = memberInfo.getCluster_id();
                System.out.println("\n[클러스터 정보]");
                System.out.println("► 클러스터 번호: " + cluster.getClusterNumber());
                
                // 클러스터의 선호도 높은 상품 4개 조회
                List<ClusterItemPreference> preferences = clusterItemPreferenceRepository
                        .findByClusterIdOrderByPreferenceScoreDesc(cluster.getId());

                System.out.println("\n[클러스터 선호도 조회]");
                System.out.println("► 조회된 선호도 데이터 수: " + preferences.size());

                if (!preferences.isEmpty()) {
                    System.out.println("\n[선호도 기반 추천]");
                    recommendedItems = preferences.stream()
                            .map(ClusterItemPreference::getItem)
                            .limit(4)
                            .collect(Collectors.toList());
                    
                    System.out.println("► 추천된 상품 목록");
                    recommendedItems.forEach(item -> {
                        System.out.println("  - 상품명: " + item.getName());
                        System.out.println("    · ID: " + item.getId());
                        System.out.println("    · 카테고리: " + item.getCategory());
                        System.out.println("    · 가격: " + item.getPrice());
                    });
                    
                    model.addAttribute("isRecommended", true);
                } else {
                    System.out.println("\n[최신 상품 추천] (선호도 데이터 없음)");
                    recommendedItems = itemService.findLatestItems(4);
                    logLatestItems(recommendedItems);
                    model.addAttribute("isRecommended", false);
                }
            } else {
                System.out.println("\n[최신 상품 추천] (클러스터 정보 없음)");
                recommendedItems = itemService.findLatestItems(4);
                logLatestItems(recommendedItems);
                model.addAttribute("isRecommended", false);
            }

            model.addAttribute("recommendedItems", recommendedItems);

            // 장바구니 아이템 카운트
            List<CartQueryDto> cartItems = cartService.findCartItems(member.getId());
            System.out.println("\n[장바구니 정보]");
            System.out.println("► 장바구니 상품 수: " + cartItems.size());
            model.addAttribute("cartItemCount", cartItems.size());

            // 주문 개수
            List<OrderDto> orders = orderService.findOrdersDetail(member.getId(), OrderStatus.ORDER);
            System.out.println("\n[주문 정보]");
            System.out.println("► 총 주문 수: " + orders.size());
            model.addAttribute("orderCount", orders.size());

            System.out.println("================================================\n");
            return "index";
        }

        // 로그인하지 않은 경우
        return "home";
    }

    // 아이템의 선호도 점수를 조회하는 메서드
    private Integer getPreferenceScore(Cluster cluster, Item item) {
        return clusterItemPreferenceRepository.findByClusterAndItem(cluster, item)
                .map(ClusterItemPreference::getPreferenceScore)
                .orElse(0); // 선호도 정보가 없으면 0점
    }

    // 최신 상품 로깅을 위한 헬퍼 메소드
    private void logLatestItems(List<Item> items) {
        System.out.println("► 최신 상품 목록");
        items.forEach(item -> {
            System.out.println("  - 상품명: " + item.getName());
            System.out.println("    · ID: " + item.getId());
            System.out.println("    · 카테고리: " + item.getCategory());
            System.out.println("    · 가격: " + item.getPrice());
            System.out.println("    · 등록일: " + item.getCreatedDate());
        });
    }

}

