# Tidewater Freight — Code Quality and Professional Java Analysis

## Scope

This analysis evaluates the Java sources and tests against:

-  AmaliTech Coding Standards 
- SOLID principles
- common code smells and test-quality practice

## Summary

The project compiles conceptually and has a small, understandable structure, but it is not yet production-quality. The most important issue is that `ShippingCalculator.calculateCost(...)` combines multiple responsibilities and suppresses every exception by returning `0`. This makes unknown zones look like a legitimate GHS 0 shipment and prevents callers and tests from distinguishing success from failure.

The repository abstraction is a positive start, but the calculator does not receive it through constructor injection. It constructs `InMemoryRateRepository` itself, preventing isolated tests and making future implementations difficult to introduce.

The test suite also has a fundamental weakness: it mostly exercises happy paths or mocks the class under test. Coverage is therefore not evidence of correct behavior. The suite needs exact assertions, repository doubles, branch coverage, and failure assertions.

## Prioritized Findings

### 1. Failures are swallowed and return a valid-looking zero — Critical

- **Location:** `ShippingCalculator.java`


`calculateCost(...)` catches `Exception` and returns `0`. Unknown zones, invalid weight values, null inputs, implementation failures, and unexpected runtime failures are all collapsed into the same result.

This violates AmaliTech error handling because a caller cannot know whether GHS 0 is a real cost or a failure. The demo will print a result for `MOON`, rather than surfacing the invalid zone. Catching `Exception` is also too broad: it hides bugs and makes a failure diagnostic impossible.

**Recommended correction:** remove the catch unless a specific, expected failure is to be converted into a domain result. Let repository exceptions propagate or wrap them in a contextual domain exception that preserves the cause. Validate weight and service level explicitly.

### 2. `ShippingCalculator` has multiple responsibilities — High

- **Location:** `ShippingCalculator.java`

The calculator currently:

1. obtains a repository implementation;
2. looks up a rate;
3. applies zone fees;
4. applies a heavy-item surcharge;
5. applies a service-level multiplier;
6. logs a result;
7. catches and translates all failures.

This is too many reasons to change and does not satisfy SRP. The calculation and presentation concerns should be separated.

**Recommended correction:** separate cost calculation into a focused component and keep console output in `Demo`. The repository lookup can remain a single responsibility of the repository.

### 3. The code violates DIP — High

- **Location:** `ShippingCalculator.java`

The high-level calculator hard-wires a concrete `InMemoryRateRepository` in an instance field. The interface exists, but it is not used as an injected dependency.

Consequences:

- tests cannot supply a deterministic fake or stub;
- replacing the repository requires changing `ShippingCalculator`;
- the calculator is harder to reuse in production.

**Recommended correction:** add a constructor such as `ShippingCalculator(RateRepository repository)` and retain no direct construction of `InMemoryRateRepository`.

### 4. Zone and service rules are not open for extension — High

- **Location:** `ShippingCalculator.java`

Every new zone fee or service behavior requires editing `calculateCost(...)`. The class has hard-coded `if`/`else if` chains for DOMESTIC, REGIONAL, INTERNATIONAL, and an exact string comparison for EXPRESS.

This is an OCP violation and resembles a flag/selection smell: the same method decides behavior from values passed as strings.

**Recommended correction:** introduce small domain types or rule implementations. At minimum, replace free strings with enums and use configuration or strategies for service and zone rules.

### 5. Magic values reduce clarity and maintainability — Medium

- **Locations:** `ShippingCalculator.java`, `InMemoryRateRepository.java`
The values `10`, `25`, `30`, `20`, `1.5`, and the base rates are not named constants or configuration. Their meaning is not searchable and changing one rule requires editing implementation code.

**Recommended correction:** use named constants or a configuration/rule model. Zone names and service levels should be typed rather than repeated as raw strings.

### 6. Console output is a hidden side effect — Medium

- **Location:** `ShippingCalculator.java`

`calculateCost(...)` claims to calculate a cost but also writes to standard output. The standard separates calculation from presentation and requires methods to avoid hidden side effects.

**Recommended correction:** make `calculateCost(...)` return only the cost. Put output in `Demo` or a presenter/client responsibility.

### 7. Inputs are neither validated nor typed safely — Medium

- **Locations:** `ShippingCalculator.java`

There is no validation for:

- negative or infinite weight;
- null zone;
- null service level;
- unknown service levels.

Negative weight can reduce or reverse a shipment cost. Null zone/service level can produce misleading or exception-derived behavior. Unknown service levels are silently treated like STANDARD.

**Recommended correction:** reject invalid values with an explicit exception or result type, and represent zone and service level with enums or validated value objects.

### 8. Repository uses mutable state and a primitive lookup contract — Medium

- **Location:** `InMemoryRateRepository.java`
`HashMap` is mutable and can be changed by future methods even when no mutation is intended. The repository also uses `IllegalArgumentException` for an expected lookup miss and expresses the contract as `double`; that makes the missing case implicit.

**Recommended correction:** initialize an immutable map with `Map.of(...)`, and consider `OptionalDouble` or a domain exception in the repository contract. Keep the caller's handling policy explicit.

### 9. The `Demo` executable mixes display strings with business behavior — Low

- **Location:** `Demo.java`

The demo is an application entry point, so its console responsibility is appropriate. However, it constructs `new ShippingCalculator()` rather than an injected repository and will therefore keep exercising the concrete wiring that violates DIP.

**Recommended correction:** construct explicit dependencies and pass them into `Demo` or the calculator.

## Test Findings

### 10. The express test tests a Mockito stub, not implementation code — Critical

- **Location:** `ShippingCalculatorTest.java`

The test mocks `ShippingCalculator`, tells the mock to return `967.5`, and then asserts that the mock returns the supplied value. It proves Mockito behavior and no production behavior. This is a replacement test that only looks like a test.

**Recommended correction:** never mock the class under test. Create a real calculator with a fake or stub `RateRepository`, and assert the exact formula result.

### 11. The happy-path assertions do not prove the expected formula — High

- **Location:** `ShippingCalculatorTest.java`

`cost > 0` is too weak to detect an incorrect rate, fee, or multiplier. It would pass for any positive result.

**Recommended correction:** assert exact outputs:

- DOMESTIC, 10kg, STANDARD = `50.0`;
- DOMESTIC, 31kg, STANDARD = `175.0` (`5 × 31 + 20`);
- INTERNATIONAL, 40kg, EXPRESS = `967.5`;
- REGIONAL, 5kg, STANDARD = `50.0`.

Use a delta or a BigDecimal-based production API if floating-point precision becomes relevant.

### 12. `assertNotNull` is semantically wrong for a primitive — Medium

- **Location:** `ShippingCalculatorTest.java`

The method returns `double`, which cannot be `null`; JUnit can only make this appear meaningful through boxing. The assertion is therefore tautological for the type and does not test a cost formula.

**Recommended correction:** replace it with an exact assertion of `50.0` for a REGIONAL 5kg shipment or the result supplied by a test repository.

### 13. Test names are not behavior-specific — Low

- **Locations:** `ShippingCalculatorTest.java`

Names such as `calculatesACost` and `returnsACostForAShipment` describe vague outcomes. Clean tests should state one behavior clearly.

**Recommended correction:** use names such as `calculatesDomesticStandardCost`, `appliesExpressMultiplier`, and `rejectsUnknownZone`.

### 14. The test suite does not cover failure or boundary behavior — High

- **Standard:** §9

Missing cases include:

- unknown zone;
- negative weight;
- zero weight;
- a weight exactly at 30kg;
- a weight above 30kg;
- null and unknown service levels;
- repository exceptions;
- formula boundaries for every zone.

The suite will also not reveal whether the `catch (Exception)` is swallowing a bug.

**Recommended correction:** write one behavior per test and assert exceptions or result values. Add a test that verifies an unknown zone is propagated or converted according to the selected domain contract.

### 15. Static wildcard imports are present — Low

- **Location:** [ShippingCalculatorTest.java](/C:/Users/EtornamAhiataku/OneDrive%20-%20AmaliTech%20gGmbH/Desktop/Github_Bucket/code-quality-b-tidewater-freight/src/test/java/com/tidewater/freight/ShippingCalculatorTest.java:5)

`import static org.junit.jupiter.api.Assertions.*;` and `import static org.mockito.Mockito.*;` are wildcard imports. The standard requires explicit imports and separate ASCII-ordered static/non-static blocks.

**Recommended correction:** import only required assertion and Mockito members.

## SOLID Principle Violations Assessment

| Principle | Result | Evidence |
|---|---|---|
| SRP | Partially violated | `ShippingCalculator` owns lookup, calculation, presentation, and exception policy. |
| OCP | Violated | Zone and service behavior is coded in changing `if` chains. |
| DIP | Violated | `ShippingCalculator` creates the concrete `InMemoryRateRepository` instead of receiving an abstraction. |

## Additional Professional Java and Code-Smell Findings

1. **Broad `catch (Exception)` is a high-risk code smell:** it catches unchecked implementation defects as if they were domain failures.
2. **Raw strings for domain concepts:** zone and service level names are misspelling-prone and create no compile-time validation.
3. **Obvious commented-out implementation comments:** several comments describe the next line rather than non-obvious business logic (§10.1). They should be replaced by self-documenting names or only retained for genuine business context.
4. **No Javadoc contract for public API:** Javadoc is not mandatory for every public method in the rule text, but public API behavior such as exceptions and units should be explicit. For this small project, a documented contract or strong types is appropriate.
5. **Unused Mockito dependency in the production/test approach:** the express test demonstrates Mockito but mocks the wrong class. Mockito should be used to mock `RateRepository`, not `ShippingCalculator`.
6. **No test for repository interaction:** a test repository or mock should prove that the zone passed to `findRate(...)` is the zone used for lookup.
7. **Potential precision issue:** money is calculated with `double`. Professional production Java should use `BigDecimal` or integer minor units, especially if prices are persisted or billed. This project is a lab, but the standard's precision would favor a dedicated monetary type.

## Findings Without a Violation

The following areas should be preserved:

- classes and package names follow normal Java casing;
- Maven is configured for JDK 21, JUnit 5, JaCoCo, and PIT;
- the repository interface is small and segregated;
- the project has no broad inheritance problem or observable LSP violation;
- test methods are independent and use no shared mutable test state.

## Suggested Refactoring 

1. Add constructor injection for `RateRepository`.
2. Replace the broad catch and return-zero behavior with explicit exceptions or a domain result.
3. Remove console output from `calculateCost(...)`.
4. Introduce typed zone and service values or configuration-backed rules.
5. Replace the three weak tests with exact formula tests and repository-fake tests.
6. Add unknown-zone, invalid-weight, boundary, and repository-failure tests.
7. Name constants or configuration for rates, fees, surcharge threshold, and multiplier.
8. Make the in-memory repository immutable and add a clear lookup contract.
9. Run Maven tests, JaCoCo verification, and PIT mutation analysis once `mvn` is available.
