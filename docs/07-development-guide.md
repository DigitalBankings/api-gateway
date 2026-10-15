# 7. Development Guide

## 7.1 Daily workflow

```bash
git checkout -b feature/<short-description>
# ... code ...
./gradlew spotlessApply          # format (google-java-format)
./gradlew build                  # compile + tests
git add -A && git commit -m "updated: <what changed>"
git push -u origin feature/<short-description>
```

Open a pull request against `main`. Commit messages in this repo follow a loose
`<verb>: <description>` style (`updated: ...`, `modified: ...`, `ref: ...`); keep that convention.

## 7.2 Code style

* **google-java-format** via Spotless. Two-space indentation, 100-column lines. Never hand-format;
  run `spotlessApply`.
* **Lombok** everywhere: `@Data` / `@Getter @Setter` on DTOs and entities, `@RequiredArgsConstructor`
  for constructor injection, `@Slf4j` for logging, `@Builder` where object construction is verbose.
* Constructor injection only (fields are `private final`). The one setter injection
  (`RoutePolicyMapServiceImpl.setGatewayRouteStore`) exists solely to break a circular dependency.
* Services are interface + `impl/` class. Controllers depend on the interface.
* Throw `BusinessException(GatewayErrorCode, message)` from services for expected failures. Do not
  throw `IllegalArgumentException` for "not found" (two places still do; prefer `BusinessException`).
* Enum values are persisted as strings (`@Enumerated(EnumType.STRING)`).
* Package name `modles` is a typo for `models`; it is referenced in many imports, so leave it unless
  you rename it in a dedicated commit.

## 7.3 Adding a new admin aggregate (e.g. a "header-rewrite policy")

1. **Entity** in `modles/` with `@Entity @Table(name = "gateway_<name>")`, `@CreationTimestamp` /
   `@UpdateTimestamp` fields, and a `Status` enum column.
2. **DDL** added to `docs/04-database.md` and applied to your local DB.
3. **Repository** in `repositories/` extending `JpaRepository<Entity, Long>`.
4. **DTOs** in `dto/<name>/`: `Create*Request` with `toEntity()`, `Update*Request` with
   `updateEntity(entity)`, `*Response` with static `fromEntity(entity)`, `List*Request` with
   `page`/`size`.
5. **Service** interface in `services/` + implementation in `services/impl/` returning
   `PagedResponse<T>` for lists (copy the pagination block from an existing impl: `page - 1`,
   `Sort.by(DESC, "id")`, `pageResult.getNumber() + 1`).
6. **Controller** in `controller/` with `register`, `getOne/{id}`, `getAll`, `updateById` endpoints
   mirroring the existing ones.
7. If the policy affects routing: add a column to `gateway_route_policy_map`, extend
   `RoutePolicyResolver`, `GatewayRouteConfigMapper`, `GatewayRouteConfigResponse.Filters`, and finally
   emit the SCG filter in `DynamicRouteLocator.buildRouteDefinition`.
8. After any write that changes routing, call `gatewayRouteStore.refreshAllRoutes()` and publish
   `new RefreshRoutesEvent(this)` so the gateway reloads.

## 7.4 Adding a gateway filter

### Per-route filter (configured from the database)

1. Create a class in `config/` extending `AbstractGatewayFilterFactory<Config>` and annotate it
   `@Component("<Name>")`. `CustomTimeoutGatewayFilterFactory` is a complete example.
2. In `DynamicRouteLocator.buildRouteDefinition` add:

```java
FilterDefinition f = new FilterDefinition();
f.setName("<Name>");            // must equal the @Component name
f.addArg("timeoutMs", "5000");  // args bind to Config fields by name
filters.add(f);
```

3. Filter order within a route follows list order; SCG applies global filters around them.

### Global filter (every request)

Implement `GlobalFilter` and `Ordered` in `filters/`. Negative order runs before routing filters,
positive after. See `PreLoggingFilter` (`-1`) and `PostLoggingFilter` (`1`).

Rules for filters on the hot path:

* No blocking calls (JPA, `RestTemplate`, `Thread.sleep`). Use reactive clients or offload with
  `Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())`.
* Do not read the request body unless you really must; it is a one-shot stream.
* Prefer `exchange.getAttributes()` for passing data between filters (e.g. a trace id).

## 7.5 Testing

There are no tests yet. `spring-boot-starter-test` is already on the classpath. Suggested layout:

```
src/test/java/com/example/apigateway/
├── services/impl/*ServiceImplTest.java      # Mockito unit tests for service logic
├── config/DynamicRouteLocatorTest.java      # given a GatewayRouteConfigResponse, assert the RouteDefinition
└── ApiGatewayApplicationTests.java          # @SpringBootTest context load (needs MySQL + Redis or Testcontainers)
```

Add Testcontainers (`org.testcontainers:mysql`, `com.redis:testcontainers-redis`) when you write the
first integration test so CI does not depend on local services.

Manual testing: use the walkthrough in [Admin API Reference](05-api-reference.md#57-end-to-end-walkthrough)
or Swagger UI.

## 7.6 Debugging tips

* **Which routes are loaded?** Watch for `Loaded Route: <code> → <uri>` on startup and after each
  `RefreshRoutesEvent`. Adding actuator enables `/actuator/gateway/routes`.
* **Cache contents (Redis):** keys look like `gatewayRoute::ALL` and `gatewayRoute::<routeCode>`.
  `redis-cli DEL gatewayRoute::ALL` forces a DB reload on the next refresh.
* **Rate limiter state (Redis):** keys `request_rate_limiter.{<ip>}.tokens` / `.timestamp`.
* **Circuit breaker state:** not exposed yet; log `cbFactory` or add actuator + `resilience4j` metrics.
* **Correlation id:** `PreLoggingFilter` adds `X-Correlation-ID` to the upstream request. It is not
  yet put into the MDC or into the error response's `traceId`; a good first improvement.

## 7.7 Dependency notes

| Dependency | Why it is here |
|------------|----------------|
| `spring-cloud-starter-gateway` | the gateway itself (WebFlux + Netty) |
| `spring-boot-starter-data-redis-reactive` | SCG `RequestRateLimiter` |
| `spring-boot-starter-data-redis` + `spring-boot-starter-cache` | Spring Cache with `RedisCacheManager` |
| `spring-cloud-starter-circuitbreaker-reactor-resilience4j` | `CircuitBreaker` filter + `ReactiveResilience4JCircuitBreakerFactory` |
| `spring-boot-starter-data-jpa` + `mysql-connector-j` | route/policy persistence |
| `caffeine`, `hazelcast*` | alternative cache backends (only Caffeine is wired) |
| `springdoc-openapi-starter-webflux-ui` | Swagger UI |
| `logstash-logback-encoder` | JSON logs |
| `seyha-logger` (GitHub Packages) | internal logging library; needs credentials to download |
| `micrometer-registry-prometheus` | metrics registry (no actuator yet, so unused) |
| `jakarta.servlet-api` (compileOnly) | required at compile time by `seyha-logger`; there is no servlet container at runtime |

Spring Cloud version is pinned by the BOM `spring-cloud-dependencies:2024.0.0`, which matches Spring
Boot 3.4.x. Upgrade both together.

## 7.8 Before you open a PR

- [ ] `./gradlew spotlessApply` ran and the diff is clean
- [ ] `./gradlew build` passes
- [ ] DDL changes are reflected in `docs/04-database.md`
- [ ] New or changed endpoints are reflected in `docs/05-api-reference.md`
- [ ] Any write path that affects routing refreshes the cache and publishes `RefreshRoutesEvent`
- [ ] No secrets in `application*.yml` committed to git
