package com.tidewater.freight.pricing;

import com.tidewater.freight.domain.ServiceLevel;
import com.tidewater.freight.domain.ShipmentRequest;
import com.tidewater.freight.domain.Zone;
import com.tidewater.freight.pricing.rules.ExpressMultiplierRule;
import com.tidewater.freight.pricing.rules.HeavyItemSurchargeRule;
import com.tidewater.freight.pricing.rules.ZoneHandlingFeeRule;
import com.tidewater.freight.rate.InMemoryRateRepository;
import com.tidewater.freight.rate.RateRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShippingCalculatorTest {

    @Mock
    private RateRepository repository;

    @Mock
    private PricingRule firstRule;

    @Mock
    private PricingRule secondRule;

    @Test
    void calculatesCostUsingMockedRepositoryAndRules() {
        ShipmentRequest request = new ShipmentRequest(
                Zone.REGIONAL, new BigDecimal("31"), ServiceLevel.STANDARD);
        when(repository.findRate(Zone.REGIONAL)).thenReturn(new BigDecimal("8"));
        when(firstRule.apply(request, new BigDecimal("248"))).thenReturn(new BigDecimal("258"));
        when(secondRule.apply(request, new BigDecimal("258"))).thenReturn(new BigDecimal("278"));

        ShippingCalculator calculator =
                new ShippingCalculator(repository, List.of(firstRule, secondRule));

        assertEquals(new BigDecimal("278"), calculator.calculateCost(request));
    }

    @Test
    void appliesRulesInTheirConfiguredOrder() {
        ShipmentRequest request = new ShipmentRequest(
                Zone.DOMESTIC, new BigDecimal("10"), ServiceLevel.STANDARD);
        when(repository.findRate(Zone.DOMESTIC)).thenReturn(new BigDecimal("5"));
        when(firstRule.apply(request, new BigDecimal("50"))).thenReturn(new BigDecimal("60"));
        when(secondRule.apply(request, new BigDecimal("60"))).thenReturn(new BigDecimal("70"));

        ShippingCalculator calculator =
                new ShippingCalculator(repository, List.of(firstRule, secondRule));
        assertEquals(new BigDecimal("70"), calculator.calculateCost(request));
    }

    @Test
    void calculatesCostWithRealDependencies() {
        ShippingCalculator calculator = new ShippingCalculator(
                new InMemoryRateRepository(),
                List.of(
                        new ZoneHandlingFeeRule(),
                        new HeavyItemSurchargeRule(),
                        new ExpressMultiplierRule()));
        ShipmentRequest request = new ShipmentRequest(
                Zone.INTERNATIONAL, new BigDecimal("40"), ServiceLevel.EXPRESS);

        assertEquals(0, new BigDecimal("967.500").compareTo(calculator.calculateCost(request)));
    }

    @Test
    void rejectsNullRateFromRepository() {
        ShipmentRequest request = new ShipmentRequest(
                Zone.DOMESTIC, new BigDecimal("10"), ServiceLevel.STANDARD);
        when(repository.findRate(Zone.DOMESTIC)).thenReturn(null);

        ShippingCalculator calculator = new ShippingCalculator(repository, List.of());

        NullPointerException exception = assertThrows(
                NullPointerException.class, () -> calculator.calculateCost(request));

        assertEquals("repository rate", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "DOMESTIC, 10, STANDARD, 50",
            "REGIONAL, 31, STANDARD, 278",
            "INTERNATIONAL, 40, EXPRESS, 967.500"
    })
    void calculatesDocumentedWorkedExamples(
            Zone zone, BigDecimal weight, ServiceLevel serviceLevel, BigDecimal expected) {
        ShippingCalculator calculator = new ShippingCalculator(
                new InMemoryRateRepository(),
                List.of(
                        new ZoneHandlingFeeRule(),
                        new HeavyItemSurchargeRule(),
                        new ExpressMultiplierRule()));
        ShipmentRequest request = new ShipmentRequest(zone, weight, serviceLevel);

        assertEquals(0, expected.compareTo(calculator.calculateCost(request)));
    }
}
