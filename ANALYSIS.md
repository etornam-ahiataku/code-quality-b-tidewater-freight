# Tidewater Freight — Code Quality and Behavior Analysis

## Scope

This analysis reflects the current Java sources and tests, including the
constructor-injected pricing rules and `BigDecimal` cost calculations.

## Summary

The calculator depends on the `RateRepository` interface and an ordered list of
`PricingRule` implementations. It validates the request and repository rate,
calculates the base cost, applies each rule in order, and returns the result.
Errors are not converted into plausible-looking costs. The demo owns console
output.

Tests cover configured rates, missing rates, input validation, pricing-rule
boundaries, rule ordering, and worked calculator examples. Remaining robustness
considerations are to define how an application should present repository
failures and to keep test/documentation claims aligned with actual coverage.

## Observed Behavior

- `ShippingCalculator` rejects a null request and a null rate returned by the
  repository. A missing configured rate is reported by the repository as
  `RateNotFoundException`.
- `InMemoryRateRepository` rejects a null zone. The `Zone` enum contains only
  `DOMESTIC`, `INTERNATIONAL`, and `REGIONAL`; `MOON` is not a valid zone and the
  demo does not print a cost for it. A valid zone without a configured rate
  throws `RateNotFoundException`.
- `ShipmentRequest` requires a non-null zone and service level, and a positive
  `BigDecimal` weight. `BigDecimal` has no NaN or infinity values; its scale is
  valid and does not change the positivity check.
- `HeavyItemSurchargeRule` adds the surcharge only when weight is strictly
  greater than 30 kg. A shipment at exactly 30 kg is not surcharged.
- `Demo` constructs the calculator with an `InMemoryRateRepository` and the
  three pricing rules, then prints two valid worked examples.

## Design and Test Notes

- Constructor injection and the `RateRepository` abstraction keep the
  calculator independent of its in-memory implementation.
- Pricing rules are separate implementations and are applied in configured
  order. The calculator does not perform console output.
- Cost assertions compare `BigDecimal` values with `compareTo`, so they do not
  depend on scale.
- Repository tests cover all configured rates, null zones, and the missing-rate
  exception. Rule tests cover 29.99 kg, 30 kg, and 30.01 kg, including a
  non-round current cost below the threshold.
- `ShipmentRequestTest` covers zero and negative weights. Those values cannot
  reach a pricing rule through a valid `ShipmentRequest`.
- Calculator tests cover a null repository rate, ordering, and documented
  results using both mocked and real dependencies.

## Potential Follow-up

The library deliberately propagates repository failures; an application
boundary such as `Demo` or a service layer should decide whether to report,
translate, or recover from those failures. Avoid turning them into a zero cost,
which would be indistinguishable from a successful calculation.
