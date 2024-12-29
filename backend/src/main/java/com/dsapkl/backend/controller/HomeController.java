package com.dsapkl.backend.controller;

import com.dsapkl.backend.controller.dto.ItemSearchCondition;
import com.dsapkl.backend.entity.Item;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.repository.query.CartQueryDto;
import com.dsapkl.backend.repository.query.MainItemDto;
import com.dsapkl.backend.repository.query.MainItemQueryRepository;
import com.dsapkl.backend.service.CartService;
import com.dsapkl.backend.service.ItemService;
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

import java.util.List;
import java.util.Optional;

import static com.dsapkl.backend.controller.CartController.getMember;

@Controller
//@Slf4j
@RequiredArgsConstructor
public class HomeController {
    private final ItemService itemService;
    private final CartService cartService;
//    private final MainItemQueryRepository mainItemQueryRepository;
//    private final ItemService itemService;
//    private final ItemImageService itemImageService;
//    private final MainItemQueryRepository mainItemQueryRepository;
//    private final FileHandler fileHandler;

//    @GetMapping("/")
//    public String home2(@ModelAttribute Optional<Integer> page, ItemSearchCondition itemSearchCondition, Model model) {
//
//        PageRequest pageRequest = PageRequest.of(page.orElseGet(() -> 0), 3);
//
//        Page<MainItemDto> result = mainItemQueryRepository.findMainItem(pageRequest, itemSearchCondition);
//
//        model.addAttribute("result", result);
//        model.addAttribute("maxPage", 10);
//
//
//        return "home";
//}

    @GetMapping("/")
    public String home(Model model, HttpServletRequest request) {

        List<Item> items = itemService.findItems();
//        List<ItemImage> itemImages = itemImageService.findAllByDeleteYN("N");
        //queryDSL TODO
        //패치 조인 일반 조인 비교 TODO
        //페이징 기능 TODO,

//        //엔티티 -> DTO
//        List<ItemListDto> itemListDto = items.stream()
//                .map(ItemListDto::new)
//                .collect(Collectors.toList());
        model.addAttribute("items", items);
        HttpSession session = request.getSession(false);

        //비로그인 사용자
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
//            log.info("home controller");
            return "home";
        }
        //로그인된 사용자
//        log.info("userHome Controller");
        // th:text="${cartItemCount}" 쓰기 위함
        Member member = getMember(request);
        List<CartQueryDto> cartItemListForm = cartService.findCartItems(member.getId());
        int cartItemCount = cartItemListForm.size();
        model.addAttribute("cartItemListForm", cartItemListForm);
        model.addAttribute("cartItemCount", cartItemCount);

        return "index";
    }
}
