# AGENTS.md

Quarkus 3.33 (LTS) monorepo. All Maven/Quarkus commands run from the `server/` subdirectory; the repo root has no pom.

## Architecture

S3-compatible file server with two server components:
- **REST API** (JAX-RS) on port 8082 — management/health on 8081
- **S3 server** (custom Vert.x router) on port 8083 — starts via `StartupEvent` in `S3Server`

Config uses YAML (`application.yml`) with `@ConfigMapping(prefix = "openfs")`.

Ports: management 8081, http 8082, s3 8083 (test ports: 9081, 9082, 9083).

## Toolchain
- Pinned via `.tool-versions` (asdf): Java 21 (temurin), Maven 3.9.9, Quarkus CLI 3.33.2.
- Platform version is set in `server/pom.xml` (`quarkus.platform.version`, currently `3.33.3`). Keep it aligned with the 3.33 LTS line.

## Package layout
- Source package: `br.com.ordnaelmedeiros.openfs` (matches pom `groupId`).

## Commands (from `server/`)
- Dev mode: `./mvnw quarkus:dev` (Dev UI at `http://localhost:8082/q/dev`)
- Tests: `./mvnw test`
- Tests with containers: `./mvnw test -Dtest.containers.enabled=true` (requires Docker)
- Package: `./mvnw package`; native: `./mvnw package -Dnative`
- Scripts in `scripts/` wrap these from the repo root.

## Docker
- Dockerfiles (`Dockerfile.jvm`, `Dockerfile.native`) are at the repo root; build context is `.` (root), not `server/`.
- COPY paths use `server/...` prefix.
- `scripts/jvm-server.sh` and `scripts/native-server.sh` build and run.

## Test quirks
- Tests are `@QuarkusTest` classes named `*Test`.
- Tests use `@ParameterizedTest` with `TargetProvider` to select targets based on `-Dtest.containers.enabled`:
  - Default (or `false`): only `QUARKUS` target (no Docker)
  - When `true`: all 3 targets (`QUARKUS`, `JVM_CONTAINER`, `NATIVE_CONTAINER`)
- S3 endpoint tests extend `BaseResourceTestS3`; API endpoint tests extend `BaseResourceTestAPI`.
- Container tests use `@ExtendWith(OpenFsContainerExtension.class)` which builds images from the root Dockerfiles via testcontainers.
- `*IT` classes use `@QuarkusIntegrationTest`; `skipITs=true` by default.

## S3 endpoint pattern
- Implement `S3Endpoint` interface (define `Request` with method+path, implement `handle(RoutingContext)`).
- CDI discovers all `S3Endpoint` beans automatically; `S3Server` registers them on the Vert.x router.
- Use `S3ResponseWriter` for XML/JSON content negotiation in responses.

## Misc
- No CI, lint, or formatter config yet; follow Quarkus defaults.
- Java and XML files use 2-space indentation (no tabs).
- Entire repo is currently untracked (no commits yet).
