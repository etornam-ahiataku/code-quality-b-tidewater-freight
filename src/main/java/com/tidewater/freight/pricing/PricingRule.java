package com.tidewater.freight.pricing;

import com.tidewater.freight.domain.ShipmentRequest;
import java.math.BigDecimal;

public interface PricingRule {
    BigDecimal apply(ShipmentRequest request, BigDecimal currentCost);
}