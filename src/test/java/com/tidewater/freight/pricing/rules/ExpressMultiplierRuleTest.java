package com.tidewater.freight.pricing.rules;

import com.tidewater.freight.domain.ServiceLevel;
import com.tidewater.freight.domain.ShipmentRequest;
import com.tidewater.freight.domain.Zone;
import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpressMultiplierRuleTest {

    private final ExpressMultiplierRule rule = new ExpressMultiplierRule();

    @ParameterizedTest
    @CsvSource({
            "STANDARD, 100, 100",
            "EXPRESS, 100, 150"
    })
    void appliesExpressMultiplierOnlyToExpressShipments(
            ServiceLevel serviceLevel, BigDecimal currentCost, BigDecimal expected) {
        ShipmentRequest request =
                new ShipmentRequest(Zone.DOMESTIC, new BigDecimal("10"), serviceLevel);

        assertEquals(0, expected.compareTo(rule.apply(request, currentCost)));
    }
}
