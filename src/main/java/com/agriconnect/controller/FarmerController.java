package com.agriconnect.controller;

import com.agriconnect.dto.*;
import com.agriconnect.entity.Order;
import com.agriconnect.entity.Product;
import com.agriconnect.entity.Transaction;
import com.agriconnect.security.CustomUserDetails;
import com.agriconnect.service.AIService;
import com.agriconnect.service.OrderService;
import com.agriconnect.service.ProductService;
import com.agriconnect.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/farmer")
@RequiredArgsConstructor
public class FarmerController {

    private final ProductService productService;
    private final OrderService orderService;
    private final TransactionService transactionService;
    private final AIService aiService;

    // ---------- Product management ----------

    @PostMapping("/products")
    public ResponseEntity<Product> addProduct(@AuthenticationPrincipal CustomUserDetails principal,
                                               @Valid @RequestBody ProductRequest request) {
        Product product = productService.createProduct(principal.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<Product> updateProduct(@AuthenticationPrincipal CustomUserDetails principal,
                                                  @PathVariable Long productId,
                                                  @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(principal.getUser(), productId, request));
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<ApiResponse> deleteProduct(@AuthenticationPrincipal CustomUserDetails principal,
                                                       @PathVariable Long productId) {
        productService.deleteProduct(principal.getUser(), productId);
        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Product deleted").build());
    }

    @PatchMapping("/products/{productId}/availability")
    public ResponseEntity<ApiResponse> setAvailability(@AuthenticationPrincipal CustomUserDetails principal,
                                                          @PathVariable Long productId,
                                                          @RequestParam boolean available) {
        productService.setAvailability(principal.getUser(), productId, available);
        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Availability updated").build());
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> myProducts(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(productService.getProductsByFarmer(principal.getUser().getId()));
    }

    // ---------- AI-powered tools ----------

    @PostMapping("/ai/generate-description")
    public ResponseEntity<Map<String, String>> generateDescription(@Valid @RequestBody AiDescriptionRequest request) {
        String description = aiService.generateProductDescription(request);
        return ResponseEntity.ok(Map.of("description", description));
    }

    @PostMapping("/ai/suggest-price")
    public ResponseEntity<Map<String, BigDecimal>> suggestPrice(@Valid @RequestBody AiPriceSuggestionRequest request) {
        BigDecimal price = aiService.suggestPrice(request);
        return ResponseEntity.ok(Map.of("suggestedPrice", price));
    }

    @GetMapping("/ai/auto-categorize")
    public ResponseEntity<Map<String, String>> autoCategorize(@RequestParam String productName) {
        return ResponseEntity.ok(Map.of("category", aiService.autoCategorize(productName)));
    }

    // ---------- Orders relevant to this farmer ----------

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> myOrders(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(orderService.getOrdersForFarmer(principal.getUser().getId()));
    }

    // ---------- Earnings / transaction history ----------

    @GetMapping("/earnings")
    public ResponseEntity<List<Transaction>> earnings(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(transactionService.getTransactionsForFarmer(principal.getUser().getId()));
    }

    @GetMapping("/earnings/total")
    public ResponseEntity<Map<String, BigDecimal>> totalEarnings(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(Map.of("totalEarnings", transactionService.getTotalEarnings(principal.getUser().getId())));
    }
}
