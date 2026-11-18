package com.tidewater.freight.pricing.rules;

import com.tidewater.freight.domain.ServiceLevel;
import com.tidewater.freight.domain.ShipmentRequest;
import com.tidewater.freight.pricing.PricingRule;
import java.math.BigDecimal;

public class ExpressMultiplierRule implements PricingRule {

    private static final BigDecimal MULTIPLIER = new BigDecimal("1.5");

    @Override
    public BigDecimal apply(ShipmentRequest request, BigDecimal currentCost) {
        return request.serviceLevel() == ServiceLevel.EXPRESS
                ? currentCost.multiply(MULTIPLIER)
                : currentCost;
    }
}