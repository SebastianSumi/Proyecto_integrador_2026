# Sanitized API Errors Specification

## Purpose
Provide stable REST failures without leaking database or framework internals.

## Requirements

### Requirement: Consistent error envelope
API failures MUST use one documented envelope containing status, reason, message, request path, correlation id, and validation violations when applicable.

#### Scenario: Validation failure
- GIVEN an invalid `/api/v1` request
- WHEN validation rejects it
- THEN the response is a documented client error with field violations and no stack trace

#### Scenario: Missing or conflicting resource
- GIVEN a missing resource or business conflict
- WHEN the endpoint handles it
- THEN it returns the documented not-found or conflict status and stable message shape

### Requirement: Sanitized infrastructure failures
Oracle integrity, optimistic-lock, malformed JSON, and conversion failures MUST map to safe client responses while detailed causes remain server-side only.

#### Scenario: Database constraint failure
- GIVEN a persistence operation violates an Oracle constraint
- WHEN the request fails
- THEN the response is a correlated conflict envelope with no SQL, schema, credentials, or stack trace

#### Scenario: Optimistic-lock failure
- GIVEN two requests update the same versioned resource
- WHEN one update loses the race
- THEN it returns the documented conflict status and envelope without persistence details

#### Scenario: Malformed JSON or conversion failure
- GIVEN a request body cannot be parsed or a value cannot be converted to its declared type
- WHEN the endpoint rejects it
- THEN it returns HTTP 400 with the documented envelope and no parser, class, SQL, or stack-trace details
