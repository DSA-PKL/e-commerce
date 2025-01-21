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
                      @RequestParam(required = false, defaultValue = "0") int page,
                      Model model,
                      HttpServletRequest request) {
        
        Member member = getMember(request);
        
        Pageable pageable = PageRequest.of(page, 12);
        Page<Item> items;
        
        // 검색 조건이 있는 경우
        if ((query != null && !query.trim().isEmpty()) || 
            (category != null && !category.trim().isEmpty())) {
            items = itemService.searchItems(query, category, null, pageable);
        } else {
            items = itemService.findItemsPage(pageable);
        }
        
        model.addAttribute("items", items);
        model.addAttribute("maxPage", items.getTotalPages());
        model.addAttribute("page", page);
        model.addAttribute("query", query);
        model.addAttribute("category", category);
        
        // 로그인한 경우
        if (member != null) {
            // 장바구니 아이템 카운트
            List<CartQueryDto> cartItems = cartService.findCartItems(member.getId());
            model.addAttribute("cartItemCount", cartItems.size());
            
            // 주문 개수
            List<OrderDto> orders = orderService.findOrdersDetail(member.getId(), OrderStatus.ORDER);
            model.addAttribute("orderCount", orders.size());
            
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

}
