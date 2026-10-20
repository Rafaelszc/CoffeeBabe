# 05 — Orquestração

Status: implementada e validada com Docker em execução. Decisão: [ADR-0007](adr/0007-orquestracao-docker-compose.md).

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
| `redis` | `redis:8-alpine`, senha `REDIS_PASSWORD`, sem persistência | sem porta no host (só a rede do Compose) |
| `frontend` | `oven/bun:1`, Vite em modo dev, `src/` e `public/` montados para hot reload | `http://localhost:5174` |

O backend só inicia depois que o `db` está saudável. O Flyway aplica as migrations na primeira subida.

## Variáveis (`.env`)

| Variável | Uso |
|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | Criação do banco e credenciais do backend |
| `DB_HOST_PORT` | Porta do banco no host (padrão 5433) |
| `JWT_SECRET` | Vira `SECURITY_JWT_TOKEN` no backend |
| `REDIS_PASSWORD` | Senha do Redis (obrigatória), usada pelo backend |

`POSTGRES_PASSWORD` e `JWT_SECRET` não têm valor padrão: o Compose se recusa a subir sem eles. `.env` está no `.gitignore` da raiz.

## Limites conhecidos

- Os bind mounts do frontend (`src/`, `public/`) usam o label `:z`. Sem ele, hosts com SELinux em `Enforcing` (Fedora) negam a leitura dentro do contêiner e o Vite falha com `EACCES`. O `:z` reetiqueta só esses dois diretórios e é ignorado em hosts sem SELinux.
- `JWT_SECRET` precisa ter pelo menos 32 bytes (HS256); com menos, o backend não sobe. `ADMIN_EMAIL`/`ADMIN_PASSWORD` criam o admin inicial.
- O frontend fala com o backend por `VITE_API_URL` (passada pelo Compose; padrão `http://localhost:8080`).
- O CORS do backend usa `CORS_ALLOWED_ORIGIN` (padrão `http://localhost:5174`, o mesmo valor publicado pelo Compose).
- Não há healthcheck do backend: sem Spring Actuator no `pom.xml`.

## Critérios de aceite

- `docker compose up --build` num clone limpo, com `.env` preenchido, termina com os três serviços em execução.
- `curl http://localhost:8080/api/product/get/all` responde com os produtos do seed.
- `http://localhost:5174` abre a home.
- `docker compose down && docker compose up` preserva os dados; `down -v` os apaga.
- Subir sem `.env` falha com mensagem pedindo `POSTGRES_PASSWORD`.
