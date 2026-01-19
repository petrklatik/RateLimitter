# Rate Limiter

A Spring Boot implementation of a Rate Limiting service.

## Assignment

The original assignment is available at [docs/Zadani_Java_Developer.pdf](docs/Zadani_Java_Developer.pdf).

## Documentation

- [Part 1: Single-node Rate Limiter](docs/part1-answers.md) - API, Algorithm, Data Structure, Concurrency

## Build & Run

```bash
# Build
mvnw.cmd clean install

# Run
mvnw.cmd spring-boot:run
```

## Test Script

```bash
# Usage: ./test.sh <count> <client-id>
./test.sh 150 client1
```

Example output:
```
Sending 150 requests for client 'client1'...
    100 200
     50 429
```

## Technology Stack

- **Framework:** Spring Boot 4.0.1
- **Language:** Java 25
- **Build Tool:** Maven
