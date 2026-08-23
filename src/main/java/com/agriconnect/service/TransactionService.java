package com.agriconnect.service;

import com.agriconnect.entity.*;
import com.agriconnect.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    /**
     * Splits an order's total across each contributing farmer and records
     * a completed transaction (earning) for each of them. Called once a
     * delivery is marked DELIVERED.
     */
    public void recordEarningsForOrder(Order order) {
        Map<User, BigDecimal> earningsByFarmer = order.getItems().stream()
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getFarmer(),
                        Collectors.reducing(BigDecimal.ZERO,
                                item -> item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())),
                                BigDecimal::add)
                ));

        earningsByFarmer.forEach((farmer, amount) -> {
            Transaction transaction = Transaction.builder()
                    .farmer(farmer)
                    .order(order)
                    .amount(amount)
                    .status(TransactionStatus.COMPLETED)
                    .build();
            transactionRepository.save(transaction);
        });
    }

    public List<Transaction> getTransactionsForFarmer(Long farmerId) {
        return transactionRepository.findByFarmerId(farmerId);
    }

    public BigDecimal getTotalEarnings(Long farmerId) {
        return transactionRepository.findByFarmerId(farmerId).stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
