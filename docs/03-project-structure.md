# 3. Project Structure

```
api-gateway/
├── build.gradle                     # Spring Boot 3.4.4, Spring Cloud 2024.0.0, Spotless, Lombok
├── settings.gradle
├── gradlew / gradlew.bat / gradle/  # Gradle 8.14.3 wrapper
├── docker-compose.keycloak.yml      # Keycloak 24 + Postgres (not yet integrated)
├── README.md                        # Original project overview
├── spring-gateway.svg               # Sequence diagram
├── gateway-flowchart.svg            # Flowchart
├── docs/                            # ← you are here
└── src/main/
    ├── resources/
    │   ├── application.yml          # all values from env vars; profile "local" by default
    │   ├── application-local.yml    # git-ignored, create your own
    │   ├── banner.txt
    │   └── logback-spring.xml       # JSON logs to console + rolling file (logs/application.log)
    └── java/com/example/apigateway/
        ├── ApiGatewayApplication.java
        ├── common/error/            # Error model + global reactive error handler
        ├── config/                  # Cache, dynamic route locator, refresher, rate-limit key resolvers, OpenAPI
        ├── controller/              # Admin REST API
        ├── dto/                     # Request / response objects, grouped per feature
        ├── enums/                   # Status enums, strategies
        ├── filters/                 # Global pre/post logging filters
        ├── mapper/                  # Entity → runtime config mapper
        ├── modles/                  # JPA entities (sic: "modles")
        ├── repositories/            # Spring Data JPA repositories
        ├── resolver/                # Resolves policy entities for a route map row
        └── services/                # Service interfaces, impl/, caches/
```

There is no `src/test` directory yet. See [Development Guide](07-development-guide.md#75-testing).

## 3.1 `config/`

| Class | Role |
|-------|------|
| `CacheConfig` | `@EnableCaching`. Builds a `RedisCacheManager` (JSON serializer with default typing, 5 min TTL) or a Caffeine `SimpleCacheManager` depending on `cache.type`. |
| `CacheNames` | Constant `GATEWAY_ROUTE = "gatewayRoute"`. Always reference this constant in cache annotations. |
| `DynamicRouteLocator` | `RouteDefinitionLocator` that converts cached `GatewayRouteConfigResponse` objects into SCG `RouteDefinition`s and registers Resilience4j configs per route. |
| `GatewayRouteRefresher` | On `@PostConstruct` writes all route definitions into SCG and publishes `RefreshRoutesEvent`. `refreshRoutes()` can be called again at any time. |
| `RateLimitConfig` | Two `KeyResolver` beans: `ipKeyResolver` (`@Primary`, remote IP) and `clientKeyResolver` (header `X-F`, fallback `anonymous`). Only `ipKeyResolver` is used today. |
| `CustomTimeoutGatewayFilterFactory` | A `CustomTimeout` gateway filter (per-route timeout). Defined but **not** currently added by `DynamicRouteLocator`; timeouts come from the Resilience4j time limiter instead. |
| `OpenApiConfig` | springdoc metadata: title, version, servers (`localhost:9090`, `api.example.com`). |

## 3.2 `controller/`

One controller per aggregate. All are plain `@RestController`s returning DTOs (mostly synchronous
even though the app is WebFlux).

| Controller | Base path |
|------------|-----------|
| `GatewayRouteController` | `/api/v1/gateway/route` |
| `KeyResolverController` | `/v1/key-resolvers` |
| `GatewayRateLimitPolicyController` | `/v1/rate-limit` |
| `CircuitBreakerPolicyController` | `/api/circuit-breaker-policy` |
| `RouteMapPolicyController` | `/api/route/mapping` |

Full endpoint list: [Admin API Reference](05-api-reference.md).

## 3.3 `dto/`

Sub-packages mirror the aggregates: `gatewayroute`, `gatewayroutemapdto`, `keyresolverpolicy`,
`ratelimitpolicy`, `circuitbreaker`.

Conventions used throughout:

* `Create*Request` / `Create*` – has a `toEntity()` method.
* `Update*Request` – has an `updateEntity(entity)` method that mutates a loaded entity.
* `*Response` – has a static `fromEntity(entity)` factory.
* `List*Request` / `GetAllRequest` – `page` (1-based) and `size`.
* `PagedResponse<T>` – `{ data: [...], pagination: { itemsPerPage, totalPages, totalItems, currentPage } }`.
* `GatewayRouteConfigResponse` – the **runtime** representation of a fully resolved route
  (route + rate limit + key resolver + circuit breaker). This is what gets cached and what
  `DynamicRouteLocator` consumes.

## 3.4 `modles/` (entities)

| Entity | Table | Notes |
|--------|-------|-------|
| `GatewayRoute` | `gateway_route` | `routeCode` unique; `timeOutMs` maps to column `time_out_ms`; defaults `timeOutMs=30000`, `authRequired=true`, `status=ACTIVE` |
| `GatewayKeyResolverPolicy` | `gateway_key_resolver_policy` | `strategy` / `fallbackStrategy` are `KeyResolverStrategy` enums stored as strings |
| `GatewayRateLimitPolicy` | `gateway_rate_limit_policy` | FK column `key_resolver_policy_id` (plain `Long`, no JPA relation) |
| `GatewayCircuityBreakerPolicy` | `gateway_circuit_breaker_policy` | `cbName` unique; no status column |
| `GatewayRoutePolicyMap` | `gateway_route_policy_map` | `routeId`, `rateLimitPolicyId`, `circuitBreakerPolicyId` as plain `Long`s, all `nullable=false` in JPA |

Relationships are **not** modelled with `@ManyToOne`; services look up related rows by id
(`RoutePolicyResolver`, bulk `findAllById` in `RoutePolicyMapServiceImpl`). Keep that style unless
you are prepared to refactor all of them together. One exception: `getAllRouteMaps` builds a
`root.join("route")` Specification when filtering by `serviceName`, which cannot work without a
JPA relation; see [Known Gaps](08-troubleshooting.md#82-known-gaps-in-the-current-code).

## 3.5 `repositories/`

Standard `JpaRepository`s. Notable custom methods:

* `GatewayRouteRepository.findByRouteCode`, `existsByRouteCodeAndIdNot`, `findByServiceNameContainingIgnoreCase`
* `GatewayKeyResolverPolicyRepository.existsByPolicyCode`
* `GatewayRoutePolicyMapRepository.findFirstByRouteIdAndStatus` and `JpaSpecificationExecutor`

## 3.6 `services/`

Interfaces in `services/`, implementations in `services/impl/`.

| Service | Responsibility |
|---------|----------------|
| `GatewayRouteService` | CRUD for routes. `updateRouteById` refreshes the cache and publishes `RefreshRoutesEvent`. |
| `KeyResolverPolicyService` | CRUD; create rejects duplicate `policyCode`. |
| `RateLimitPolicyService` | CRUD. |
| `CircuitBreakerPolicyService` | CRUD. |
| `RoutePolicyMapService` | Links route ↔ policies, builds `GatewayRouteConfigResponse`, and exposes `getAllRoutesFromDB()` / `getRouteConfigFromDB(routeCode)` used by the cache layer. |
| `caches/GatewayRouteStore` | Spring-Cache facade in front of `RoutePolicyMapService`. |

`RoutePolicyMapServiceImpl` and `GatewayRouteStore` depend on each other, which is why
`spring.main.allow-circular-references: true` is set and the store is injected `@Lazy` via a setter.

## 3.7 `resolver/` and `mapper/`

* `RoutePolicyResolver.resolve(map)` loads the rate-limit, key-resolver and circuit-breaker entities
  referenced by one map row and returns them as a record.
* `GatewayRouteConfigMapper.toResponse(route, rateLimit, keyResolver, cb)` builds the runtime DTO.
  `GatewayRouteConfigResponse.Filters.from(...)` duplicates the same logic; prefer the mapper.

## 3.8 `filters/`

Two `GlobalFilter`s: `PreLoggingFilter` (order -1, adds `X-Correlation-ID`) and `PostLoggingFilter`
(order 1, logs status + duration). They run for **every** request, including admin calls.

## 3.9 `common/error/`

* `GatewayErrorCode` (enum with `code`, `HttpStatus`, `defaultMessage`) – note there is a second,
  unused enum with the same name in `enums/`. Always import `com.example.apigateway.common.error.GatewayErrorCode`.
* `BusinessException` – throw this from services with a `GatewayErrorCode`.
* `GatewayErrorResponse` – JSON body.
* `GlobalErrorHandler` – maps exceptions to responses (see [Architecture](02-architecture.md#error-responses)).
