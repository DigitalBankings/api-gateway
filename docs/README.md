# API Gateway – Developer Documentation

Welcome. This folder is the onboarding guide for developers joining the **api-gateway** project.
Read the documents in order the first time; afterwards use them as reference.

| # | Document | What you will learn |
|---|----------|---------------------|
| 1 | [Getting Started](01-getting-started.md) | Prerequisites, environment variables, database setup, running the app locally |
| 2 | [Architecture](02-architecture.md) | How a request flows through the gateway, dynamic routing, caching and refresh |
| 3 | [Project Structure](03-project-structure.md) | Package-by-package walkthrough of the source code |
| 4 | [Database](04-database.md) | Tables, relationships and the SQL that matches the JPA entities |
| 5 | [Admin API Reference](05-api-reference.md) | Every management endpoint with request/response examples and an end-to-end walkthrough |
| 6 | [Configuration](06-configuration.md) | `application.yml`, cache backends, logging, OpenAPI |
| 7 | [Development Guide](07-development-guide.md) | Code style, how to add a route filter or policy, testing, commit conventions |
| 8 | [Troubleshooting & Known Gaps](08-troubleshooting.md) | Common startup errors and quirks in the current code you should know about |

## 30-second summary

* The gateway is a **Spring Cloud Gateway** (reactive, Netty) application on **Spring Boot 3.4.4 / Java 17**.
* Routes are **not** defined in YAML. They live in **MySQL** tables and are loaded at startup into
  Spring Cloud Gateway through a custom `RouteDefinitionLocator`.
* Each route can be linked to a **rate-limit policy** (Redis token bucket) and a
  **circuit-breaker policy** (Resilience4j) through a mapping table.
* The same application exposes a small **admin REST API** to create and update routes and policies at runtime.
* Route configuration is cached (Redis by default, Caffeine optional) and pushed to the gateway
  whenever a route is updated.

## Diagrams in the repository root

* `spring-gateway.svg` – sequence diagram of the request flow.
* `gateway-flowchart.svg` – flowchart of routing and policy resolution.

Open them in a browser or your IDE; they complement [Architecture](02-architecture.md).
