package com.dsapkl.backend.restcontroller.user;

import com.dsapkl.backend.entity.Item;
import com.dsapkl.backend.service.CartService;
import com.dsapkl.backend.service.ItemImageService;
import com.dsapkl.backend.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserItemRestController {

    private final ItemService itemService;
    private final ItemImageService itemImageService;
    private final CartService cartService;

    @GetMapping("/api/items/{itemId}/rating")
    public ResponseEntity<Map<String, Object>> getItemRating(@PathVariable Long itemId) {
        Item item = itemService.findItem(itemId);
        Map<String, Object> response = new HashMap<>();
        response.put("averageRating", item.getAverageRating());
        response.put("reviewCount", item.getReviewCount());
        return ResponseEntity.ok(response);
    }

}
