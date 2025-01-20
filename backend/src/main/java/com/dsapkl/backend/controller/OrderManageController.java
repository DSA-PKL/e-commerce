package com.dsapkl.backend.controller;

import com.dsapkl.backend.entity.Order;
import com.dsapkl.backend.service.OrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/orders/manage")
@RequiredArgsConstructor
public class OrderManageController {

    private final OrderService orderService;

    @GetMapping("")
    public String manageOrders(Model model) {
        List<Order> orders = orderService.findAllOrders();
        model.addAttribute("orders", orders);
        return "orders/orderManage";
    }

    @PostMapping("/{id}/status")
    @ResponseBody
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderStatusUpdateRequest request) {
        try {
            orderService.updateOrderStatus(id, request.getStatus());
            return ResponseEntity.ok("Order status updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred while updating order status");
        }
    }
}

@Data
class OrderStatusUpdateRequest {
    private String status;
} 