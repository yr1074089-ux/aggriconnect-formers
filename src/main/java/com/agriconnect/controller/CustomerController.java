package com.agriconnect.controller;

import com.agriconnect.dto.PlaceOrderRequest;
import com.agriconnect.entity.Order;
import com.agriconnect.entity.Product;
import com.agriconnect.security.CustomUserDetails;
import com.agriconnect.service.OrderService;
import com.agriconnect.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final ProductService productService;
    private final OrderService orderService;

    // ---------- Browse products (also reachable publicly under /api/products/browse) ----------

    @GetMapping("/products")
    public ResponseEntity<List<Product>> browseProducts(@RequestParam(required = false) String category,
                                                          @RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return ResponseEntity.ok(productService.search(keyword));
        }
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(productService.browseByCategory(category));
        }
        return ResponseEntity.ok(productService.browseAvailable());
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getById(productId));
    }

    // ---------- Orders ----------

    @PostMapping("/orders")
    public ResponseEntity<Order> placeOrder(@AuthenticationPrincipal CustomUserDetails principal,
                                             @Valid @RequestBody PlaceOrderRequest request) {
        Order order = orderService.placeOrder(principal.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> myOrders(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(orderService.getOrdersForCustomer(principal.getUser().getId()));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Order> getOrder(@AuthenticationPrincipal CustomUserDetails principal,
                                           @PathVariable Long orderId) {
        Order order = orderService.getById(orderId);
        orderService.assertOwnership(principal.getUser(), order);
        return ResponseEntity.ok(order);
    }
}
