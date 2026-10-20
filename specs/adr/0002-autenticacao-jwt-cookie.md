# ADR-0002 — Autenticação com JWT em cookie HttpOnly

Status: proposta (escolha confirmada pelo autor; detalhes a revisar)

## Contexto

O backend não tem autenticação. `application.properties` já define `security.jwt.token` e `security.jwt.token.expirationHours`, o que indica a intenção de usar JWT. O frontend roda em outra origem (`localhost:5174`) e hoje simula a sessão com um cookie legível por JavaScript.

## Alternativas

1. **JWT em cookie `HttpOnly`** — JavaScript não lê o token, o que reduz o dano de XSS. Exige proteção contra CSRF.
2. **JWT em header `Authorization: Bearer`** — mais simples para clientes não-web, mas o token fica em `localStorage` ou memória, exposto a XSS.
3. **Sessão no servidor** — revogação simples, mas descarta o que já foi configurado e exige armazenamento de sessão.

## Decisão

Opção 1. O token é emitido em `POST /api/auth/signin` no cookie `HttpOnly; SameSite=Lax` (`Secure` desligado em localhost, configurável). Um filtro do Spring Security lê o cookie e preenche o contexto de segurança. A geração e validação do token ficam atrás de um gateway (`TokenGateway`) para manter o `core` independente de biblioteca.

## Consequências

- O frontend não manipula o token; usa `fetch(..., { credentials: 'include' })` e `GET /api/auth/me`.
- CORS precisa de origem explícita e `allowCredentials(true)` (já configurado em `WebConfig`).
- CSRF: `SameSite=Lax` cobre o caso comum; rotas que alteram estado validam também o cabeçalho `Origin`. Em localhost, frontend (`localhost:5174`) e backend (`localhost:8080`) são o mesmo "site" para `SameSite` (portas não contam), então `Lax` funciona. Se um dia for hospedado em domínios distintos, rever.
- Logout precisa limpar o cookie no servidor. Revogação antes da expiração não existe no MVP.
- O segredo atual no repositório deve ser rotacionado.
