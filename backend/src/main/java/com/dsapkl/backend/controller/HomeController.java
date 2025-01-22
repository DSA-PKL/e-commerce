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
import java.util.ArrayList;

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
                      @RequestParam(required = false, defaultValue = "8") int size,
                      Model model,
                      HttpServletRequest request) {
        try {
            Member member = getMember(request);
            
            // 페이지네이션 설정 (0-based page index로 변환)
            PageRequest pageRequest = PageRequest.of(page - 1, size);
            
            // 검색 및 카테고리 필터링
            Page<Item> itemPage;
            
            // 검색어나 카테고리가 있는 경우
            if ((query != null && !query.trim().isEmpty()) || 
                (category != null && !category.trim().isEmpty())) {
                
                // 카테고리 처리
                if (category != null && !category.trim().isEmpty()) {
                    try {
                        Category categoryEnum = Category.valueOf(category.toUpperCase());
                        model.addAttribute("selectedCategory", categoryEnum);
                    } catch (IllegalArgumentException e) {
                        category = null;
                    }
                }
                
                // 검색 실행
                itemPage = itemService.searchItems(query, category, null, pageRequest);
            } else {
                // 일반 목록 조회
                itemPage = itemService.findItemsPage(pageRequest);
            }
            
            // 페이지네이션 정보 추가
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", Math.max(1, itemPage.getTotalPages()));
            model.addAttribute("totalItems", itemPage.getTotalElements());
            model.addAttribute("size", size);
            
            // 검색 결과 처리
            if (itemPage.isEmpty()) {
                model.addAttribute("noResults", true);
                model.addAttribute("items", new ArrayList<>());
            } else {
                model.addAttribute("items", itemPage.getContent());
            }
            
            // 검색 파라미터 유지
            model.addAttribute("query", query);
            model.addAttribute("category", category);
            
            // 로그인한 경우 추가 정보
            if (member != null) {
                List<CartQueryDto> cartItems = cartService.findCartItems(member.getId());
                model.addAttribute("cartItemCount", cartItems.size());
                
                List<OrderDto> orders = orderService.findOrdersDetail(member.getId(), OrderStatus.ORDER);
                model.addAttribute("orderCount", orders.size());
                
                return "index";
            }
            
            return "home";
            
        } catch (Exception e) {
            log.error("Error in home: ", e);
            model.addAttribute("error", "An error occurred while processing your request.");
            return "home";
        }
    }

    // 아이템의 선호도 점수를 조회하는 메서드
    private Integer getPreferenceScore(Cluster cluster, Item item) {
        return clusterItemPreferenceRepository.findByClusterAndItem(cluster, item)
                .map(ClusterItemPreference::getPreferenceScore)
                .orElse(0); // 선호도 정보가 없으면 0점
    }

}
