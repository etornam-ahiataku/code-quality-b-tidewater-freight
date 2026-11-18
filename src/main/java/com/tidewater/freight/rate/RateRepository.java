package com.tidewater.freight.rate;

import com.tidewater.freight.domain.Zone;
import java.math.BigDecimal;

public interface RateRepository {
    BigDecimal findRate(Zone zone);
}