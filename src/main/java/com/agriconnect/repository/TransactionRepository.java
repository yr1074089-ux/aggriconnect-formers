package com.agriconnect.repository;

import com.agriconnect.entity.Transaction;
import com.agriconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFarmer(User farmer);
    List<Transaction> findByFarmerId(Long farmerId);
}
