# 2. Architecture

## 2.1 Big picture

```
                 ┌──────────────────────────────────────────────────────────────┐
                 │                     api-gateway (Netty, port 9090)           │
                 │                                                              │
  Client ──────▶ │  PreLoggingFilter ─▶ Route match ─▶ RequestRateLimiter ─▶     │ ──▶ Upstream service
                 │   (adds X-Correlation-ID)  (Path+Method)   (Redis bucket)    │     (targetUri)
                 │                                   ─▶ CircuitBreaker+TimeLimiter│
                 │  PostLoggingFilter ◀────────────────────── response ◀────────│
                 │                                                              │
  Admin ───────▶ │  /api/v1/gateway/route/**, /v1/rate-limit/**, ...            │
  (REST)         │  Controllers ─▶ Services ─▶ JPA ─▶ MySQL                      │
                 │                     │                                        │
                 │                     ▼                                        │
                 │  GatewayRouteStore (Spring Cache: Redis or Caffeine)         │
                 │                     │                                        │
                 │                     ▼                                        │
                 │  DynamicRouteLocator ─▶ RouteDefinitionWriter ─▶ SCG routes  │
                 └──────────────────────────────────────────────────────────────┘
```

Two very different kinds of traffic hit the same process:

1. **Proxied traffic** – requests whose path and method match a registered route. Spring Cloud
   Gateway (SCG) forwards them to the upstream `targetUri`, applying rate limiting and circuit
   breaking.
2. **Admin traffic** – REST calls to the management controllers that create or update routes and
   policies in MySQL.

Keep that in mind when choosing route paths: a route whose `path` pattern overlaps an admin
endpoint (for example `/api/**`) will shadow the admin API.

## 2.2 Domain model

```
gateway_key_resolver_policy   (how to identify the caller: IP, HEADER, JWT_CLAIM, API_KEY)
          ▲
          │ key_resolver_policy_id
gateway_rate_limit_policy     (replenishRate, burstCapacity, windowSeconds)
          ▲
          │ rate_limit_policy_id
gateway_route_policy_map ─────┼─── circuit_breaker_policy_id ──▶ gateway_circuit_breaker_policy
          │
          │ route_id
          ▼
gateway_route                 (serviceName, routeCode, path, httpMethod, targetUri, timeOutMs, authRequired)
```

* A **route** alone does nothing. It becomes active in the gateway only once a row in
  `gateway_route_policy_map` links it (optionally) to a rate-limit policy and a circuit-breaker policy.
* Policies are reusable: many routes can share one rate-limit or circuit-breaker policy.
* `routeCode` is the public identifier of a route (e.g. `AUTH-SERVICE-ea63159f`). It is generated on
  create as `SERVICE_NAME` upper-cased plus 8 random hex characters and becomes the SCG route id and
  the Resilience4j circuit-breaker name.

## 2.3 Startup: loading routes from the database

```
ApplicationContext ready
   └─ GatewayRouteRefresher.@PostConstruct init()
        └─ refreshRoutes()
             ├─ DynamicRouteLocator.getRouteDefinitions()
             │     └─ GatewayRouteStore.getAllRoutes()            @Cacheable("gatewayRoute", key="ALL")
             │           └─ RoutePolicyMapServiceImpl.getAllRoutesFromDB()
             │                 └─ pages through gateway_route_policy_map (500 per page)
             │                    bulk-loads routes + policies, maps to GatewayRouteConfigResponse
             ├─ for each route: RouteDefinitionWriter.delete(id) then save(definition)
             └─ publish RefreshRoutesEvent  → SCG rebuilds its route table
```

`DynamicRouteLocator` (`config/DynamicRouteLocator.java`) turns each `GatewayRouteConfigResponse`
into an SCG `RouteDefinition`:

| Source field | SCG element |
|--------------|-------------|
| `routeId` (= routeCode) | `RouteDefinition.id` and circuit-breaker name |
| `uri` (= targetUri) | `RouteDefinition.uri` |
| `predicates.path` | `Path` predicate (`pattern`) |
| `predicates.method` | `Method` predicate (upper-cased) |
| `filters.rateLimit` | `RequestRateLimiter` filter with `redis-rate-limiter.replenishRate` / `burstCapacity`, key resolver `#{@ipKeyResolver}` |
| `filters.circuitBreaker` + `filters.timeoutMs` | `CircuitBreaker` filter; a Resilience4j `CircuitBreakerConfig` + `TimeLimiterConfig` is registered on the `ReactiveResilience4JCircuitBreakerFactory` under the routeCode |

Note that `DynamicRouteLocator` is itself a `RouteDefinitionLocator` bean, so SCG's composite
locator also reads it directly. The explicit `RouteDefinitionWriter` save in the refresher is what
makes routes appear in the in-memory repository and triggers the refresh event.

## 2.4 Request flow for proxied traffic

1. **`PreLoggingFilter`** (global, order `-1`) logs method, path and the `Authorization` header,
   then adds a random `X-Correlation-ID` header to the outgoing request.
2. **Route matching** – SCG evaluates `Path` and `Method` predicates in route order.
3. **`RequestRateLimiter`** – Redis token bucket keyed by the client IP (`ipKeyResolver` bean in
   `config/RateLimitConfig.java`). Exceeding the bucket returns HTTP 429 from SCG.
4. **`CircuitBreaker`** – Resilience4j breaker named after the routeCode. The attached time limiter
   uses the route's `timeOutMs` (default 30000 in the entity, falling back to 5 s if null). An open
   breaker throws `CallNotPermittedException`; a timeout throws `TimeoutException`.
5. **Proxy** to `targetUri`; the matched path is forwarded unchanged (no `StripPrefix` or `RewritePath`
   filters are added).
6. **`PostLoggingFilter`** (global, order `1`) logs status and duration after the response completes.

### Error responses

`common/error/GlobalErrorHandler.java` is an `ErrorWebExceptionHandler` with order `-2`, so it
replaces Spring Boot's default error page for both proxied and admin requests. It unwraps the root
cause and maps it to a `GatewayErrorCode`:

| Exception | errorCode | HTTP |
|-----------|-----------|------|
| `BusinessException` | code carried by the exception | from the code |
| `MethodArgumentNotValidException`, `ConstraintViolationException` | `EGW422` | 422 |
| `ResponseStatusException` (4xx) | `EGW400` | 400 |
| `TimeoutException` | `EGW504` | 504 |
| `CallNotPermittedException` (breaker open) | `EGW503CB` | 503 |
| anything else | `EGW500` | 500 |

Body shape (`GatewayErrorResponse`):

```json
{
  "errorCode": "E0001",
  "message": "GatewayRoute not found with route id: 42",
  "status": 404,
  "path": "/api/v1/gateway/route/getOne/42",
  "method": "GET",
  "traceId": "unknown",
  "timestamp": "2026-10-15T10:21:33.120"
}
```

`traceId` is read from the exchange attribute `traceId`, which nothing sets yet, hence `unknown`.

## 2.5 Caching and refresh

`services/caches/GatewayRouteStore.java` wraps route loading with Spring Cache annotations on the
cache named `gatewayRoute` (`CacheNames.GATEWAY_ROUTE`).

| Method | Annotation | Key | Effect |
|--------|------------|-----|--------|
| `getAllRoutes()` | `@Cacheable` | `ALL` | Returns the cached collection or loads from DB |
| `refreshAllRoutes()` | `@CachePut` | `ALL` | Always reloads from DB and overwrites the cache |
| `get(routeCode)` | `@Cacheable` | routeCode | Single route from DB |
| `put(route)` | `@CachePut` | routeCode | Stores one route config |
| `evict(routeCode)` / `evictAll()` | `@CacheEvict` | – | Removes entries |

Backend is chosen by `cache.type` (see [Configuration](06-configuration.md#62-cache-backend)):
Redis entries live 5 minutes; Caffeine entries live 5 minutes with a 10 000 entry cap.

### When does the gateway pick up changes?

| Action | Cache | SCG routes |
|--------|-------|------------|
| Create route map (`POST /api/route/mapping/register`) | `put(route)` + `refreshAllRoutes()` | **not** refreshed until something publishes `RefreshRoutesEvent` or the app restarts |
| Update route (`POST /api/v1/gateway/route/updateByRouteId`) | `refreshAllRoutes()` | `RefreshRoutesEvent` published → SCG re-reads `DynamicRouteLocator` → fresh routes |
| Create/update rate-limit, circuit-breaker or key-resolver policy | untouched | untouched (stale until the `ALL` entry expires **and** a refresh event fires, or restart) |
| Create route (`/register`) | untouched | untouched (a route without a map row is never loaded anyway) |

This is a known gap; see [Troubleshooting & Known Gaps](08-troubleshooting.md#82-known-gaps-in-the-current-code).
A simple fix is to call `gatewayRouteStore.refreshAllRoutes()` and publish `RefreshRoutesEvent` from
every policy service write path, or add an admin `POST /refresh` endpoint that calls
`GatewayRouteRefresher.refreshRoutes()`.

## 2.6 Runtime route JSON

`GatewayRouteConfigResponse` is the shape stored in the cache and returned by the route-map API:

```json
{
  "routeId": "AUTH-SERVICE-ea63159f",
  "serviceName": "auth-service",
  "uri": "http://localhost:8000",
  "predicates": { "path": "/api/oauth2/authorize", "method": "POST" },
  "filters": {
    "timeoutMs": 30000,
    "authRequired": false,
    "rateLimit": {
      "replenishRate": 5,
      "burstCapacity": 10,
      "windowSeconds": 1,
      "keyResolver": { "strategy": "HEADER", "headerName": "X-User-Id", "fallbackStrategy": "IP" }
    },
    "circuitBreaker": {
      "slidingWindowType": "COUNT",
      "windowSize": 20,
      "failureRateThreshold": 50,
      "slowCallRateThreshold": 50,
      "slowCallDurationMs": 2000,
      "openStateWaitMs": 30000,
      "halfOpenCalls": 5,
      "timeoutMs": 2000
    }
  }
}
```

## 2.7 Reactive vs blocking

The gateway runs on Netty / WebFlux, but persistence uses **blocking** Spring Data JPA. The admin
controllers call repositories directly on the event loop (one of them wraps the result in
`Mono.just`). This is acceptable for low-volume admin calls but must **not** be done inside gateway
filters on the hot path. If you add a DB lookup to a filter, wrap it in
`Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())`.
