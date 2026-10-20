# 05 — Orquestração

Status: implementada na branch `feature/docker-orchestration`; ainda não validada com Docker em execução. Decisão: [ADR-0007](adr/0007-orquestracao-docker-compose.md).

## Uso

```
cp .env.example .env        # ajuste POSTGRES_PASSWORD e JWT_SECRET
docker compose up --build   # sobe db, backend e frontend
docker compose down         # para (mantém os dados)
docker compose down -v      # para e apaga o volume do banco
```

| Serviço | Imagem | Endereço no host |
|---|---|---|
| `db` | `postgres:17`, volume `pgdata`, healthcheck `pg_isready` | `127.0.0.1:5433` (`DB_HOST_PORT`) |
| `backend` | build multi-stage: `eclipse-temurin:25-jdk` compila, `25-jre` executa como usuário sem privilégios | `http://localhost:8080` |
| `frontend` | `oven/bun:1`, Vite em modo dev, `src/` e `public/` montados para hot reload | `http://localhost:5174` |

O backend só inicia depois que o `db` está saudável. O Flyway aplica as migrations na primeira subida.

## Variáveis (`.env`)

| Variável | Uso |
|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | Criação do banco e credenciais do backend |
| `DB_HOST_PORT` | Porta do banco no host (padrão 5433) |
| `JWT_SECRET` | Vira `SECURITY_JWT_TOKEN` no backend |

`POSTGRES_PASSWORD` e `JWT_SECRET` não têm valor padrão: o Compose se recusa a subir sem eles. `.env` está no `.gitignore` da raiz.

## Limites conhecidos

- `application.properties` ainda contém o segredo JWT e a senha antigos como padrão para execução fora do Docker. A limpeza é parte de `feature/identity-auth` (SEC-14).
- O frontend lê `import.meta.env.BASE_URL` e hardcoda `http://localhost:8080`; `VITE_API_URL` já é passada pelo Compose para quando o cliente HTTP único existir (`feature/frontend-api-integration`).
- O CORS do backend fixa `http://localhost:5174`, o mesmo valor publicado pelo Compose.
- Não há healthcheck do backend: sem Spring Actuator no `pom.xml`.

## Critérios de aceite

- `docker compose up --build` num clone limpo, com `.env` preenchido, termina com os três serviços em execução.
- `curl http://localhost:8080/api/product/get/all` responde com os produtos do seed.
- `http://localhost:5174` abre a home.
- `docker compose down && docker compose up` preserva os dados; `down -v` os apaga.
- Subir sem `.env` falha com mensagem pedindo `POSTGRES_PASSWORD`.
