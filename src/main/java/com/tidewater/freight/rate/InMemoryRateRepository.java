package com.tidewater.freight.rate;

import com.tidewater.freight.domain.Zone;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

public class InMemoryRateRepository implements RateRepository {

    private final Map<Zone, BigDecimal> rates = new EnumMap<>(Zone.class);

    public InMemoryRateRepository() {
        rates.put(Zone.DOMESTIC, new BigDecimal("5.00"));
        rates.put(Zone.REGIONAL, new BigDecimal("8.00"));
        rates.put(Zone.INTERNATIONAL, new BigDecimal("15.00"));
    }

    @Override
    public BigDecimal findRate(Zone zone) {
        BigDecimal rate = rates.get(zone);
        if (rate == null) {
            throw new RateNotFoundException(zone);
        }
        return rate;
    }
}