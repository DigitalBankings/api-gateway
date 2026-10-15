# 6. Configuration

## 6.1 `application.yml` walkthrough

File: `src/main/resources/application.yml`. Every secret or environment-specific value is a
`${PLACEHOLDER}`; see [Getting Started](01-getting-started.md#14-environment-variables) for the list.

```yaml
server:
  port: ${SERVER_PORT}
  shutdown: graceful            # waits for in-flight requests on SIGTERM

spring:
  main:
    allow-circular-references: true   # RoutePolicyMapServiceImpl <-> GatewayRouteStore
  application:
    name: api-gateway
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}

  datasource:                   # MySQL via HikariCP
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      leak-detection-threshold: 60000
      auto-commit: false

  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: none            # schema is managed manually, see docs/04-database.md
    properties:
      hibernate.jdbc.time_zone: UTC

  data:
    redis:                      # Lettuce, pooled
      timeout: 5000ms
      lettuce.pool: { max-active: 50, max-idle: 20, min-idle: 5 }

  cache:
    type: redis                 # Spring Boot auto-config hint; the real choice is cache.type below
    redis:
      time-to-live: 600000ms    # overridden by CacheConfig (5 min)
```

### Profiles

`SPRING_PROFILES_ACTIVE` defaults to `local`, so Spring looks for `application-local.yml`.
That file is git-ignored on purpose (it once contained credentials, see commit history). Each
developer creates their own. For other environments add `application-<env>.yml` **without**
secrets and inject secrets through environment variables.

## 6.2 Cache backend

`config/CacheConfig.java` reads the custom property `cache.type` (default `redis`).

| `cache.type` | Backend | TTL | Notes |
|--------------|---------|-----|-------|
| `redis` | `RedisCacheManager` | 5 min | Values serialized as JSON with Jackson default typing (so `GatewayRouteConfigResponse` deserializes back to the right class, not `LinkedHashMap`). Null values are not cached. Shared across gateway instances. |
| `caffeine` | `SimpleCacheManager` with one `CaffeineCache` named `gatewayRoute` | 5 min, max 10 000 entries | In-process only. Any `@Cacheable` that names a cache other than `gatewayRoute` fails at runtime because `SimpleCacheManager` does not create caches on demand. |

Hazelcast is on the classpath (`build.gradle`) but there is no Hazelcast cache manager wired up.

Redis is still required for **rate limiting** even when `cache.type=caffeine`, because SCG's
`RequestRateLimiter` uses `ReactiveStringRedisTemplate`.

## 6.3 Rate-limit key resolvers

`config/RateLimitConfig.java` defines two `KeyResolver` beans:

| Bean | Key | Used by |
|------|-----|---------|
| `ipKeyResolver` (`@Primary`) | remote IP address | every dynamic route (`DynamicRouteLocator` hard-codes `#{@ipKeyResolver}`) |
| `clientKeyResolver` | header `X-F`, fallback `anonymous` | nothing yet |

Behind a load balancer the remote IP is the balancer's; switch to `X-Forwarded-For` handling before
production (SCG's `XForwardedRemoteAddressResolver` is the usual choice).

## 6.4 Logging

`src/main/resources/logback-spring.xml`:

* JSON output (logstash encoder) to **console** and to a **rolling file**
  `logs/application.log` (10 MB per file, 30 days, 300 MB cap).
* Log directory comes from `logging.file.path` (default `logs`); the folder is git-ignored.
* Loggers `com.example.apigateway` and `com.seyha.common.logger` are `INFO`.

To see SQL during development add to your `application-local.yml`:

```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.springframework.cloud.gateway: DEBUG
```

## 6.5 OpenAPI / Swagger

`config/OpenApiConfig.java` plus `springdoc-openapi-starter-webflux-ui` expose:

* `/swagger-ui.html`
* `/v3/api-docs`

Server URLs listed in the spec are `http://localhost:9090` and `https://api.example.com`; update
them when the real hosts are known. Only the admin controllers appear; proxied routes do not.

## 6.6 Metrics

`micrometer-registry-prometheus` is a dependency, but `spring-boot-starter-actuator` is not, so no
`/actuator/prometheus` endpoint is exposed. To enable metrics add the actuator starter and:

```yaml
management:
  endpoints.web.exposure.include: health,prometheus,gateway
  endpoint.gateway.enabled: true   # also gives /actuator/gateway/routes for debugging
```

## 6.7 Code formatting (Spotless)

`build.gradle` applies Spotless with google-java-format, unused-import removal, trailing-whitespace
trimming and final newline. `./gradlew build` does **not** run `spotlessCheck` automatically; run
`./gradlew spotlessApply` before committing (see [Development Guide](07-development-guide.md)).

## 6.8 Keycloak compose file

`docker-compose.keycloak.yml` is a standalone dev stack (Keycloak 24 + Postgres 15, admin/admin,
port 8080). It is not referenced by the application; it is the starting point for the planned
`authRequired` enforcement.
