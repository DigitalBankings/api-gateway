# 1. Getting Started

## 1.1 Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| JDK | 17 | `build.gradle` uses a Java 17 toolchain. Gradle will fail if no JDK 17 is available. |
| Gradle | wrapper (8.14.3) | Use `./gradlew` (or `gradlew.bat` on Windows). Do not install Gradle separately. |
| MySQL | 8.x | Holds the route and policy tables. |
| Redis | 6+ | Required for rate limiting and for the default cache backend. |
| Docker (optional) | any | Only needed if you want to run Keycloak via `docker-compose.keycloak.yml`. |
| GitHub account + token | – | Needed to download the private `seyha-logger` dependency (see 1.2). |

## 1.2 GitHub Packages credentials (required to build)

`build.gradle` pulls `com.github.OEURNSEYHA:seyha-logger:1.1.16` from a GitHub Packages Maven
repository. Gradle must authenticate even for public packages.

Create (or edit) `~/.gradle/gradle.properties`:

```properties
gpr.user=<your-github-username>
gpr.key=<a GitHub personal access token with read:packages scope>
```

Alternatively export environment variables `GITHUB_ACTOR` and `GITHUB_TOKEN`.
Without these the build fails with `Could not resolve com.github.OEURNSEYHA:seyha-logger`.

## 1.3 Create the database

```sql
CREATE DATABASE api_gateway CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Then run the table DDL from [Database](04-database.md#43-ddl-matching-the-entities).
Hibernate is configured with `ddl-auto: none`, so **tables are never created automatically**.

## 1.4 Environment variables

`src/main/resources/application.yml` reads everything from the environment. There are no
defaults for the datasource, Redis host or server port, so the app will not start without them.

| Variable | Required | Default | Purpose |
|----------|----------|---------|---------|
| `SERVER_PORT` | yes | – | HTTP port of the gateway (OpenAPI config assumes `9090`). |
| `SPRING_PROFILES_ACTIVE` | no | `local` | Active profile. `application-local.yml` is git-ignored; create your own. |
| `SPRING_DATASOURCE_URL` | yes | – | e.g. `jdbc:mysql://localhost:3306/api_gateway` |
| `SPRING_DATASOURCE_USERNAME` | yes | – | MySQL user |
| `SPRING_DATASOURCE_PASSWORD` | yes | – | MySQL password (may be empty but must be set) |
| `REDIS_HOST` | yes | – | Redis host |
| `REDIS_PORT` | no | `6379` | Redis port |
| `REDIS_USERNAME` | no | `default` | Redis ACL user |
| `REDIS_PASSWORD` | yes | – | Redis password (set to empty string if none) |
| `cache.type` (property) | no | `redis` | `redis` or `caffeine`. See [Configuration](06-configuration.md#62-cache-backend). |

### Option A – local profile file (recommended)

Create `src/main/resources/application-local.yml` (already in `.gitignore`):

```yaml
server:
  port: 9090

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/api_gateway
    username: root
    password: root
  data:
    redis:
      host: localhost
      port: 6379
      password: ""

cache:
  type: redis   # or caffeine if you do not want to run Redis for caching
```

Values in the profile file override the `${...}` placeholders in `application.yml`.

### Option B – environment variables

PowerShell:

```powershell
$env:SERVER_PORT = "9090"
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3306/api_gateway"
$env:SPRING_DATASOURCE_USERNAME = "root"
$env:SPRING_DATASOURCE_PASSWORD = "root"
$env:REDIS_HOST = "localhost"
$env:REDIS_PASSWORD = ""
```

Bash:

```bash
export SERVER_PORT=9090
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/api_gateway
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=root
export REDIS_HOST=localhost
export REDIS_PASSWORD=
```

## 1.5 Build and run

```bash
# format code (required before committing, see Development Guide)
./gradlew spotlessApply

# compile + run tests
./gradlew build

# run the gateway
./gradlew bootRun
```

On Windows replace `./gradlew` with `gradlew.bat`.

You should see the ASCII banner from `banner.txt` and then log lines such as
`Loaded Route: AUTH-SERVICE-ea63159f → http://localhost:8000` for every active route mapping
in the database. With an empty database no routes are loaded, which is fine.

## 1.6 Verify it is running

| URL | What it is |
|-----|------------|
| `http://localhost:9090/swagger-ui.html` | Swagger UI for the admin API (springdoc) |
| `http://localhost:9090/v3/api-docs` | Raw OpenAPI JSON |

Note: there is no Spring Boot Actuator dependency, so `/actuator/**` endpoints (health, prometheus)
are **not** available yet. See [Troubleshooting & Known Gaps](08-troubleshooting.md).

## 1.7 Register your first route

Follow the end-to-end walkthrough in [Admin API Reference](05-api-reference.md#57-end-to-end-walkthrough).
It creates a key-resolver policy, a rate-limit policy, a circuit-breaker policy, a route,
links them together, and then calls the proxied route.

## 1.8 Optional: Keycloak

`docker-compose.keycloak.yml` starts Keycloak 24 with a Postgres database:

```bash
docker compose -f docker-compose.keycloak.yml up -d
# admin console: http://localhost:8080  (admin / admin)
```

Keycloak is **not yet wired into the gateway** (there is no Spring Security or OAuth2 dependency).
The compose file exists so the auth integration can be developed next. The `authRequired`
flag on a route is stored but not enforced today.

## 1.9 IDE setup

* Install the **Lombok** plugin and enable annotation processing (IntelliJ: Settings → Build →
  Compiler → Annotation Processors).
* Import the project as a Gradle project; let the IDE use the wrapper.
* Install a google-java-format plugin or rely on `./gradlew spotlessApply`.
