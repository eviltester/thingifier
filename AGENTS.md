## Test Design Rules

When adding or changing tests, write tests around domain rules and meaningful data
combinations, not around code coverage.

A test method should cover one behavior or decision rule. Multiple assertions are
fine when they verify different outcomes of the same behavior, but do not combine
multiple independent conditions, scenarios, or rules into one test method.

Prefer splitting tests when:
- the test name needs "and" to describe what it verifies
- different inputs exercise different domain rules
- one failure would make it unclear which scenario broke
- a helper method hides a list of separate scenarios inside one test
- setup has to be repeated or reset inside a loop

Use parameterized/data-driven tests when:
- the same rule is being checked against multiple equivalent examples
- the setup and expected outcome shape are the same for each row
- the data table makes the domain combinations easier to see

Do not use one broad test plus a helper containing many assertions to cover a
scenario matrix. Either expose the matrix as a parameterized test or split the
scenarios into named tests.

For content negotiation tests, separate these rules unless a single test is
explicitly proving their interaction:
- exact media type selection
- wildcard fallback
- structured suffix matching
- q-value priority
- q=0 exclusion
- unsupported media type fallback
- specificity tie-breaking
- client order tie-breaking
- final HTTP status/content-type/body assertions

Before finishing, review every amended test class and split any test that combines
separable conditions.
