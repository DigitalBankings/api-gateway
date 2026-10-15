# 5. Admin API Reference

All endpoints are served by the gateway itself on `SERVER_PORT` (examples assume `9090`).
Swagger UI at `http://localhost:9090/swagger-ui.html` is generated from the same controllers.

Conventions:

* Request and response bodies are JSON. Most list endpoints are `POST` with a body (not `GET` with
  query parameters) so that filters can grow without URL changes.
* `page` is **1-based**. `size` is the page size.
* Paged responses use `PagedResponse<T>`:

```json
{
  "data": [ ... ],
  "pagination": { "itemsPerPage": 10, "totalPages": 3, "totalItems": 25, "currentPage": 1 }
}
```

* Errors are returned as `GatewayErrorResponse` (see [Architecture](02-architecture.md#error-responses)).
* There is **no authentication** on the admin API today. Do not expose it publicly.

Paths in the original root README (`/api/v1/gateway/rate-limit`, `/api/v1/gateway/routes`, …) are
out of date; the tables below reflect the controllers in the code.

---

## 5.1 Routes – `GatewayRouteController`

Base path: `/api/v1/gateway/route`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/gateway/route/register` | Create a route |
| POST | `/api/v1/gateway/route/getAll` | List routes (paged, newest first) |
| GET | `/api/v1/gateway/route/getOne/{routeId}` | Get a route by numeric id |
| POST | `/api/v1/gateway/route/updateByRouteId` | Update a route; refreshes cache and SCG routes |

### Create

```http
POST /api/v1/gateway/route/register
Content-Type: application/json

{
  "serviceName": "user-service",
  "path": "/api/users/**",
  "httpMethod": "GET",
  "targetUri": "http://localhost:8081",
  "authRequired": false
}
```

`routeCode` in the request is ignored; it is generated as `USER-SERVICE-<8 hex>`.
`timeoutMs` cannot be set on create (entity default 30000); use update.

Response (`GatewayRouteResponse`):

```json
{
  "id": 1,
  "serviceName": "user-service",
  "routeCode": "USER-SERVICE-3f9a1c2b",
  "paths": "/api/users/**",
  "httpMethod": "GET",
  "targetUri": "http://localhost:8081",
  "authRequired": false,
  "timeoutMs": 30000,
  "status": "ACTIVE"
}
```

Note the response field is `paths` (plural) while the request field is `path`.

### List

```http
POST /api/v1/gateway/route/getAll
{ "page": 1, "size": 10 }
```

### Update

```http
POST /api/v1/gateway/route/updateByRouteId
{
  "routeId": 1,
  "serviceName": "user-service",
  "path": "/api/users/**",
  "httpMethod": "GET",
  "targetUri": "http://user-service:8081",
  "timeoutMs": 10000,
  "authRequired": false
}
```

Behaviour to know:

* `serviceName`, `path`, `httpMethod`, `targetUri` are only applied when non-null.
* `timeoutMs` and `authRequired` are primitives and are **always** written. Omitting them sets
  `timeoutMs=0` and `authRequired=false`. Always send them.
* `routeCode` cannot be changed (the setter re-assigns the existing value).
* After saving, the service calls `GatewayRouteStore.refreshAllRoutes()` and publishes
  `RefreshRoutesEvent`, so the gateway picks up the change immediately.

---

## 5.2 Key-resolver policies – `KeyResolverController`

Base path: `/v1/key-resolvers`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/key-resolvers/register` | Create |
| GET | `/v1/key-resolvers/getOne/{id}` | Get by id |
| POST | `/v1/key-resolvers/getAll` | List (paged) |
| POST | `/v1/key-resolvers/updateById` | Update |

### Create

```http
POST /v1/key-resolvers/register
{
  "policyCode": "user-header",
  "strategy": "HEADER",
  "headerName": "X-User-Id",
  "fallbackStrategy": "IP",
  "status": "ACTIVE"
}
```

`strategy` ∈ `IP | HEADER | JWT_CLAIM | API_KEY`. The stored `policyCode` is generated as
`PHL-<8 hex>`; the request value is only used for the duplicate check.

Response (`KeyResolverPolicyResponse`):

```json
{
  "id": 1,
  "policyCode": "PHL-9b2e71aa",
  "strategy": "HEADER",
  "headerName": "X-User-Id",
  "fallbackStrategy": "IP",
  "status": "ACTIVE",
  "createdAt": "2026-10-15T10:00:00",
  "updatedAt": "2026-10-15T10:00:00"
}
```

### Update

```http
POST /v1/key-resolvers/updateById
{ "id": 1, "strategy": "HEADER", "headerName": "X-Client-Id", "fallbackStrategy": "IP", "status": "ACTIVE" }
```

Fields are applied when the **existing entity's** field is non-null (see
[Known Gaps](08-troubleshooting.md#82-known-gaps-in-the-current-code)); in practice send every field.

> Important: at runtime SCG always uses the `ipKeyResolver` bean regardless of the strategy stored
> here. The key-resolver policy is stored and returned in the route config JSON but not yet applied.

---

## 5.3 Rate-limit policies – `GatewayRateLimitPolicyController`

Base path: `/v1/rate-limit`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/rate-limit/register` | Create |
| GET | `/v1/rate-limit/getOne/{id}` | Get by id |
| POST | `/v1/rate-limit/getAll` | List (paged) |
| POST | `/v1/rate-limit/updateById` | Update (all fields overwritten) |

### Create

```http
POST /v1/rate-limit/register
{
  "replenishRate": 5,
  "burstCapacity": 10,
  "windowSeconds": 1,
  "keyResolverPolicyId": 1
}
```

Semantics (Spring Cloud Gateway `RedisRateLimiter`):

* `replenishRate` – tokens added to the bucket per second (steady-state requests/second).
* `burstCapacity` – bucket size (max requests in one burst). Must be ≥ `replenishRate`.
* `windowSeconds` – stored for future use; SCG's rate limiter is per-second.

Response (`RateLimitPolicyResponse`) echoes the fields plus `id`, `status`, `createdAt`, `updatedAt`.

### Update

```http
POST /v1/rate-limit/updateById
{ "id": 1, "replenishRate": 20, "burstCapacity": 40, "windowSeconds": 1, "keyResolverPolicyId": 1, "status": "ACTIVE" }
```

Every field is overwritten, including nulls. Send the full object.

---

## 5.4 Circuit-breaker policies – `CircuitBreakerPolicyController`

Base path: `/api/circuit-breaker-policy`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/circuit-breaker-policy/create` | Create |
| POST | `/api/circuit-breaker-policy/getAll` | List (paged) |
| GET | `/api/circuit-breaker-policy/getOne/{id}` | Get by id |
| POST | `/api/circuit-breaker-policy/updateById` | Update (all fields overwritten) |

### Create

```http
POST /api/circuit-breaker-policy/create
{
  "cbName": "default-cb",
  "slidingWindowType": "COUNT",
  "slidingWindowSize": 20,
  "failureRateThreshold": 50,
  "slowCallRateThreshold": 50,
  "slowCallDurationMs": 2000,
  "openStateWaitMs": 30000,
  "halfOpenCalls": 5,
  "timeoutMs": 2000
}
```

Mapping to Resilience4j in `DynamicRouteLocator`:

| Field | Resilience4j setting | Applied? |
|-------|----------------------|----------|
| `failureRateThreshold` | `failureRateThreshold` | yes |
| `slowCallRateThreshold` | `slowCallRateThreshold` | yes |
| `slowCallDurationMs` | `slowCallDurationThreshold` | yes |
| `openStateWaitMs` | `waitDurationInOpenState` | yes |
| `halfOpenCalls` | `permittedNumberOfCallsInHalfOpenState` | yes |
| `slidingWindowType`, `slidingWindowSize` | `slidingWindowType/Size` | **no** (stored only) |
| `timeoutMs` | `TimeLimiterConfig.timeoutDuration` | **no** – the route's `timeOutMs` is used |

Response (`ResponseCircuitBreaker`) echoes the fields plus `id`, `createdAt`, `updatedAt`.

---

## 5.5 Route ↔ policy mapping – `RouteMapPolicyController`

Base path: `/api/route/mapping`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/route/mapping/register` | Link a route to a rate-limit policy and a circuit-breaker policy |
| POST | `/api/route/mapping/getAll` | List resolved route configs (paged, optional filters) |

### Register

```http
POST /api/route/mapping/register
{
  "routeId": 1,
  "rateLimitPolicyId": 1,
  "circuitBreakerPolicyId": 1
}
```

Both policy ids are required by the JPA mapping (`nullable = false`). The response is the resolved
`GatewayRouteConfigResponse` (see [Architecture](02-architecture.md#26-runtime-route-json)), which is
also written into the cache.

The SCG route table is **not** refreshed by this call. Either call the route update endpoint for
the same route afterwards or restart the gateway (see Known Gaps).

### List

```http
POST /api/route/mapping/getAll
{ "page": 1, "size": 10, "serviceName": "user", "status": "ACTIVE" }
```

Response (`GetRouteMapResponse`):

```json
{
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 1,
  "items": [ { "routeId": "USER-SERVICE-3f9a1c2b", "serviceName": "user-service", ... } ]
}
```

`serviceName` filtering uses a JPA join that the entity model does not support today; omit it until
that is fixed (see Known Gaps). `status` filtering works.

---

## 5.6 Proxied routes

Anything that matches a loaded route's `path` + `httpMethod` is forwarded to `targetUri` with the
same path. There is no prefix stripping. Example: a route with `path=/api/users/**` and
`targetUri=http://localhost:8081` turns `GET /api/users/42` into `GET http://localhost:8081/api/users/42`.

Responses you may receive from the gateway rather than the upstream:

| HTTP | Source | Meaning |
|------|--------|---------|
| 429 | SCG `RequestRateLimiter` | Bucket for this client IP is empty |
| 503 `EGW503CB` | `GlobalErrorHandler` | Circuit breaker open |
| 504 `EGW504` | `GlobalErrorHandler` | Upstream slower than the route timeout |
| 404 | SCG | No route matched |

---

## 5.7 End-to-end walkthrough

Run these in order against a fresh database. The example proxies to httpbin so no upstream service
is needed.

```bash
BASE=http://localhost:9090

# 1. key resolver
curl -s -X POST $BASE/v1/key-resolvers/register -H 'Content-Type: application/json' \
  -d '{"policyCode":"ip","strategy":"IP","status":"ACTIVE"}'
# → note "id": 1

# 2. rate limit (5 req/s, burst 10)
curl -s -X POST $BASE/v1/rate-limit/register -H 'Content-Type: application/json' \
  -d '{"replenishRate":5,"burstCapacity":10,"windowSeconds":1,"keyResolverPolicyId":1}'
# → "id": 1

# 3. circuit breaker
curl -s -X POST $BASE/api/circuit-breaker-policy/create -H 'Content-Type: application/json' \
  -d '{"cbName":"default-cb","slidingWindowType":"COUNT","slidingWindowSize":20,"failureRateThreshold":50,"slowCallRateThreshold":50,"slowCallDurationMs":2000,"openStateWaitMs":30000,"halfOpenCalls":5,"timeoutMs":2000}'
# → "id": 1

# 4. route
curl -s -X POST $BASE/api/v1/gateway/route/register -H 'Content-Type: application/json' \
  -d '{"serviceName":"httpbin","path":"/anything/**","httpMethod":"GET","targetUri":"https://httpbin.org","authRequired":false}'
# → "id": 1, "routeCode": "HTTPBIN-xxxxxxxx"

# 5. link route ↔ policies
curl -s -X POST $BASE/api/route/mapping/register -H 'Content-Type: application/json' \
  -d '{"routeId":1,"rateLimitPolicyId":1,"circuitBreakerPolicyId":1}'

# 6. push the new route into SCG (an update publishes RefreshRoutesEvent)
curl -s -X POST $BASE/api/v1/gateway/route/updateByRouteId -H 'Content-Type: application/json' \
  -d '{"routeId":1,"timeoutMs":30000,"authRequired":false}'

# 7. call through the gateway
curl -i $BASE/anything/hello
# → 200 from httpbin, with X-Correlation-ID visible in the echoed headers

# 8. trip the rate limiter
for i in $(seq 1 15); do curl -s -o /dev/null -w "%{http_code}\n" $BASE/anything/hello; done
# → first ~10 return 200, then 429
```

Step 6 is only needed because the mapping endpoint does not yet refresh SCG; restarting the app
achieves the same.
