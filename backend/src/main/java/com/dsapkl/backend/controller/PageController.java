package com.dsapkl.backend.controller;

import com.dsapkl.backend.config.AuthenticatedUser;
import com.dsapkl.backend.entity.*;
import com.dsapkl.backend.repository.ClusterItemPreferenceRepository;
import com.dsapkl.backend.repository.MemberInfoRepository;
import com.dsapkl.backend.repository.MemberRepository;
import com.dsapkl.backend.repository.OrderDto;
import com.dsapkl.backend.repository.query.CartQueryDto;
import com.dsapkl.backend.service.CartService;
import com.dsapkl.backend.service.ItemService;
import com.dsapkl.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PageController {
    private final ItemService itemService;
    private final CartService cartService;
    private final OrderService orderService;
    private final MemberInfoRepository memberInfoRepository;
    private final ClusterItemPreferenceRepository clusterItemPreferenceRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/admin")
    public String adminPage(Model model,
                            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                            @RequestParam(value = "query", required = false) String query,
                            @RequestParam(value = "orderStatus", required = false) OrderStatus orderStatus,
                            @RequestParam(value = "category", required = false) String category) {

        Member member = memberRepository.findByEmail(authenticatedUser.getEmail()).orElseThrow(()-> new IllegalArgumentException("회원 정보를 찾을 수 없습니다. (SECURITY)"));

        List<Item> items = itemService.searchItems(query, category);

//        try {
//            MemberInfo memberInfo = memberInfoRepository.findByMemberId(member.getId())
//                    .orElseThrow(()-> new IllegalArgumentException("memberInfo 정보를 찾을 수 없습니다."));
//
//            Cluster cluster = memberInfo.getCluster_id();
//            if (cluster != null) {
//                List<Item> sortedItems = items.stream()
//                        .sorted((item1, item2) -> {
//                            Integer score1 = getPreferenceScore(cluster, item1);
//                            Integer score2 = getPreferenceScore(cluster, item2);
//                            return score2.compareTo(score1);
//                        }).toList();
//                items = sortedItems;
//            }
//        } catch (Exception e) {
//            log.error("선호도 정렬 중 오류 발생: ", e);
//        }

        List<CartQueryDto> cartItemListForm = cartService.findCartItems(member.getId());
        model.addAttribute("cartItemListForm", cartItemListForm);
        model.addAttribute("cartItemCount", cartItemListForm.size());

        List<OrderDto> orderDetail = orderService.findOrdersDetail(member.getId(), orderStatus);
        long orderCount = orderDetail.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.ORDER)
                .count();

        model.addAttribute("items", items);
        model.addAttribute("orderCount", orderCount);

        return "auth/admin";
    }

    @GetMapping("/user")
    public String userPage(Model model,
                           @RequestParam(value = "query", required = false) String query,
                           @RequestParam(value = "category", required = false) String category,
                           @RequestParam(value = "orderStatus", required = false) OrderStatus orderStatus,
                           @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        Member member = memberRepository.findByEmail(authenticatedUser.getEmail()).orElseThrow(()-> new IllegalArgumentException("회원 정보를 찾을 수 없습니다. (SECURITY)"));

        List<Item> items = itemService.searchItems(query, category);

//        try {
//            MemberInfo memberInfo = memberInfoRepository.findByMemberId(member.getId())
//                    .orElseThrow(()-> new IllegalArgumentException("memberInfo 정보를 찾을 수 없습니다."));
//
//            Cluster cluster = memberInfo.getCluster_id();
//            if (cluster != null) {
//                List<Item> sortedItems = items.stream()
//                        .sorted((item1, item2) -> {
//                            Integer score1 = getPreferenceScore(cluster, item1);
//                            Integer score2 = getPreferenceScore(cluster, item2);
//                            return score2.compareTo(score1);
//                        }).toList();
//                items = sortedItems;
//            }
//        } catch (Exception e) {
//            log.error("선호도 정렬 중 오류 발생: ", e);
//        }

        List<CartQueryDto> cartItemListForm = cartService.findCartItems(member.getId());
        model.addAttribute("cartItemListForm", cartItemListForm);
        model.addAttribute("cartItemCount", cartItemListForm.size());

        List<OrderDto> orderDetail = orderService.findOrdersDetail(member.getId(), orderStatus);
        long orderCount = orderDetail.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.ORDER)
                .count();

        model.addAttribute("items", items);
        model.addAttribute("orderCount", orderCount);

        return "auth/user";
    }


    @GetMapping("/guest")
    public String homePage(Model model,
                           @RequestParam(value = "query", required = false) String query,
                           @RequestParam(value = "category", required = false) String category) {
        List<Item> items = itemService.searchItems(query, category);
        model.addAttribute("items", items);
        return "home";
    }

    private Integer getPreferenceScore(Cluster cluster, Item item) {
        return clusterItemPreferenceRepository.findByClusterAndItem(cluster, item)
                .map(ClusterItemPreference::getPreferenceScore)
                .orElse(0);
    }

}
