# Documentation

## Purpose

This directory contains all technical documentation for the Notification Engine project.

The documentation is organized by responsibility so engineers can quickly locate architectural decisions, system design, and supporting diagrams.

---

## Directory Structure

```
docs/
├── adr/
├── architecture/
├── diagrams/
```

### adr/

Architecture Decision Records (ADRs).

Each ADR documents an important engineering decision, including:

- Context
- Problem
- Decision
- Alternatives Considered
- Consequences

---

### architecture/

Contains documents describing the overall architecture of the system.

Examples include:

- Request Lifecycle
- Security Architecture
- Package Structure
- Notification Flow

---

### diagrams/

Contains all architecture diagrams used throughout the project.

Examples:

- Request Lifecycle
- Security Filter Chain
- Package Structure
- Entity Relationship Diagram

---

## Documentation Principles

This repository follows these documentation principles:

- Documentation evolves together with the code.
- Architecture decisions are recorded using ADRs.
- Diagrams are stored separately from explanatory documents.
- Documentation describes the system, not the learning process.

---

## Contributing

Whenever a significant architectural decision is made:

1. Update the relevant architecture document.
2. Create or update an ADR if required.
3. Keep diagrams synchronized with implementation.

Documentation is treated as part of the software rather than an afterthought.