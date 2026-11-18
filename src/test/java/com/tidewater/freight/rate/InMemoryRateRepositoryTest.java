package com.tidewater.freight.rate;

import com.tidewater.freight.domain.Zone;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryRateRepositoryTest {

    private final InMemoryRateRepository repository = new InMemoryRateRepository();

    @ParameterizedTest
    @CsvSource({
            "DOMESTIC, 5.00",
            "REGIONAL, 8.00",
            "INTERNATIONAL, 15.00"
    })
    void returnsConfiguredRate(Zone zone, BigDecimal expected) {
        assertEquals(0, expected.compareTo(repository.findRate(zone)));
    }

    @Test
    void rejectsNullZone() {
        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> repository.findRate(null));

        assertEquals("zone", exception.getMessage());
    }
}
