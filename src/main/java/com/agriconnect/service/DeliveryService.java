package com.agriconnect.service;

import com.agriconnect.entity.*;
import com.agriconnect.exception.BadRequestException;
import com.agriconnect.exception.ResourceNotFoundException;
import com.agriconnect.exception.UnauthorizedActionException;
import com.agriconnect.repository.DeliveryRepository;
import com.agriconnect.repository.OrderRepository;
import com.agriconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    public List<Delivery> getUnassignedDeliveries() {
        return deliveryRepository.findByStatus(DeliveryStatus.UNASSIGNED);
    }

    public List<Delivery> getTasksForPartner(User partner) {
        return deliveryRepository.findByDeliveryPartner(partner);
    }

    public Delivery getByOrderId(Long orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No delivery record found for order: " + orderId));
    }

    public Delivery getById(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + id));
    }

    @Transactional
    public Delivery assignDeliveryPartner(Long deliveryId, Long deliveryPartnerId) {
        Delivery delivery = getById(deliveryId);

        User partner = userRepository.findById(deliveryPartnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery partner not found"));
        if (partner.getRole() != Role.DELIVERY_PARTNER) {
            throw new BadRequestException("Selected user is not a delivery partner");
        }

        delivery.setDeliveryPartner(partner);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setAssignedDate(LocalDateTime.now());
        deliveryRepository.save(delivery);

        Order order = delivery.getOrder();
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        return delivery;
    }

    /**
     * Allows a delivery partner to self pick-up an unassigned task.
     */
    @Transactional
    public Delivery selfAssign(Long deliveryId, User partner) {
        Delivery delivery = getById(deliveryId);
        if (delivery.getStatus() != DeliveryStatus.UNASSIGNED) {
            throw new BadRequestException("This delivery task has already been assigned");
        }
        return assignDeliveryPartner(deliveryId, partner.getId());
    }

    @Transactional
    public Delivery updateStatus(Long deliveryId, User partner, DeliveryStatus status, String currentLocation) {
        Delivery delivery = getById(deliveryId);

        if (delivery.getDeliveryPartner() == null || !delivery.getDeliveryPartner().getId().equals(partner.getId())) {
            throw new UnauthorizedActionException("You are not assigned to this delivery");
        }

        delivery.setStatus(status);
        if (currentLocation != null && !currentLocation.isBlank()) {
            delivery.setCurrentLocation(currentLocation);
        }

        Order order = delivery.getOrder();

        switch (status) {
            case PICKED_UP -> delivery.setPickedUpDate(LocalDateTime.now());
            case IN_TRANSIT -> order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
            case DELIVERED -> {
                delivery.setDeliveredDate(LocalDateTime.now());
                order.setStatus(OrderStatus.DELIVERED);
                transactionService.recordEarningsForOrder(order);
            }
            case CANCELLED -> order.setStatus(OrderStatus.CANCELLED);
            default -> { }
        }

        orderRepository.save(order);
        return deliveryRepository.save(delivery);
    }
}
