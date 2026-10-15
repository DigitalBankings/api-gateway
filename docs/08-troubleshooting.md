# 8. Troubleshooting & Known Gaps

## 8.1 Startup and build problems

| Symptom | Cause | Fix |
|---------|-------|-----|
| `Could not resolve com.github.OEURNSEYHA:seyha-logger:1.1.16` | No GitHub Packages credentials | Set `gpr.user` / `gpr.key` in `~/.gradle/gradle.properties` or export `GITHUB_ACTOR` / `GITHUB_TOKEN` ([Getting Started 1.2](01-getting-started.md#12-github-packages-credentials-required-to-build)) |
| `Toolchain ... Java 17 not found` | Gradle toolchain cannot locate JDK 17 | Install JDK 17 or enable auto-provisioning with the `foojay-resolver` plugin |
| `Could not resolve placeholder 'SERVER_PORT'` (or any `SPRING_DATASOURCE_*`, `REDIS_*`) | Missing env var and no `application-local.yml` | Create the local profile file or export the variables ([1.4](01-getting-started.md#14-environment-variables)) |
| `Table 'api_gateway.gateway_route' doesn't exist` | `ddl-auto: none`, schema not created | Run the DDL in [Database 4.3](04-database.md#43-ddl-matching-the-entities) |
| `Unknown column 'gr1_0.time_out_ms'` | Table created from the old README SQL (`timeout_ms`) | Rename the column to `time_out_ms` or recreate the table |
| `RedisConnectionFactory required` | `cache.type=redis` but Redis auto-config failed | Check `REDIS_HOST` / `REDIS_PASSWORD`, or set `cache.type=caffeine` for cache only (Redis is still needed for rate limiting) |
| `Cannot find cache named 'GATEWAY_ROUTE'` | `cache.type=caffeine` and `GatewayRouteStore.refreshRoute` was called | That method uses a literal cache name that differs from `CacheNames.GATEWAY_ROUTE`; change it to the constant |
| `The dependencies of some of the beans in the application context form a cycle` | `allow-circular-references` removed | Keep the flag, or break the cycle between `RoutePolicyMapServiceImpl` and `GatewayRouteStore` |
| Gateway starts but no `Loaded Route:` lines | No rows in `gateway_route_policy_map` | Routes only load when mapped; create a mapping |
| `404` for a path you registered | Mapping created but SCG not refreshed, or method mismatch | Call the route update endpoint (publishes `RefreshRoutesEvent`) or restart; check `httpMethod` is upper-case and matches |
| `429` immediately on the first request | `burstCapacity` smaller than 1 or Redis Lua script failing | Ensure `burstCapacity >= replenishRate >= 1` and Redis is reachable |
| Admin endpoint returns a 404 after registering a route | Route `path` pattern overlaps the admin path (e.g. `/api/**`) | Use more specific route paths; admin paths are `/api/v1/gateway/**`, `/api/route/**`, `/api/circuit-breaker-policy/**`, `/v1/**` |

## 8.2 Known gaps in the current code

These are behaviours a new developer will trip over. They are documented here so nobody rediscovers
them; each is a good first task.

### Routing / policies

1. **Key-resolver policy is not applied.** `DynamicRouteLocator` always sets
   `key-resolver=#{@ipKeyResolver}`. The stored strategy (`HEADER`, `JWT_CLAIM`, …) is only echoed in
   the config JSON. Fix: build a `KeyResolver` per strategy (or a composite bean that reads the
   strategy from a route attribute) and reference it in the filter definition.
2. **`windowSeconds` is ignored.** SCG's Redis rate limiter is per second; the field is stored only.
3. **Circuit-breaker `slidingWindowType`, `slidingWindowSize` and `timeoutMs` are ignored.** The
   Resilience4j config does not set the sliding window, and the time limiter uses the route's
   `timeOutMs` instead of the policy's `timeoutMs`.
4. **`authRequired` is not enforced.** There is no security filter; Keycloak is only a compose file.
5. **`CustomTimeoutGatewayFilterFactory` is never used** by `DynamicRouteLocator`.
6. **No `StripPrefix` / `RewritePath`.** The full matched path is forwarded; upstreams must serve
   the same path the client used.

### Cache / refresh

7. **Policy writes do not refresh routes.** Creating or updating a rate-limit, circuit-breaker or
   key-resolver policy, and creating a route mapping, leaves the `ALL` cache entry and the SCG route
   table stale. Only `updateByRouteId` refreshes both. See [Architecture 2.5](02-architecture.md#25-caching-and-refresh).
8. **`GatewayRouteStore.refreshRoute` uses the literal cache name `"GATEWAY_ROUTE"`** instead of
   `CacheNames.GATEWAY_ROUTE` (`"gatewayRoute"`). With Redis this silently writes to a second cache;
   with Caffeine it throws.
9. **`traceId` is always `"unknown"`** in error responses; nothing sets the `traceId` exchange
   attribute. `PreLoggingFilter` generates an `X-Correlation-ID` that could be reused.

### Admin API

10. **`UpdateGatewayRouteRequest`**: `timeoutMs` and `authRequired` are primitives, so omitting them
    writes `0` / `false`. `routeCode` cannot be changed (the code re-assigns the existing value).
11. **`UpdateKeyResolverPolicyRequest.updateEntity`** checks the *entity's* fields for null rather
    than the request's, and re-assigns `policyCode` to itself. Any null in the request overwrites a
    non-null entity value.
12. **`CreateKeyResolverPolicyRequest`** ignores the supplied `policyCode` (generates `PHL-…`), yet the
    duplicate check runs against the supplied value.
13. **`CreateRateLimitPolicy.burstRate`** and **`CreateCircuitBreaker.halfOpenStateWaitMs`** /
    **`UpdateCircuitBreakerRequest.halfOpenStateWaitMs`** are accepted but never stored.
14. **`GetRouteMapRequest.serviceName` filter** builds `root.join("route")`, but
    `GatewayRoutePolicyMap` has no `route` association (it stores `routeId` as a `Long`). Supplying
    `serviceName` throws at query time. Either add a `@ManyToOne` or filter via a subquery on `routeId`.
15. **No validation is triggered.** DTOs carry `@NotBlank` / `@NotNull`, but controllers do not use
    `@Valid`, so the annotations are inert.
16. **Not-found errors are inconsistent.** Most services throw `BusinessException`, but
    `KeyResolverPolicyServiceImpl.getOne` throws `IllegalArgumentException` (→ 500), and
    `KeyResolverPolicyServiceImpl.updateById` uses `ROUTE_NOT_FOUND` for a key-resolver.
17. **Pagination off-by-one risk.** List endpoints compute `page - 1`; sending `page: 0` throws
    `IllegalArgumentException` from `PageRequest`. `getAllRouteMaps` guards with `Math.max`, the others
    do not.
18. **Admin API has no authentication.**

### Housekeeping

19. Two enums named `GatewayErrorCode` exist (`common/error` is the real one; `enums/` is unused).
20. `GatewayRouteConfigResponse.Filters.from(...)` duplicates `GatewayRouteConfigMapper`.
21. `GatewayRouteResponse.paths` vs request `path` naming mismatch.
22. Root `README.md` endpoint table and SQL are outdated; this `docs/` folder is the current source.
23. No tests, no CI workflow (a `.github/workflows` directory was deleted in an early commit), no
    Dockerfile for the gateway itself.

## 8.3 Where to look when something is wrong

| Question | Start here |
|----------|------------|
| Why was my request routed (or not)? | `config/DynamicRouteLocator.java`, startup logs, enable `org.springframework.cloud.gateway: DEBUG` |
| Why did I get this error JSON? | `common/error/GlobalErrorHandler.java` → `mapError` |
| Why is the gateway using old config? | `services/caches/GatewayRouteStore.java`, Redis key `gatewayRoute::ALL`, [2.5](02-architecture.md#25-caching-and-refresh) |
| Why is the circuit breaker opening? | Resilience4j config built in `DynamicRouteLocator`; thresholds come from the mapped policy |
| Why does the build fail? | `build.gradle` repositories block (GitHub Packages) and Java toolchain |
