package com.agriconnect.repository;

import com.agriconnect.entity.Order;
import com.agriconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomer(User customer);
    List<Order> findByCustomerId(Long customerId);
}
