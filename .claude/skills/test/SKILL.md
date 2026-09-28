---
name: test
description: Run and improve tests for a selected part of Thousand Online. Use $ARGUMENTS to specify the target, for example game, backend, room, or all.
---

# Test Skill

The requested test target is:

$ARGUMENTS

## Test process

1. Inspect the relevant project structure and existing tests.
2. Determine what should be tested for the requested target.
3. Check whether appropriate tests already exist.
4. Add or improve tests when necessary.
5. Run the relevant tests.
6. Report the test results.

## Project-specific rules

- Backend tests use JUnit 5 and AssertJ.
- Use Mockito where mocking is appropriate.
- Use Testcontainers for integration tests requiring real infrastructure.
- Prefer testing game/domain logic independently from Spring.
- Do not introduce unnecessary integration tests when a unit test is sufficient.
- Tests should verify behaviour, not implementation details.
- Keep tests focused and readable.

## Target examples

- `game` — focus on the Tysiąc game domain and game rules.
- `room` — focus on lobby/room behaviour.
- `backend` — inspect and test the backend.
- `all` — run the relevant backend test suite.

## Output

Return:

### Tests performed

Describe what was inspected and tested.

### Changes made

List any tests created or modified.

### Result

Report the commands executed and whether they passed or failed.

### Remaining issues

List important gaps or problems that still need attention.
