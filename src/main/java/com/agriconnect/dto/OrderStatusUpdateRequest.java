package com.agriconnect.dto;

import com.agriconnect.entity.OrderStatus;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {
    private OrderStatus status;
}
