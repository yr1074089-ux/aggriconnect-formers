package com.agriconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiDescriptionRequest {
    @NotBlank
    private String productName;

    private String category;

    private String keyFeatures; // comma separated highlights e.g. "organic,fresh,pesticide-free"
}
