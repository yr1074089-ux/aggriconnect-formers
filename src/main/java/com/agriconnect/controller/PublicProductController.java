package com.agriconnect.controller;

import com.agriconnect.entity.Product;
import com.agriconnect.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public, unauthenticated storefront endpoints so prospective customers
 * can browse the marketplace before creating an account.
 */
@RestController
@RequestMapping("/api/products/browse")
@RequiredArgsConstructor
public class PublicProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> browse(@RequestParam(required = false) String category,
                                                 @RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return ResponseEntity.ok(productService.search(keyword));
        }
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(productService.browseByCategory(category));
        }
        return ResponseEntity.ok(productService.browseAvailable());
    }
}
