# Saludablemente Contribution Guidelines

## Backend
- Use Java 21 and Spring Boot.
- Keep a modular monolith: each business module owns its entities, repositories, services, controllers, DTOs, and mappers.
- Cross-module access must use explicitly exposed public services; never access another module's repository directly.
- Expose REST contracts under `/api/v1` using input and output DTOs. Do not serialize JPA entities directly.
- Use `@EntityGraph` only for proven fetch requirements that prevent N+1 queries; do not add eager relationships by default.

## Quality
- Validate request DTOs and enforce business rules in services.
- Keep transactions at application-service boundaries and test successful and rollback behavior for transactional operations.
- Add focused tests with each behavior change and keep Spring Modulith tests green.
- Configure CORS and secrets through properties or environment variables, never hard-code credentials.

## Git
- Use Conventional Commits.
- Keep commits as cohesive, verified work units with related tests.
- Do not add AI attribution or Co-Authored-By trailers.