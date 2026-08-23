package com.agriconnect.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignDeliveryRequest {
    @NotNull
    private Long deliveryPartnerId;
}
