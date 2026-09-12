# Secure Oracle Runtime Specification

## Purpose
Run the application and Oracle provisioning without versioned credentials.

## Requirements

### Requirement: External credential configuration
The system MUST obtain Oracle URL, usernames, and passwords from operator-supplied environment or deployment configuration and MUST NOT require secrets committed to the repository.

#### Scenario: Provision and start with supplied values
- GIVEN all required Oracle values are supplied externally
- WHEN provisioning and the application start
- THEN both use those values and complete without exposing secret values in output

#### Scenario: Missing required value
- GIVEN a required Oracle value is absent
- WHEN provisioning or startup is attempted
- THEN the operation fails clearly before database mutation and reports only the missing setting name

### Requirement: Reproducible provisioning workflow
Provisioning commands MUST state prerequisites, execution order, and target schemas for Teams, Activities, and Activity enrollments.

#### Scenario: Fresh environment
- GIVEN an empty supported Oracle environment
- WHEN an operator follows the documented sequence
- THEN required schemas, grants, and tables are created successfully
