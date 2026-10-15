# 4. Database

Database name: `api_gateway` (MySQL 8). Hibernate runs with `ddl-auto: none`, so the schema is
managed by hand. There is no Flyway/Liquibase yet; when you change an entity, update this file and
share the migration SQL with the team.

## 4.1 Entity-relationship overview

```
gateway_key_resolver_policy 1 ──── * gateway_rate_limit_policy
                                           1
                                           │
                                           *
gateway_route 1 ──── * gateway_route_policy_map * ──── 1 gateway_circuit_breaker_policy
```

* One route may have several map rows; the gateway uses the first `ACTIVE` one
  (`findFirstByRouteIdAndStatus`) for single lookups and **all** rows for the bulk load, keeping the
  first per route (`putIfAbsent`).
* Policies are shared; deleting a policy that is still referenced is blocked by the FK.

## 4.2 Column reference

### `gateway_route`

| Column | Type | Entity field | Notes |
|--------|------|--------------|-------|
| `id` | BIGINT PK AI | `id` | |
| `service_name` | VARCHAR(100) NOT NULL | `serviceName` | used to build `route_code` |
| `route_code` | VARCHAR(100) NOT NULL UNIQUE | `routeCode` | generated: `SERVICE-NAME-xxxxxxxx` |
| `path` | VARCHAR(255) NOT NULL | `path` | SCG `Path` predicate pattern, e.g. `/api/users/**` |
| `target_uri` | VARCHAR(255) NOT NULL | `targetUri` | e.g. `http://user-service:8080` |
| `http_method` | VARCHAR(20) NOT NULL | `httpMethod` | `GET`, `POST`, ... |
| `time_out_ms` | INT NOT NULL | `timeOutMs` | default 30000. **Note the column name** (`time_out_ms`, not `timeout_ms`). |
| `auth_required` | BOOLEAN NOT NULL | `authRequired` | stored, not enforced yet |
| `status` | VARCHAR(20) NOT NULL | `status` (`RouteStatus`) | `ACTIVE`, `INACTIVE`, `DEPRECATED` |
| `created_at` | DATETIME | `createdAt` | `@CreationTimestamp` |
| `updated_at` | DATETIME | `updatedAt` | `@UpdateTimestamp` |

### `gateway_key_resolver_policy`

| Column | Type | Entity field | Notes |
|--------|------|--------------|-------|
| `id` | BIGINT PK AI | `id` | |
| `policy_code` | VARCHAR(50) | `policyCode` | generated: `PHL-xxxxxxxx` |
| `strategy` | VARCHAR(30) NOT NULL | `strategy` (`KeyResolverStrategy`) | `IP`, `HEADER`, `JWT_CLAIM`, `API_KEY` |
| `header_name` | VARCHAR(100) | `headerName` | for `HEADER` strategy |
| `fallback_strategy` | VARCHAR(30) | `fallbackStrategy` | optional |
| `status` | VARCHAR(20) NOT NULL | `status` (`Status`) | |
| `created_at` / `updated_at` | DATETIME | | |

### `gateway_rate_limit_policy`

| Column | Type | Entity field | Notes |
|--------|------|--------------|-------|
| `id` | BIGINT PK AI | `id` | |
| `replenish_rate` | INT | `replenishRate` | tokens added per second |
| `burst_capacity` | INT | `burstCapacity` | max tokens in bucket |
| `window_seconds` | INT | `windowSeconds` | stored; not passed to SCG today |
| `key_resolver_policy_id` | BIGINT | `keyResolverPolicyId` | FK → key resolver |
| `status` | VARCHAR(20) | `status` | default `ACTIVE` |
| `created_at` / `updated_at` | DATETIME | | |

### `gateway_circuit_breaker_policy`

| Column | Type | Entity field | Notes |
|--------|------|--------------|-------|
| `id` | BIGINT PK AI | `id` | |
| `cb_name` | VARCHAR(50) NOT NULL UNIQUE | `cbName` | human label; the runtime breaker name is the routeCode |
| `sliding_window_type` | VARCHAR(10) | `slidingWindowType` | `COUNT`, `TIME` (stored, not applied) |
| `sliding_window_size` | INT | `slidingWindowSize` | stored, not applied |
| `failure_rate_threshold` | INT | `failureRateThreshold` | % |
| `slow_call_rate_threshold` | INT | `slowCallRateThreshold` | % |
| `slow_call_duration_ms` | INT | `slowCallDurationMs` | |
| `open_state_wait_ms` | INT | `openStateWaitMs` | |
| `half_open_calls` | INT | `halfOpenCalls` | |
| `timeout_ms` | INT | `timeoutMs` | stored; the time limiter uses the **route** timeout instead |
| `created_at` / `updated_at` | DATETIME | | |

### `gateway_route_policy_map`

| Column | Type | Entity field | Notes |
|--------|------|--------------|-------|
| `id` | BIGINT PK AI | `id` | |
| `route_id` | BIGINT NOT NULL | `routeId` | FK → route |
| `rate_limit_policy_id` | BIGINT NOT NULL | `rateLimitPolicyId` | FK; JPA declares NOT NULL |
| `circuit_breaker_policy_id` | BIGINT NOT NULL | `circuitBreakerPolicyId` | FK; JPA declares NOT NULL |
| `status` | VARCHAR(20) | `status` | `ACTIVE` / `INACTIVE` |
| `created_at` / `updated_at` | DATETIME | | |

## 4.3 DDL matching the entities

This DDL is derived from the JPA entities in `modles/` and is the one to use for a fresh local
database. (The SQL in the root `README.md` predates several entity changes: `time_out_ms`,
BIGINT map id, no `name` column on rate limits, no `status` on circuit breakers.)

```sql
CREATE TABLE gateway_route (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    route_code VARCHAR(100) NOT NULL UNIQUE,
    path VARCHAR(255) NOT NULL,
    target_uri VARCHAR(255) NOT NULL,
    http_method VARCHAR(20) NOT NULL,
    time_out_ms INT NOT NULL DEFAULT 30000,
    auth_required BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL
);

CREATE TABLE gateway_key_resolver_policy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_code VARCHAR(50) NULL,
    strategy VARCHAR(30) NOT NULL,
    header_name VARCHAR(100) NULL,
    fallback_strategy VARCHAR(30) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL
);

CREATE TABLE gateway_rate_limit_policy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    replenish_rate INT NULL,
    burst_capacity INT NULL,
    window_seconds INT NULL DEFAULT 1,
    key_resolver_policy_id BIGINT NULL,
    status VARCHAR(20) NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    CONSTRAINT fk_rate_key_policy
        FOREIGN KEY (key_resolver_policy_id) REFERENCES gateway_key_resolver_policy(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE gateway_circuit_breaker_policy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cb_name VARCHAR(50) NOT NULL UNIQUE,
    sliding_window_type VARCHAR(10) NULL DEFAULT 'COUNT',
    sliding_window_size INT NULL DEFAULT 20,
    failure_rate_threshold INT NULL DEFAULT 50,
    slow_call_rate_threshold INT NULL DEFAULT 50,
    slow_call_duration_ms INT NULL DEFAULT 2000,
    open_state_wait_ms INT NULL DEFAULT 30000,
    half_open_calls INT NULL DEFAULT 5,
    timeout_ms INT NULL DEFAULT 2000,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL
);

CREATE TABLE gateway_route_policy_map (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id BIGINT NOT NULL,
    rate_limit_policy_id BIGINT NOT NULL,
    circuit_breaker_policy_id BIGINT NOT NULL,
    status VARCHAR(20) NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    CONSTRAINT fk_route_map_route
        FOREIGN KEY (route_id) REFERENCES gateway_route(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_route_map_rate
        FOREIGN KEY (rate_limit_policy_id) REFERENCES gateway_rate_limit_policy(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_route_map_cb
        FOREIGN KEY (circuit_breaker_policy_id) REFERENCES gateway_circuit_breaker_policy(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);
```

Enum columns are `VARCHAR` because every entity uses `@Enumerated(EnumType.STRING)`. MySQL `ENUM`
types also work but make adding values a schema change.

## 4.4 Sample seed data

```sql
INSERT INTO gateway_key_resolver_policy (policy_code, strategy, header_name, fallback_strategy, status)
VALUES ('PHL-seed0001', 'HEADER', 'X-User-Id', 'IP', 'ACTIVE');

INSERT INTO gateway_rate_limit_policy (replenish_rate, burst_capacity, window_seconds, key_resolver_policy_id, status)
VALUES (5, 10, 1, 1, 'ACTIVE');

INSERT INTO gateway_circuit_breaker_policy (cb_name, sliding_window_type, sliding_window_size, failure_rate_threshold,
    slow_call_rate_threshold, slow_call_duration_ms, open_state_wait_ms, half_open_calls, timeout_ms)
VALUES ('default-cb', 'COUNT', 20, 50, 50, 2000, 30000, 5, 2000);

INSERT INTO gateway_route (service_name, route_code, path, target_uri, http_method, time_out_ms, auth_required, status)
VALUES ('httpbin', 'HTTPBIN-seed0001', '/anything/**', 'https://httpbin.org', 'GET', 30000, FALSE, 'ACTIVE');

INSERT INTO gateway_route_policy_map (route_id, rate_limit_policy_id, circuit_breaker_policy_id, status)
VALUES (1, 1, 1, 'ACTIVE');
```

After restarting the gateway, `GET http://localhost:9090/anything/hello` is proxied to httpbin.

## 4.5 Useful queries

```sql
-- Which routes are live (have an ACTIVE map row)?
SELECT r.route_code, r.http_method, r.path, r.target_uri, m.rate_limit_policy_id, m.circuit_breaker_policy_id
FROM gateway_route r
JOIN gateway_route_policy_map m ON m.route_id = r.id AND m.status = 'ACTIVE';

-- Routes without any policy mapping (never loaded by the gateway)
SELECT r.* FROM gateway_route r
LEFT JOIN gateway_route_policy_map m ON m.route_id = r.id
WHERE m.id IS NULL;
```
