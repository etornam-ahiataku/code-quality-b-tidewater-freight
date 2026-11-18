package com.tidewater.freight.pricing.rules;

import com.tidewater.freight.domain.ShipmentRequest;
import com.tidewater.freight.domain.Zone;
import com.tidewater.freight.pricing.PricingRule;
import java.math.BigDecimal;
import java.util.Map;

public class ZoneHandlingFeeRule implements PricingRule {

    private static final Map<Zone, BigDecimal> FEES = Map.of(
            Zone.REGIONAL, new BigDecimal("10"),
            Zone.INTERNATIONAL, new BigDecimal("25"));

    @Override
    public BigDecimal apply(ShipmentRequest request, BigDecimal currentCost) {
        return currentCost.add(FEES.getOrDefault(request.zone(), BigDecimal.ZERO));
    }
}