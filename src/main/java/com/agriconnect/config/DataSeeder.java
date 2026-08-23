package com.agriconnect.config;

import com.agriconnect.entity.*;
import com.agriconnect.repository.ProductRepository;
import com.agriconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds a handful of demo accounts and products on startup (H2 in-memory DB only)
 * so the API can be explored immediately via Postman/Swagger without manual setup.
 * All demo accounts use the password: Passw0rd!
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        String demoPassword = passwordEncoder.encode("Passw0rd!");

        User farmer = userRepository.save(User.builder()
                .name("Ramesh Kumar")
                .email("farmer@demo.com")
                .password(demoPassword)
                .phone("9876500001")
                .address("Village Rampur, Uttar Pradesh")
                .role(Role.FARMER)
                .build());

        User customer = userRepository.save(User.builder()
                .name("Anjali Sharma")
                .email("customer@demo.com")
                .password(demoPassword)
                .phone("9876500002")
                .address("Hazratganj, Lucknow, Uttar Pradesh")
                .role(Role.CUSTOMER)
                .build());

        userRepository.save(User.builder()
                .name("Suresh Yadav")
                .email("delivery@demo.com")
                .password(demoPassword)
                .phone("9876500003")
                .address("Gomti Nagar, Lucknow, Uttar Pradesh")
                .role(Role.DELIVERY_PARTNER)
                .build());

        userRepository.save(User.builder()
                .name("Platform Admin")
                .email("admin@demo.com")
                .password(demoPassword)
                .role(Role.ADMIN)
                .build());

        productRepository.save(Product.builder()
                .name("Fresh Tomatoes")
                .description("Farm-fresh, naturally grown tomatoes picked at peak ripeness for maximum taste and nutrition.")
                .category("vegetables")
                .price(BigDecimal.valueOf(28.50))
                .quantity(200)
                .unit("kg")
                .aiGenerated(true)
                .available(true)
                .farmer(farmer)
                .build());

        productRepository.save(Product.builder()
                .name("Alphonso Mangoes")
                .description("Sweet, aromatic Alphonso mangoes sourced directly from local orchards.")
                .category("fruits")
                .price(BigDecimal.valueOf(75.00))
                .quantity(120)
                .unit("kg")
                .aiGenerated(true)
                .available(true)
                .farmer(farmer)
                .build());

        productRepository.save(Product.builder()
                .name("Basmati Rice")
                .description("Premium, long-grain basmati rice, naturally aged for aroma and flavor.")
                .category("grains")
                .price(BigDecimal.valueOf(55.00))
                .quantity(500)
                .unit("kg")
                .aiGenerated(false)
                .available(true)
                .farmer(farmer)
                .build());

        System.out.println("=================================================================");
        System.out.println(" Demo data seeded. Sample login credentials (password: Passw0rd!)");
        System.out.println("   Farmer:   farmer@demo.com");
        System.out.println("   Customer: customer@demo.com");
        System.out.println("   Delivery: delivery@demo.com");
        System.out.println("   Admin:    admin@demo.com");
        System.out.println("=================================================================");
    }
}
