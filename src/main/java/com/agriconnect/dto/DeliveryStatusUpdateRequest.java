package com.agriconnect.dto;

import com.agriconnect.entity.DeliveryStatus;
import lombok.Data;

@Data
public class DeliveryStatusUpdateRequest {
    private DeliveryStatus status;
    private String currentLocation;
}
