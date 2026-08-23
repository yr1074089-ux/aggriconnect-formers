package com.agriconnect.service;

import com.agriconnect.dto.AiDescriptionRequest;
import com.agriconnect.dto.AiPriceSuggestionRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AIService powers the "AI-powered tools" of the farmer portal:
 *  1) Auto-generating polished, marketing-ready product descriptions.
 *  2) Suggesting a fair market price based on category, quality grade and quantity.
 *  3) Auto-categorizing a product from its name when the farmer doesn't specify one.
 *
 * This is implemented as a deterministic, rule/template-based engine so the project
 * runs fully offline out of the box. To upgrade to a real LLM-backed implementation,
 * simply replace the method bodies below with calls to an external AI API
 * (e.g. the Anthropic or OpenAI Messages API) using the same method signatures -
 * no controller/service caller code needs to change.
 */
@Service
public class AIService {

    private static final Map<String, BigDecimal> BASE_PRICE_PER_UNIT = Map.ofEntries(
            Map.entry("vegetables", BigDecimal.valueOf(30)),
            Map.entry("fruits", BigDecimal.valueOf(60)),
            Map.entry("grains", BigDecimal.valueOf(45)),
            Map.entry("dairy", BigDecimal.valueOf(55)),
            Map.entry("pulses", BigDecimal.valueOf(90)),
            Map.entry("spices", BigDecimal.valueOf(150)),
            Map.entry("flowers", BigDecimal.valueOf(40)),
            Map.entry("poultry", BigDecimal.valueOf(180)),
            Map.entry("other", BigDecimal.valueOf(50))
    );

    private static final List<String> QUALITY_ADJECTIVES = Arrays.asList(
            "farm-fresh", "naturally grown", "carefully harvested", "sustainably sourced"
    );

    public String generateProductDescription(AiDescriptionRequest request) {
        String name = capitalize(request.getProductName());
        String category = request.getCategory() == null || request.getCategory().isBlank()
                ? autoCategorize(request.getProductName())
                : request.getCategory().toLowerCase();

        String adjective = QUALITY_ADJECTIVES.get(Math.abs(name.hashCode()) % QUALITY_ADJECTIVES.size());

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                "%s %s, sourced directly from local farms. This %s produce is picked at peak ripeness to ensure maximum freshness, taste, and nutrition.",
                adjective.substring(0, 1).toUpperCase() + adjective.substring(1), name.toLowerCase(), category
        ));

        if (request.getKeyFeatures() != null && !request.getKeyFeatures().isBlank()) {
            List<String> features = Arrays.stream(request.getKeyFeatures().split(","))
                    .map(String::trim)
                    .filter(f -> !f.isEmpty())
                    .collect(Collectors.toList());
            if (!features.isEmpty()) {
                sb.append(" Highlights: ").append(String.join(", ", features)).append(".");
            }
        }

        sb.append(" Buying directly from the farmer means better prices for you and fairer earnings for the grower who cultivated it.");

        return sb.toString();
    }

    public BigDecimal suggestPrice(AiPriceSuggestionRequest request) {
        String categoryKey = request.getCategory() == null ? "other" : request.getCategory().toLowerCase().trim();
        BigDecimal base = BASE_PRICE_PER_UNIT.getOrDefault(categoryKey, BASE_PRICE_PER_UNIT.get("other"));

        BigDecimal qualityMultiplier = switch (safeGrade(request.getQualityGrade())) {
            case "premium" -> BigDecimal.valueOf(1.25);
            case "economy" -> BigDecimal.valueOf(0.85);
            default -> BigDecimal.ONE; // standard
        };

        // Slight bulk discount encouragement for large quantities (helps farmers price competitively)
        BigDecimal quantityFactor = request.getQuantity() != null && request.getQuantity() >= 100
                ? BigDecimal.valueOf(0.95)
                : BigDecimal.ONE;

        return base.multiply(qualityMultiplier).multiply(quantityFactor).setScale(2, RoundingMode.HALF_UP);
    }

    public String autoCategorize(String productName) {
        if (productName == null) return "other";
        String n = productName.toLowerCase();

        if (containsAny(n, "tomato", "potato", "onion", "carrot", "spinach", "cabbage", "brinjal", "okra", "cauliflower", "peas", "capsicum"))
            return "vegetables";
        if (containsAny(n, "mango", "banana", "apple", "grape", "papaya", "orange", "watermelon", "guava", "pomegranate"))
            return "fruits";
        if (containsAny(n, "rice", "wheat", "maize", "barley", "millet", "corn"))
            return "grains";
        if (containsAny(n, "milk", "curd", "paneer", "ghee", "butter", "cheese"))
            return "dairy";
        if (containsAny(n, "dal", "lentil", "chana", "moong", "rajma", "bean"))
            return "pulses";
        if (containsAny(n, "chili", "turmeric", "cumin", "coriander", "pepper", "cardamom", "cinnamon"))
            return "spices";
        if (containsAny(n, "rose", "marigold", "jasmine", "lily", "flower"))
            return "flowers";
        if (containsAny(n, "chicken", "egg", "duck"))
            return "poultry";

        return "other";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) return true;
        }
        return false;
    }

    private String safeGrade(String grade) {
        return grade == null ? "standard" : grade.toLowerCase().trim();
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
