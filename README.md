# Rate Limiter

A Spring Boot implementation of a Rate Limiting service.

## Assignment Document

The original assignment is available at [docs/Zadani_Java_Developer.pdf](docs/Zadani_Java_Developer.pdf).

## Architecture Decision Records

All design decisions are documented in Architecture Decision Records (ADRs): [docs/adr](docs/adr)

## Build & Run

### Prerequisites
- Java 25
- Maven (or use included Maven Wrapper mvnw/mvnw.cmd)

### Commands

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

## Git Branches

The implementation follows a feature branch workflow:

| Branch | Content |
|--------|---------|
| `main` | Base structure and documentation |
| `feature/part1-single-node` | API, Algorithm, Data Structure, Concurrency |
| `feature/part2-distributed` | Distributed System considerations |
| `feature/part3-production` | Configuration, Testing |
| `feature/cyclic-dependencies` | Bonus: Cyclic dependency resolution |

## Technology Stack

- **Framework:** Spring Boot 4.0.1
- **Language:** Java 25
- **Build Tool:** Maven
