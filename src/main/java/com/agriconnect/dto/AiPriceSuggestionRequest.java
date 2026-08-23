package com.agriconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiPriceSuggestionRequest {
    @NotBlank
    private String category;

    @NotNull
    private Integer quantity;

    private String unit;

    private String qualityGrade; // e.g. Premium, Standard, Economy
}
