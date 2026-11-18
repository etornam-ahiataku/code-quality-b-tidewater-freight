package com.tidewater.freight;

import com.tidewater.freight.domain.*;
import com.tidewater.freight.pricing.ShippingCalculator;
import com.tidewater.freight.pricing.rules.*;
import com.tidewater.freight.rate.InMemoryRateRepository;
import java.math.BigDecimal;
import java.util.List;

public class Demo {
    public static void main(String[] args) {
        ShippingCalculator calculator = new ShippingCalculator(
                new InMemoryRateRepository(),
                List.of(new ZoneHandlingFeeRule(),
                        new HeavyItemSurchargeRule(),
                        new ExpressMultiplierRule()));

        System.out.println("DOMESTIC 10kg STANDARD     -> " + calculator.calculateCost(
                new ShipmentRequest(Zone.DOMESTIC, new BigDecimal("10"), ServiceLevel.STANDARD)));
        System.out.println("INTERNATIONAL 40kg EXPRESS -> " + calculator.calculateCost(
                new ShipmentRequest(Zone.INTERNATIONAL, new BigDecimal("40"), ServiceLevel.EXPRESS)));

         }
}