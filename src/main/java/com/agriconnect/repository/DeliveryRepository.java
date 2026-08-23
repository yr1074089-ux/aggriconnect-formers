package com.agriconnect.repository;

import com.agriconnect.entity.Delivery;
import com.agriconnect.entity.DeliveryStatus;
import com.agriconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrderId(Long orderId);
    List<Delivery> findByDeliveryPartner(User deliveryPartner);
    List<Delivery> findByStatus(DeliveryStatus status);
}
