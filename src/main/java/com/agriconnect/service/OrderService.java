package com.agriconnect.service;

import com.agriconnect.dto.PlaceOrderRequest;
import com.agriconnect.entity.*;
import com.agriconnect.exception.BadRequestException;
import com.agriconnect.exception.ResourceNotFoundException;
import com.agriconnect.exception.UnauthorizedActionException;
import com.agriconnect.repository.DeliveryRepository;
import com.agriconnect.repository.OrderRepository;
import com.agriconnect.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final DeliveryRepository deliveryRepository;

    @Transactional
    public Order placeOrder(User customer, PlaceOrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Order must contain at least one item");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        Order order = Order.builder()
                .customer(customer)
                .deliveryAddress(request.getDeliveryAddress())
                .status(OrderStatus.PLACED)
                .build();

        for (PlaceOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.getProductId()));

            if (!Boolean.TRUE.equals(product.getAvailable())) {
                throw new BadRequestException("Product '" + product.getName() + "' is currently unavailable");
            }
            if (product.getQuantity() < itemReq.getQuantity()) {
                throw new BadRequestException("Insufficient stock for '" + product.getName() + "'. Available: " + product.getQuantity());
            }

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();
            orderItems.add(orderItem);

            product.setQuantity(product.getQuantity() - itemReq.getQuantity());
            if (product.getQuantity() == 0) {
                product.setAvailable(false);
            }
            productRepository.save(product);
        }

        order.setItems(orderItems);
        order.setTotalAmount(total);
        order = orderRepository.save(order);

        // create an unassigned delivery record for this order so it enters the delivery pipeline
        Delivery delivery = Delivery.builder()
                .order(order)
                .status(DeliveryStatus.UNASSIGNED)
                .build();
        deliveryRepository.save(delivery);

        return order;
    }

    public List<Order> getOrdersForCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public List<Order> getOrdersForFarmer(Long farmerId) {
        return orderRepository.findAll().stream()
                .filter(order -> order.getItems().stream()
                        .anyMatch(item -> item.getProduct().getFarmer().getId().equals(farmerId)))
                .collect(Collectors.toList());
    }

    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public Order updateStatus(Long orderId, OrderStatus status) {
        Order order = getById(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public void assertOwnership(User customer, Order order) {
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorizedActionException("You do not have permission to view this order");
        }
    }
}
