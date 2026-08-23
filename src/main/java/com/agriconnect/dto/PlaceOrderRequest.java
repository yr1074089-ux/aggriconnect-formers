package com.agriconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class PlaceOrderRequest {
    @NotEmpty
    private List<OrderItemRequest> items;

    @NotBlank
    private String deliveryAddress;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
    }
}
