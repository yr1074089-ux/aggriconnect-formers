package com.agriconnect.controller;

import com.agriconnect.entity.Order;
import com.agriconnect.entity.Role;
import com.agriconnect.entity.User;
import com.agriconnect.repository.OrderRepository;
import com.agriconnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final OrderRepository orderRepository;

    @GetMapping("/users")
    public ResponseEntity<List<User>> allUsers() {
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<User>> usersByRole(@PathVariable Role role) {
        return ResponseEntity.ok(userService.getByRole(role));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> allOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }
}
