package com.agriconnect.repository;

import com.agriconnect.entity.Product;
import com.agriconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByFarmer(User farmer);
    List<Product> findByFarmerId(Long farmerId);
    List<Product> findByAvailableTrue();
    List<Product> findByCategoryIgnoreCaseAndAvailableTrue(String category);
    List<Product> findByNameContainingIgnoreCaseAndAvailableTrue(String name);
}
