package com.agriconnect.controller;

import com.agriconnect.dto.AssignDeliveryRequest;
import com.agriconnect.dto.DeliveryStatusUpdateRequest;
import com.agriconnect.entity.Delivery;
import com.agriconnect.security.CustomUserDetails;
import com.agriconnect.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    // Available/unassigned tasks a delivery partner can pick up
    @GetMapping("/tasks/available")
    public ResponseEntity<List<Delivery>> availableTasks() {
        return ResponseEntity.ok(deliveryService.getUnassignedDeliveries());
    }

    // Tasks currently assigned to the logged-in delivery partner
    @GetMapping("/tasks/mine")
    public ResponseEntity<List<Delivery>> myTasks(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(deliveryService.getTasksForPartner(principal.getUser()));
    }

    @PostMapping("/tasks/{deliveryId}/accept")
    public ResponseEntity<Delivery> acceptTask(@AuthenticationPrincipal CustomUserDetails principal,
                                                @PathVariable Long deliveryId) {
        return ResponseEntity.ok(deliveryService.selfAssign(deliveryId, principal.getUser()));
    }

    @PatchMapping("/tasks/{deliveryId}/status")
    public ResponseEntity<Delivery> updateStatus(@AuthenticationPrincipal CustomUserDetails principal,
                                                  @PathVariable Long deliveryId,
                                                  @Valid @RequestBody DeliveryStatusUpdateRequest request) {
        Delivery updated = deliveryService.updateStatus(
                deliveryId, principal.getUser(), request.getStatus(), request.getCurrentLocation());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/order/{orderId}/track")
    public ResponseEntity<Delivery> trackByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(deliveryService.getByOrderId(orderId));
    }
}
