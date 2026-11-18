package com.tidewater.freight.pricing.rules;

import com.tidewater.freight.domain.ShipmentRequest;
import com.tidewater.freight.pricing.PricingRule;
import java.math.BigDecimal;

public class HeavyItemSurchargeRule implements PricingRule {

    private static final BigDecimal WEIGHT_LIMIT_KG = new BigDecimal("30");
    private static final BigDecimal SURCHARGE = new BigDecimal("20");

    @Override
    public BigDecimal apply(ShipmentRequest request, BigDecimal currentCost) {
        return request.weightKg().compareTo(WEIGHT_LIMIT_KG) > 0
                ? currentCost.add(SURCHARGE)
                : currentCost;
    }
}