# Plano de implementação

Base: [specs/](../specs/00-VISION.md) (PRD revisão 3). Cada fase vira uma branch `feature/*` a partir de `develop` e um PR de volta para `develop`.

## Ordem e dependências

```
F0 specs ─► F1 docker-orchestration ─► F2 identity-auth ─► F3 catalog-search ─► F4 cart-per-user ─► F5 orders-checkout
                                                                  └──────────────────────────────────────────┐
                                                           F6 frontend-api-integration (por módulo, após cada F2–F5)
```

- F1 vem antes de tudo porque Testcontainers (F2 em diante) e o ambiente padrão dependem do Docker.
- F2 vem antes das demais porque carrinho, pedidos e escrita de catálogo precisam de usuário autenticado.
- F6 é fatiada: o trecho de cada módulo do frontend entra junto ou logo depois do backend dele, para sempre haver um fluxo funcionando de ponta a ponta. Só o cliente HTTP único (T6.1) precisa existir cedo.
- Dentro de F3, F4 e F5 não há paralelismo útil: cada uma muda o schema e o contrato do frontend.

## Riscos

| Risco | Mitigação |
|---|---|
| Migrations V2–V4 foram alteradas e renomeadas no working tree; bancos antigos quebram no checksum do Flyway | Documentar `docker compose down -v` ao trocar de branch; migrations novas a partir da `V5` |
| Spring Boot 4.0 e Java 25 são recentes; APIs de Security e Testcontainers mudaram | Validar cada dependência nova com um teste mínimo antes de construir em cima |
| Passar `price` de `double` para `BigDecimal` quebra DTOs e frontend | Fazer em F3 com teste de contrato do JSON |
| Cookie `HttpOnly` não funciona se o frontend não usar `credentials: 'include'` | T6.1 antes de T2.x integrar no frontend |
| Entidades JPA no `core` acoplam validação ao hash de senha | DTO valida a senha em texto puro; entidade só guarda o hash (ADR-0003) |

## Checkpoints

- Fim de F1: `docker compose up --build` sobe os três serviços (critérios em `specs/05-ORCHESTRATION.md`).
- Fim de F2: login pelo navegador funciona e `/api/cart/*` sem cookie retorna `401`.
- Fim de F3: busca paginada por texto e categoria, upload de imagem exibido na home.
- Fim de F4: dois usuários com carrinhos isolados.
- Fim de F5: pedido de ponta a ponta com total R$ 32,88 no exemplo do PRD.
