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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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
    public String home(@RequestParam(value = "query", required = false) String query,
                      @RequestParam(value = "orderStatus", required = false) OrderStatus orderStatus,
                      @RequestParam(value = "category", required = false) String category,
                      Model model, HttpServletRequest request) {

        // 검색어와 카테고리를 모델에 추가
        model.addAttribute("searchQuery", query);
        model.addAttribute("selectedCategory", category != null ? Category.valueOf(category) : null);

        List<Item> items = itemService.searchItems(query, category);
        HttpSession session = request.getSession(false);

        // 비로그인 사용자
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            model.addAttribute("items", items);
            return "home";
        }

        // 로그인된 사용자
        Member member = getMember(request);

        try {
            MemberInfo memberInfo = memberInfoRepository.findById(member.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Member information not found."));

            Cluster cluster = memberInfo.getCluster_id();
            if (cluster != null) {
                // 최대 선호도 점수 계산
                int maxScore = 0;
                for (Item item : items) {
                    if (!item.getScore().isEmpty()) {
                        maxScore = Math.max(maxScore, item.getScore().get(0).getPreferenceScore());
                    }
                }
                
                model.addAttribute("maxScore", maxScore);
                
                // 정렬 로직은 유지
                List<Item> sortedItems = items.stream()
                    .sorted((item1, item2) -> {
                        Integer score1 = getPreferenceScore(cluster, item1);
                        Integer score2 = getPreferenceScore(cluster, item2);
                        return score2.compareTo(score1);
                    })
                    .toList();
                items = sortedItems;
            }
        } catch (Exception e) {
            System.err.println("Error occurred while sorting preferences: " + e.getMessage());
        }

        model.addAttribute("items", items);

        // 장바구니 정보
        List<CartQueryDto> cartItemListForm = cartService.findCartItems(member.getId());
        int cartItemCount = cartItemListForm.size();
        model.addAttribute("cartItemListForm", cartItemListForm);
        model.addAttribute("cartItemCount", cartItemCount);

        // 주문 정보
        List<OrderDto> ordersDetail = orderService.findOrdersDetail(member.getId(), orderStatus);

        long orderCount = ordersDetail.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.ORDER)
                .count();
        model.addAttribute("orderCount", orderCount);

        return "index";
    }

    // 아이템의 선호도 점수를 조회하는 메서드
    private Integer getPreferenceScore(Cluster cluster, Item item) {
        return clusterItemPreferenceRepository.findByClusterAndItem(cluster, item)
                .map(ClusterItemPreference::getPreferenceScore)
                .orElse(0); // 선호도 정보가 없으면 0점
    }

}
