package com.agriconnect.service;

import com.agriconnect.dto.AiDescriptionRequest;
import com.agriconnect.dto.AiPriceSuggestionRequest;
import com.agriconnect.dto.ProductRequest;
import com.agriconnect.entity.Product;
import com.agriconnect.entity.User;
import com.agriconnect.exception.ResourceNotFoundException;
import com.agriconnect.exception.UnauthorizedActionException;
import com.agriconnect.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final AIService aiService;

    public Product createProduct(User farmer, ProductRequest request) {
        String category = (request.getCategory() == null || request.getCategory().isBlank())
                ? aiService.autoCategorize(request.getName())
                : request.getCategory();

        String description = request.getDescription();
        boolean aiGenerated = false;
        if (request.isUseAiDescription() || description == null || description.isBlank()) {
            AiDescriptionRequest aiReq = new AiDescriptionRequest();
            aiReq.setProductName(request.getName());
            aiReq.setCategory(category);
            description = aiService.generateProductDescription(aiReq);
            aiGenerated = true;
        }

        var price = request.getPrice();
        if (request.isUseAiPrice()) {
            AiPriceSuggestionRequest priceReq = new AiPriceSuggestionRequest();
            priceReq.setCategory(category);
            priceReq.setQuantity(request.getQuantity());
            priceReq.setUnit(request.getUnit());
            price = aiService.suggestPrice(priceReq);
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(description)
                .category(category)
                .price(price)
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .imageUrl(request.getImageUrl())
                .farmer(farmer)
                .aiGenerated(aiGenerated)
                .available(true)
                .build();

        return productRepository.save(product);
    }

    public Product updateProduct(User farmer, Long productId, ProductRequest request) {
        Product product = getById(productId);
        assertOwnership(farmer, product);

        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setQuantity(request.getQuantity());
        product.setUnit(request.getUnit());
        product.setImageUrl(request.getImageUrl());
        product.setUpdatedAt(LocalDateTime.now());

        if (request.isUseAiDescription()) {
            AiDescriptionRequest aiReq = new AiDescriptionRequest();
            aiReq.setProductName(request.getName());
            aiReq.setCategory(request.getCategory());
            product.setDescription(aiService.generateProductDescription(aiReq));
            product.setAiGenerated(true);
        } else if (request.getDescription() != null && !request.getDescription().isBlank()) {
            product.setDescription(request.getDescription());
            product.setAiGenerated(false);
        }

        if (request.isUseAiPrice()) {
            AiPriceSuggestionRequest priceReq = new AiPriceSuggestionRequest();
            priceReq.setCategory(request.getCategory());
            priceReq.setQuantity(request.getQuantity());
            priceReq.setUnit(request.getUnit());
            product.setPrice(aiService.suggestPrice(priceReq));
        } else {
            product.setPrice(request.getPrice());
        }

        return productRepository.save(product);
    }

    public void deleteProduct(User farmer, Long productId) {
        Product product = getById(productId);
        assertOwnership(farmer, product);
        productRepository.delete(product);
    }

    public void setAvailability(User farmer, Long productId, boolean available) {
        Product product = getById(productId);
        assertOwnership(farmer, product);
        product.setAvailable(available);
        productRepository.save(product);
    }

    public List<Product> getProductsByFarmer(Long farmerId) {
        return productRepository.findByFarmerId(farmerId);
    }

    public List<Product> browseAvailable() {
        return productRepository.findByAvailableTrue();
    }

    public List<Product> browseByCategory(String category) {
        return productRepository.findByCategoryIgnoreCaseAndAvailableTrue(category);
    }

    public List<Product> search(String keyword) {
        return productRepository.findByNameContainingIgnoreCaseAndAvailableTrue(keyword);
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private void assertOwnership(User farmer, Product product) {
        if (!product.getFarmer().getId().equals(farmer.getId())) {
            throw new UnauthorizedActionException("You do not have permission to modify this product");
        }
    }
}
