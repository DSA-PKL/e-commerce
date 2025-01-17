package com.dsapkl.backend.controller;

import com.dsapkl.backend.controller.dto.ItemSearchCondition;
import com.dsapkl.backend.entity.*;
import com.dsapkl.backend.repository.ClusterItemPreferenceRepository;
import com.dsapkl.backend.repository.MemberInfoRepository;
import com.dsapkl.backend.repository.OrderDto;
import com.dsapkl.backend.repository.query.CartQueryDto;
import com.dsapkl.backend.repository.query.MainItemDto;
import com.dsapkl.backend.repository.query.MainItemQueryRepository;
import com.dsapkl.backend.service.CartService;
import com.dsapkl.backend.service.ItemService;
import com.dsapkl.backend.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

import static com.dsapkl.backend.controller.CartController.getMember;

@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController {
    private final ItemService itemService;
    private final CartService cartService;
    private final OrderService orderService;

    @GetMapping("/")
    public String home(@RequestParam(value = "query", required = false) String query,
                      @RequestParam(value = "orderStatus", required = false) OrderStatus orderStatus,
                      @RequestParam(value = "category", required = false) String category,
                      Model model, HttpServletRequest request) {

        List<Item> items = itemService.searchItems(query, category);
        HttpSession session = request.getSession(false);

        // 비로그인 사용자
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            model.addAttribute("items", items);
            return "home";
        }
        //로그인된 사용자

        // 로그인된 사용자
        Member member = getMember(request);

        try {
            // 회원의 클러스터 정보 조회
            MemberInfo memberInfo = memberInfoRepository.findById(member.getId())
                    .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

            Cluster cluster = memberInfo.getCluster_id();
            if (cluster != null) {
                // 검색된 아이템들의 선호도 정보를 조회하고 정렬
                List<Item> sortedItems = items.stream()
                    .sorted((item1, item2) -> {
                        Integer score1 = getPreferenceScore(cluster, item1);
                        Integer score2 = getPreferenceScore(cluster, item2);
                        return score2.compareTo(score1); // 내림차순 정렬
                    })
                    .toList();
                items = sortedItems;
            }
        } catch (Exception e) {
            // 오류 발생 시 기본 검색 결과 유지
            System.err.println("선호도 정렬 중 오류 발생: " + e.getMessage());
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
