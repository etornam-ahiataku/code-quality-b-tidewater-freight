package com.tidewater.freight.domain;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShipmentRequestTest {

    @Test
    void acceptsValidShipment() {
        ShipmentRequest request = new ShipmentRequest(
                Zone.DOMESTIC, new BigDecimal("10"), ServiceLevel.STANDARD);

        assertEquals(new BigDecimal("10"), request.weightKg());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-0.01"})
    void rejectsNonPositiveWeight(String weight) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ShipmentRequest(
                        Zone.DOMESTIC,
                        new BigDecimal(weight),
                        ServiceLevel.STANDARD));

        assertEquals("weightKg must be greater than zero", exception.getMessage());
    }

    @Test
    void rejectsNullZone() {
        assertThrows(NullPointerException.class,
                () -> new ShipmentRequest(null, new BigDecimal("1"), ServiceLevel.STANDARD));
    }

    @Test
    void rejectsNullServiceLevel() {
        assertThrows(NullPointerException.class,
                () -> new ShipmentRequest(Zone.DOMESTIC, new BigDecimal("1"), null));
    }
}
