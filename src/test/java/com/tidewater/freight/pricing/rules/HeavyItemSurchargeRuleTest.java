package com.tidewater.freight.pricing.rules;

import com.tidewater.freight.domain.ServiceLevel;
import com.tidewater.freight.domain.ShipmentRequest;
import com.tidewater.freight.domain.Zone;
import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeavyItemSurchargeRuleTest {

    private final HeavyItemSurchargeRule rule = new HeavyItemSurchargeRule();

    @ParameterizedTest
    @CsvSource({
            "29.99, 100, 100",
            "29.99, 37.25, 37.25",
            "30, 100, 100",
            "30.01, 100, 120"
    })
    void appliesSurchargeOnlyAboveThirtyKilograms(
            BigDecimal weight, BigDecimal currentCost, BigDecimal expected) {
        ShipmentRequest request =
                new ShipmentRequest(Zone.DOMESTIC, weight, ServiceLevel.STANDARD);

        assertEquals(0, expected.compareTo(rule.apply(request, currentCost)));
    }
}
