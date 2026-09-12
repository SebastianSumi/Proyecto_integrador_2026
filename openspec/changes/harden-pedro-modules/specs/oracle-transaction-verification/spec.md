# Oracle Transaction Verification Specification

## Purpose
Prove transactional behavior against a real Oracle database through an opt-in harness.

## Requirements

### Requirement: Real-Oracle transaction scenarios
The harness MUST verify enrollment rollback and MUST verify activity-place, enrollment, and Team uniqueness races using bounded barriers and timeouts.

#### Scenario: Enrollment batch rollback
- GIVEN a batch containing one invalid enrollment
- WHEN the transaction fails
- THEN no enrollment from that batch remains and the activity is unchanged

#### Scenario: Concurrent overlapping activity-place operations
- GIVEN concurrent transactions schedule overlapping activities at one place and time
- WHEN both transactions complete
- THEN exactly one commits, the other returns a conflict outcome, and no overlapping activity rows exist

#### Scenario: Concurrent equivalent enrollments
- GIVEN concurrent transactions enroll the same person in one activity
- WHEN both transactions complete
- THEN exactly one current membership exists, one request succeeds, and the other returns a conflict outcome

#### Scenario: Concurrent equivalent Team names
- GIVEN concurrent transactions submit equivalent names
- WHEN both transactions complete
- THEN at most one succeeds, the other receives a conflict outcome, and only one canonical row exists

### Requirement: Deterministic isolation and cleanup
The harness MUST be opt-in, identify its Oracle target, fail on timeout, and clean all test data in success and failure paths.

#### Scenario: Harness cleanup
- GIVEN an Oracle test run completes or times out
- WHEN cleanup executes
- THEN created rows are removed and the result records bounded evidence
