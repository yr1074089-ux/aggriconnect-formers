package com.agriconnect.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"items", "hibernateLazyInitializer", "handler"})
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_partner_id")
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private User deliveryPartner;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.UNASSIGNED;

    private String currentLocation;

    private LocalDateTime assignedDate;

    private LocalDateTime pickedUpDate;

    private LocalDateTime deliveredDate;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
