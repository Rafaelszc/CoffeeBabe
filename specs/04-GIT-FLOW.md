# 04 — Git flow

Status: rascunho para revisão. Decisão registrada em [ADR-0005](adr/0005-git-flow.md).

## Branches

| Branch | Origem | Destino | Função |
|---|---|---|---|
| `main` | — | — | Código estável, pronto para release. Só recebe merge de `develop` (release) ou de `hotfix/*`. |
| `develop` | `main` | — | Integração. Base de todas as features. Já criada e publicada em `origin/develop`. |
| `feature/<nome>` | `develop` | `develop` | Uma capacidade ou parte dela. Nome em kebab-case. |
| `hotfix/<nome>` | `main` | `main` e `develop` | Correção urgente em produção. |

## Fluxo de uma feature

```
git checkout develop && git pull
git checkout -b feature/<nome>
# trabalhar, commitar
git push -u origin feature/<nome>
# abrir PR para develop
```

Convenção de commit já usada no repositório: `tipo (escopo): descrição`, por exemplo `feat (backend): implements global exception handler`. Tipos em uso: `feat`, `refactor`. Escopos: `backend`, `frontend`, `specs`.

## Features planejadas

Ordem de acordo com o mapa de capacidades do [PRD](01-PRD.md):

1. `feature/specs-iniciais` — estas specs
2. `feature/docker-orchestration` — Compose, Dockerfiles, `.env.example`, configuração por variáveis de ambiente
3. `feature/identity-auth` — Spring Security, cadastro, login, cookie, `/me`, segredos em variáveis de ambiente
4. `feature/catalog-search` — categoria, busca, paginação, CRUD de admin, upload de imagem no banco
5. `feature/cart-per-user` — carrinho do usuário autenticado, quantidade
6. `feature/orders-checkout` — pedido, status, entrega/retirada
7. `feature/frontend-design` — redesign visual, tokens, pt-BR, tema escuro ([06-DESIGN](06-DESIGN.md))
8. `feature/frontend-api-integration` — cliente HTTP único, remoção de `mockData`, guardas de rota (pode ser dividida por módulo)

## Regras

- Nunca commitar direto em `main` nem em `develop`.
- PR para `develop` referencia a seção da spec que implementa e inclui os testes dos critérios de aceite.
- Atualizar a spec no mesmo PR quando a decisão mudar.
- Release: `develop` → `main` por PR, com tag de versão.
