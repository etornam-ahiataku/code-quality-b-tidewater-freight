package com.tidewater.freight.pricing;

import com.tidewater.freight.domain.ShipmentRequest;
import com.tidewater.freight.rate.RateRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class ShippingCalculator {

    private final RateRepository repository;
    private final List<PricingRule> rules;

    public ShippingCalculator(RateRepository repository, List<PricingRule> rules) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.rules = List.copyOf(rules);
    }

    public BigDecimal calculateCost(ShipmentRequest request) {
        Objects.requireNonNull(request, "request");
        BigDecimal cost = repository.findRate(request.zone()).multiply(request.weightKg());
        for (PricingRule rule : rules) {
            cost = rule.apply(request, cost);
        }
        return cost;
    }
}