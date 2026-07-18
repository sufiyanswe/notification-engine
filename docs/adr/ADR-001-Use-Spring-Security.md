# ADR-001: Use Spring Security

> **Status:** Accepted

---

# Context

The Notification Engine requires a centralized mechanism to enforce authentication and authorization across all HTTP endpoints.

As the application grows, implementing security logic inside individual controllers would lead to duplicated code, inconsistent security policies, higher maintenance costs, and an increased risk of exposing protected resources.

A single security layer is required to ensure that access control is applied consistently throughout the application.

---

# Decision

Adopt Spring Security as the application's security framework and define authorization rules using a custom `SecurityFilterChain`.

Security responsibilities are centralized within a dedicated configuration class (`SecurityConfig`) rather than being implemented inside controllers or business services.

Authorization policies are declared in one place and executed before requests reach the application's business logic.

---

# Alternatives Considered

## Controller-based authentication

Authentication and authorization logic implemented directly inside controllers.

Rejected because:

- duplicates security logic
- violates separation of concerns
- difficult to maintain
- inconsistent policy enforcement
- increases the risk of unsecured endpoints

---

## Custom Servlet Filters

Implement security manually using the Servlet API.

Rejected because:

- significantly increases implementation complexity
- requires maintaining authentication infrastructure manually
- duplicates functionality already provided by Spring Security
- harder to test and evolve

---

## Spring Security Filter Chain

Selected because:

- centralized security architecture
- industry standard
- extensible
- integrates naturally with Spring Boot
- separates infrastructure from business logic
- simplifies future migration to JWT, OAuth2, or other authentication mechanisms

---

# Consequences

## Benefits

- Single source of truth for security policies
- Consistent authorization across the application
- Reduced code duplication
- Easier maintenance
- Supports future authentication mechanisms
- Follows industry-standard architecture

## Trade-offs

- Additional framework complexity
- Initial learning curve
- Requires understanding Spring Security internals