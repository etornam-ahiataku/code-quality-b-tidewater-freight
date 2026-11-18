package com.tidewater.freight.pricing.rules;

import com.tidewater.freight.domain.ServiceLevel;
import com.tidewater.freight.domain.ShipmentRequest;
import com.tidewater.freight.domain.Zone;
import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ZoneHandlingFeeRuleTest {

    private final ZoneHandlingFeeRule rule = new ZoneHandlingFeeRule();

    @ParameterizedTest
    @CsvSource({
            "DOMESTIC, 100, 100",
            "REGIONAL, 100, 110",
            "INTERNATIONAL, 100, 125"
    })
    void appliesConfiguredZoneFee(Zone zone, BigDecimal currentCost, BigDecimal expected) {
        ShipmentRequest request =
                new ShipmentRequest(zone, new BigDecimal("10"), ServiceLevel.STANDARD);

        assertEquals(0, expected.compareTo(rule.apply(request, currentCost)));
    }
}
