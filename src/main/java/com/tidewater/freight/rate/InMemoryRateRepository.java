package com.tidewater.freight.rate;

import com.tidewater.freight.domain.Zone;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class InMemoryRateRepository implements RateRepository {

    private final Map<Zone, BigDecimal> rates = new EnumMap<>(Zone.class);

    public InMemoryRateRepository() {
        this(Map.of(
                Zone.DOMESTIC, new BigDecimal("5.00"),
                Zone.REGIONAL, new BigDecimal("8.00"),
                Zone.INTERNATIONAL, new BigDecimal("15.00")));
    }

    InMemoryRateRepository(Map<Zone, BigDecimal> rates) {
        this.rates.putAll(Objects.requireNonNull(rates, "rates"));
    }

    @Override
    public BigDecimal findRate(Zone zone) {
        Objects.requireNonNull(zone, "zone");
        BigDecimal rate = rates.get(zone);
        if (rate == null) {
            throw new RateNotFoundException(zone);
        }
        return rate;
    }
}