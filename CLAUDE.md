# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

CoffeeBabe is a coffee-shop app: a Spring Boot REST API (`backend/`) and a React SPA (`frontend/`). The README is a placeholder. `Jogo2/` appears in git status as staged-then-deleted files unrelated to this project; ignore it.

## Docker Compose

The root `docker-compose.yml` runs db, backend and frontend without installing Java, Bun or Postgres. See `specs/05-ORCHESTRATION.md`.

```
cp .env.example .env        # set POSTGRES_PASSWORD and JWT_SECRET (required)
docker compose up --build   # db on 127.0.0.1:5433, backend on :8080, frontend on :5174
docker compose down         # stop, keep data
docker compose down -v      # stop and delete the db volume
```

Run `down -v` when switching to a branch with different migrations, since Flyway fails on checksum mismatches. The frontend bind mounts use `:z` so they work under SELinux (Fedora).

## Backend (`backend/`)

Spring Boot (Java 25, Maven wrapper), Postgres + Flyway, Lombok, Bean Validation. JWT config lives in `application.properties`.

```
cd backend
./mvnw spring-boot:run          # needs Postgres at localhost:5432/coffeebabe (postgres/123)
./mvnw test                     # all tests
./mvnw test -Dtest=ClassName#method   # single test
```

Only `BackendApplicationTests` exists, and it needs a live database.

### Architecture (clean/hexagonal, package `com.coffeebabe.backend`)

Dependencies point inward: `adapter`/`infra` -> `application` -> `core`.

- `core/entities`: domain models (`Product`, `User`, `Cart`, `UserRole`) with validation annotations.
- `core/usecases`: input-port interfaces (`ProductCrudUseCase`, `UserAuthUseCase`, `ManageCartUseCase`, ...).
- `core/gateways`: output-port interfaces (`*CrudGateway`) that persistence must implement.
- `core/exceptions`: domain exceptions (e.g. `ProductNotFoundException`, `InvalidDataException`).
- `application`: use-case implementations (`*Impl`) that depend only on gateways.
- `adapter/web/controller`: REST controllers. `adapter/dtos`: request/response DTOs, including `ApiResponseEntity`.
- `adapter/persistence`: gateway implementations (`*CrudAdapter`) that delegate to Spring Data repositories.
- `infra/persistence/jpa`: Spring Data JPA repositories. `infra/exception/RestExceptionAdvice`: global exception handler that maps the core exceptions to HTTP responses. `infra/web/WebConfig`: CORS, which allows only `http://localhost:5174` and the GET/POST/DELETE/OPTIONS methods. Adding PUT/PATCH endpoints requires updating it.

To add a feature: define the use case and gateway in `core`, implement them in `application` and `adapter/persistence`, then expose a controller.

### Database

Flyway migrations are in `src/main/resources/db/migration/` (`V<n>__create-table-<name>.sql`, singular table names). Schema changes need a new migration. Don't edit applied ones unless you are resetting the DB.

## Frontend (`frontend/`)

React 19, Vite, Tailwind CSS v4, shadcn/ui (`components.json`) with Radix, react-hook-form + zod, react-router-dom, sonner toasts. Bun is the package manager (`bun.lock`). The `@` alias maps to `src/`.

```
cd frontend
bun install
bun run dev        # vite dev server
bun run build
bun run lint       # eslint
```

No frontend tests are configured.

Layout of `src/`:
- `api/`: fetch wrappers (`productService.js`, `userService.js`). Some functions still return mock data from `src/data/mockData`, and some hardcode `http://localhost:8080`. Check each function before assuming it hits the backend.
- `components/ui`: shadcn primitives. `components/site`: app chrome (Navbar, Layout, ProductCard, ...).
- `pages/`: route pages with page-specific components in `pages/<Page>/components`. `schemas/`: zod schemas. `context/`: React context.
- `.env` defines `BASE_URL`. `productService.js` reads `import.meta.env.BASE_URL`, which is Vite's built-in base path rather than that variable. Vite only exposes `VITE_`-prefixed variables.
