package com.agriconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank
    private String name;

    private String description;

    private String category;

    @NotNull @Positive
    private BigDecimal price;

    @NotNull
    private Integer quantity;

    private String unit;

    private String imageUrl;

    // If true and description is blank, AI will auto-generate it
    private boolean useAiDescription;

    // If true, AI will suggest/override the price
    private boolean useAiPrice;
}
