package com.tidewater.freight.rate;

import com.tidewater.freight.domain.Zone;

public class RateNotFoundException extends RuntimeException {
    public RateNotFoundException(Zone zone) {
        super("No rate configured for zone: " + zone);
    }
}