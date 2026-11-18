# Tidewater Freight: Code Quality Analysis

**Evaluated against:** AmaliTech Coding Standards, SOLID principles, common code smells and test-quality practice.

## Summary

The project is small and easy to follow, but it is not yet production-quality.

- `ShippingCalculator.calculateCost(...)` has too many responsibilities and hides every failure by returning `0`. An unknown zone looks like a valid GHS 0 shipment.
- The calculator creates `InMemoryRateRepository` itself instead of receiving a `RateRepository`, so it cannot be tested in isolation.
- The tests mostly cover happy paths or mock the class under test, so coverage does not prove correct behavior.

This report lists only **Critical** and **High** findings.

## Findings Overview

| # | Finding | Severity | Location |
|---|---|---|---|
| 1 | Failures swallowed, valid-looking `0` returned | Critical | `ShippingCalculator.java` |
| 2 | Test mocks the class under test | Critical | `ShippingCalculatorTest.java` |
| 3 | Multiple responsibilities (SRP) | High | `ShippingCalculator.java` |
| 4 | Hard-wired repository (DIP) | High | `ShippingCalculator.java` |
| 5 | Zone and service rules not open for extension (OCP) | High | `ShippingCalculator.java` |
| 6 | Weak assertions (`cost > 0`) | High | `ShippingCalculatorTest.java` |
| 7 | No failure or boundary tests | High | Test suite (Standard §9) |

## Production Code

### 1. Failures are swallowed (Critical)

`calculateCost(...)` catches `Exception` and returns `0`. Unknown zones, invalid weights, null inputs and unexpected runtime errors all produce the same result, so a caller cannot tell a real GHS 0 cost from a failure. The demo prints a result for `MOON` instead of reporting an invalid zone.

**Fix:** Remove the catch, or narrow it to a specific expected failure and wrap it in a domain exception that keeps the cause. Validate weight and service level explicitly.

### 3. Multiple responsibilities (High)

The calculator currently:

1. obtains a repository,
2. looks up a rate,
3. applies zone fees,
4. applies a heavy-item surcharge,
5. applies a service-level multiplier,
6. writes to the console,
7. catches and translates all failures.

**Fix:** Keep calculation in a focused component. Move console output to `Demo`.

### 4. Hard-wired repository, DIP violation (High)

The calculator constructs a concrete `InMemoryRateRepository` in a field, even though a `RateRepository` interface exists. This blocks test doubles and makes swapping implementations require code changes.

**Fix:** Add a constructor `ShippingCalculator(RateRepository repository)` and remove direct construction of `InMemoryRateRepository`.

### 5. Rules not open for extension, OCP violation (High)

Zones (DOMESTIC, REGIONAL, INTERNATIONAL) and service levels (EXPRESS) are selected with `if`/`else if` chains over raw strings. Every new zone or service means editing `calculateCost(...)`.

**Fix:** Replace strings with enums, and use configuration or strategy objects for zone and service rules.

## Tests

### 2. Test mocks the class under test (Critical)

The express test mocks `ShippingCalculator`, stubs it to return `967.5`, then asserts that value. It verifies Mockito, not production code.

**Fix:** Use a real `ShippingCalculator` with a fake or stub `RateRepository` and assert the exact formula result.

### 6. Weak assertions (High)

`cost > 0` passes for any positive value, so wrong rates, fees or multipliers go undetected.

**Fix:** Assert exact values:

| Scenario | Expected |
|---|---|
| DOMESTIC, 10 kg, STANDARD | `50.0` |
| DOMESTIC, 31 kg, STANDARD | `175.0` (5 × 31 + 20) |
| INTERNATIONAL, 40 kg, EXPRESS | `967.5` |
| REGIONAL, 5 kg, STANDARD | `50.0` |

Use a delta, or a `BigDecimal`-based API if floating-point precision matters.

### 7. No failure or boundary coverage (High)

Missing cases:

- unknown zone
- negative and zero weight
- weight exactly at 30 kg and above 30 kg
- null and unknown service levels
- repository exceptions
- formula boundaries for every zone

Without these, the suite cannot reveal whether `catch (Exception)` is hiding a bug.

**Fix:** One behavior per test, asserting either a thrown exception or an exact result.

## SOLID Assessment

| Principle | Result | Evidence |
|---|---|---|
| SRP | Partially violated | Calculator owns lookup, calculation, output and exception policy |
| OCP | Violated | Behavior is encoded in `if` chains |
| DIP | Violated | Concrete repository created inside the calculator |

## Keep As Is

- Class and package naming follows Java conventions.
- Maven is set up for JDK 21, JUnit 5, JaCoCo and PIT.
- The repository interface is small and focused.
- No inheritance or LSP problems.
- Tests are independent and share no mutable state.

## Suggested Refactoring Order

1. Inject `RateRepository` through the constructor.
2. Remove the broad catch and `return 0`; use explicit exceptions or a domain result.
3. Remove console output from `calculateCost(...)`.
4. Introduce enums or configuration-backed rules for zones and service levels.
5. Replace weak and mock-based tests with exact-value tests using a repository fake.
6. Add unknown-zone, invalid-weight, boundary and repository-failure tests.
7. Run Maven tests, JaCoCo verification and PIT mutation analysis.