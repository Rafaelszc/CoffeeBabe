# 02 — Arquitetura

Status: rascunho para revisão

## Visão geral

```
frontend (React/Vite, :5174)  --HTTP+cookie-->  backend (Spring Boot, :8080)  -->  PostgreSQL :5432
```

Dois projetos independentes no mesmo repositório: `backend/` e `frontend/`. A comunicação é REST/JSON. O CORS permite só a origem do frontend e usa credenciais (cookie).

## Backend

Java 25, Spring Boot, Spring Data JPA, Flyway, Lombok, Bean Validation. Pacote raiz `com.coffeebabe.backend`. Estilo limpo/hexagonal; as dependências apontam para dentro:

```
adapter / infra  -->  application  -->  core
```

| Pacote | Papel |
|---|---|
| `core/entities` | Entidades de domínio (hoje também são entidades JPA). |
| `core/usecases` | Portas de entrada (interfaces). |
| `core/gateways` | Portas de saída (interfaces de persistência). |
| `core/exceptions` | Exceções de domínio. |
| `application` | Implementação dos use cases (`*Impl`), dependem só de gateways. |
| `adapter/web/controller` | Controllers REST. |
| `adapter/dtos` | DTOs de entrada e saída. |
| `adapter/persistence` | Implementação dos gateways (`*CrudAdapter`) sobre os repositórios. |
| `infra/persistence/jpa` | Repositórios Spring Data. |
| `infra/exception` | `RestExceptionAdvice`, mapeia exceções para HTTP. |
| `infra/web` | `WebConfig` (CORS). |

Novo: `infra/security` para a configuração do Spring Security, filtro JWT e `PasswordEncoder`. O use case de auth fica em `application`; a emissão e validação de JWT entram atrás de uma porta (`TokenGateway`) para o `core` não depender de biblioteca (ver [ADR-0002](adr/0002-autenticacao-jwt-cookie.md)).

### Convenções a manter

- Um controller por módulo, rotas sob `/api/<recurso>`.
- Erro de domínio vira exceção em `core/exceptions` e é mapeada em `RestExceptionAdvice`. Falta mapear `MethodArgumentNotValidException` e `ConstraintViolationException` com os campos inválidos.
- Respostas sempre em `ApiResponseEntity`. `GET` retorna `200`, criação `201`; `302` deixa de ser usado.
- O usuário autenticado é obtido do contexto de segurança, nunca de parâmetro ou corpo.

### Dados

Migrations em `backend/src/main/resources/db/migration/` (`V<n>__create-table-<nome>.sql`, tabelas no singular, exceto `users`, que é palavra reservada). Existentes: `users`, `product`, `best_seller`, `cart`.

Mudanças previstas (cada uma em migration nova, `V5` em diante):

| Mudança | Módulo |
|---|---|
| Ajustar `users` (tamanhos, índice único de e-mail com `lower()`), hash de senha | identity |
| `product.category` (`HOT_COFFEE`, `COLD_COFFEE`, `SNACK`), ampliar `name`, `price` para `NUMERIC(10,2)`, índice para busca (`pg_trgm` ou `ILIKE` simples no MVP) | catalog |
| Seed de imagens: não é migration (SQL não lê arquivos); um `ApplicationRunner` em `infra` carrega `resources/seed/images/*` na primeira subida | catalog |
| Tabela `product_image` (`product_id` único com `ON DELETE CASCADE`, `content_type`, `size_bytes`, `data BYTEA`, `updated_at`); remover `product.image_url` | catalog |
| `cart` ganha `quantity` e `UNIQUE (user_id, product_id)` em vez de uma linha por unidade | cart |
| `orders`, `order_item` (snapshot de nome e preço), endereço de entrega | orders |

Observação: as migrations V2, V3 e V4 foram renomeadas/alteradas no working tree ainda não commitado. Enquanto não houver ambiente compartilhado com banco já migrado, a equipe pode recriar o banco; depois que `main` for publicada, só migrations novas (RNF-2).

### Ponto de atenção: entidades de domínio = entidades JPA

Hoje `core/entities` carrega anotações JPA e de validação, o que acopla o núcleo ao framework. Para o MVP, a decisão é manter assim e registrar a dívida ([ADR-0003](adr/0003-entidades-jpa-no-core.md)).

## Frontend

React 19, Vite, Tailwind v4, shadcn/ui (Radix), react-router-dom, react-hook-form + zod, sonner. Gerenciador: Bun.

| Pasta | Papel |
|---|---|
| `src/api` | Chamadas HTTP. Deve ter um único cliente (`apiClient`) com `BASE_URL`, `credentials: 'include'` e tratamento do envelope. |
| `src/context/AuthContext` | Estado do usuário logado, preenchido por `GET /api/auth/me`. |
| `src/pages/<Página>` | Telas e componentes específicos. |
| `src/schemas` | Schemas zod, espelhando as validações do backend. |
| `src/data/mockData` | Dados simulados; deve desaparecer à medida que cada módulo for integrado. |

Pontos a corrigir na integração:
- `BASE_URL` em `productService.js` lê `import.meta.env.BASE_URL` (caminho base do Vite). A variável de ambiente correta precisa do prefixo `VITE_` (`VITE_API_URL`).
- URLs `http://localhost:8080` fixas devem usar o cliente único.
- O cookie de sessão não pode mais ser lido nem escrito por JavaScript (`getCookieData`, `clearCookieData` e `document.cookie` saem de `userService.js`).
- Rotas `/checkout`, `/profile` e pedidos precisam de guarda de rota baseada em `AuthContext`.

## Fluxos principais

**Login:** `POST /api/auth/signin` → backend valida e responde `Set-Cookie: token=...; HttpOnly; SameSite=Lax` → frontend chama `GET /api/auth/me` e guarda o usuário no `AuthContext`.

**Compra:** `POST /api/cart/add` (usuário vem do token) → `GET /api/cart` → `POST /api/order` com tipo de entrega (e endereço se `DELIVERY`) → backend recalcula preços a partir do banco, grava `order` + `order_item`, limpa o carrinho na mesma transação → `201` com o pedido.

## Ambientes e comandos

Subir tudo: `docker compose up --build` (ver [05-ORCHESTRATION](05-ORCHESTRATION.md)). Sem Docker, ver `CLAUDE.md`. Backend: `./mvnw spring-boot:run` e `./mvnw test`. Frontend: `bun run dev`, `bun run build`, `bun run lint`. Variáveis de ambiente estão em [03-SECURITY](03-SECURITY.md).

## Decisões registradas

- [ADR-0001](adr/0001-arquitetura-hexagonal.md) — Arquitetura limpa/hexagonal no backend
- [ADR-0002](adr/0002-autenticacao-jwt-cookie.md) — Autenticação com JWT em cookie HttpOnly
- [ADR-0003](adr/0003-entidades-jpa-no-core.md) — Entidades JPA dentro do `core`
- [ADR-0004](adr/0004-pedido-sem-pagamento-online.md) — Pedido sem pagamento online no MVP
- [ADR-0005](adr/0005-git-flow.md) — Git flow com `develop` e `feature/*`
- [ADR-0006](adr/0006-imagens-no-postgres.md) — Imagens de produto armazenadas no PostgreSQL
- [ADR-0007](adr/0007-orquestracao-docker-compose.md) — Orquestração com Docker Compose
