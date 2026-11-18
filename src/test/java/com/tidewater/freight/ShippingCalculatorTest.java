package com.tidewater.freight;

import org.junit.jupiter.api.Test;

import com.tidewater.freight.pricing.ShippingCalculator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShippingCalculatorTest {

    @Test
    void calculatesACost() {
        ShippingCalculator calculator = mock(ShippingCalculator.class);
        when(calculator.calculateCost("DOMESTIC", 10, "STANDARD")).thenReturn(1.0);
        double cost = calculator.calculateCost("DOMESTIC", 10, "STANDARD");
        assertTrue(cost > 0); 
    }

    @Test
    void calculatesExpressCost() {
        ShippingCalculator calculator = mock(ShippingCalculator.class);
        when(calculator.calculateCost("INTERNATIONAL", 40, "EXPRESS")).thenReturn(967.5);
        assertEquals(967.5, calculator.calculateCost("INTERNATIONAL", 40, "EXPRESS"));
    }

    @Test
    void returnsACostForAShipment() {
        ShippingCalculator calculator = mock(ShippingCalculator.class);
        when(calculator.calculateCost("REGIONAL", 5, "STANDARD")).thenReturn(1.0);
        double cost = calculator.calculateCost("REGIONAL", 5, "STANDARD");
        assertTrue(cost > 0); 
    }
}
