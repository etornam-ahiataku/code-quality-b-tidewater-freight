package com.tidewater.freight.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record ShipmentRequest(Zone zone, BigDecimal weightKg, ServiceLevel serviceLevel) {
    public ShipmentRequest {
        Objects.requireNonNull(zone, "zone");
        Objects.requireNonNull(serviceLevel, "serviceLevel");
        if (weightKg == null || weightKg.signum() <= 0) {
            throw new IllegalArgumentException("weightKg must be greater than zero");
        }
    }
}