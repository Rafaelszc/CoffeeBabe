# ADR-0007 — Orquestração com Docker Compose

Status: aceita (confirmada pelo autor)

## Contexto

O sistema tem três partes (PostgreSQL, backend Spring Boot, frontend Vite) e roda só em localhost. Hoje exige Java 25, Bun e um Postgres instalados, com credenciais fixas em `application.properties`.

## Decisão

Um `docker-compose.yml` na raiz sobe os três serviços. Detalhes em [05-ORCHESTRATION](../05-ORCHESTRATION.md). Pontos principais:

- Configuração por `.env` (não versionado), com `.env.example` versionado.
- Backend configurado só por variáveis de ambiente (binding relaxado do Spring: `SPRING_DATASOURCE_*`, `SECURITY_JWT_TOKEN`), sem alterar `application.properties`.
- Frontend roda o servidor de desenvolvimento do Vite, para manter hot reload e a origem `http://localhost:5174` que o CORS já permite.
- Portas publicadas apenas em `127.0.0.1`.

## Consequências

- Um comando sobe tudo; Docker passa a ser requisito para esse caminho (o caminho sem Docker continua válido).
- O banco do Compose usa a porta 5433 no host por padrão, para não colidir com um Postgres local na 5432.
- O frontend em modo desenvolvimento não é um build de produção; como não há deploy, é aceitável. Se houver deploy, trocar por `vite build` servido por Nginx.
- Testcontainers é usado nos testes de integração; ele precisa do mesmo acesso ao Docker.
