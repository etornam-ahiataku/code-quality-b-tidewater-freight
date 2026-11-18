# Tidewater Freight Shipping Cost Demo

A Java 21 command-line demo that calculates shipping costs in GHS from a
shipment's zone, weight, and service level. The project uses an in-memory rate
repository and composable pricing rules; it does not expose a network API or
persist rates.

## Pricing behavior

The demo applies pricing in this order:

1. Base cost = zone rate per kg × weight in kg.
2. Add the zone handling fee.
3. Add the heavy-item surcharge if the weight is over 30 kg.
4. Multiply the running total by 1.5 for EXPRESS service.

The calculator applies the rule order supplied to it. The `Demo` class wires
the rules in the order above.

| Zone | Rate per kg (GHS) | Handling fee (GHS) |
|---|---:|---:|
| `DOMESTIC` | 5.00 | none |
| `REGIONAL` | 8.00 | 10 |
| `INTERNATIONAL` | 15.00 | 25 |

| Rule | Behavior |
|---|---|
| Heavy-item surcharge | Add 20 GHS when weight is strictly over 30 kg; exactly 30 kg is not surcharged. |
| `EXPRESS` service | Multiply the running total by 1.5. |
| `STANDARD` service | No service-level adjustment. |

`ShipmentRequest` requires a zone and service level and rejects null values and
weights that are zero or negative.

### Worked examples

| Shipment | Calculation | Cost |
|---|---|---:|
| Domestic, 10 kg, standard | 10 × 5.00 | 50.00 GHS |
| Regional, 31 kg, standard | 31 × 8.00 + 10 + 20 | 278.00 GHS |
| International, 40 kg, express | (40 × 15.00 + 25 + 20) × 1.5 | 967.500 GHS |

Costs are represented with `BigDecimal`. The demo prints the resulting decimal
scale as-is; it does not format all amounts to a fixed number of decimal places.

## Project structure

```text
src/
├── main/java/com/tidewater/freight/
│   ├── Demo.java
│   ├── domain/
│   │   ├── ServiceLevel.java
│   │   ├── ShipmentRequest.java
│   │   └── Zone.java
│   ├── pricing/
│   │   ├── PricingRule.java
│   │   ├── ShippingCalculator.java
│   │   └── rules/
│   │       ├── ExpressMultiplierRule.java
│   │       ├── HeavyItemSurchargeRule.java
│   │       └── ZoneHandlingFeeRule.java
│   └── rate/
│       ├── InMemoryRateRepository.java
│       ├── RateNotFoundException.java
│       └── RateRepository.java
└── test/java/com/tidewater/freight/
    ├── domain/
    │   └── ShipmentRequestTest.java
    ├── pricing/
    │   ├── ShippingCalculatorTest.java
    │   └── rules/
    │       ├── ExpressMultiplierRuleTest.java
    │       ├── HeavyItemSurchargeRuleTest.java
    │       └── ZoneHandlingFeeRuleTest.java
    └── rate/
        └── InMemoryRateRepositoryTest.java
```

`ShippingCalculator` receives a `RateRepository` and an ordered list of
`PricingRule` implementations through its constructor. The demo supplies the
in-memory repository and the three rules.

## Requirements

- JDK 21 or later
- Maven

Run commands from the project root.

## Run the demo

```bash
mvn compile
mvn exec:java "-Dexec.mainClass=com.tidewater.freight.Demo"
```

The demo prints domestic standard and international express examples. A rate
lookup for a zone without a configured rate throws `RateNotFoundException`.

## Run tests

```bash
mvn test
```

The tests use JUnit 5 and Mockito. They cover:

- Shipment request validation, including null zone/service level and
  non-positive weights.
- Configured repository rates, null zones, and the missing-rate exception.
- Zone fees, the heavy-item threshold at 29.99, 30, and 30.01 kg, and standard
  versus express behavior.
- Calculator results for the worked examples, rule ordering, and a null rate
  returned by a repository.

## Coverage and mutation testing

JaCoCo report:

```bash
mvn verify
```

Open `target/site/jacoco/index.html` after the command completes.

PIT mutation testing:

```bash
mvn test-compile org.pitest:pitest-maven:mutationCoverage
```

Open `target/pit-reports/index.html` after the command completes.

## Add a pricing rule

1. Add a class in `pricing/rules/` that implements `PricingRule`.
2. Add focused tests for the rule's behavior and boundary conditions.
3. Add the rule to the list in `Demo` in the desired calculation order.

## Contributions

For contribution guidance, contact the maintainer:
[etornam-ahiataku](https://github.com/etornam-ahiataku/code-quality-b-tidewater-freight)
([email](mailto:etornam-ahiataku@amalitech.com)).
