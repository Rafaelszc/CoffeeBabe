# ADR-0008 — Refresh token com rotação em Redis

Status: aceita (confirmada pelo autor)

## Contexto

O JWT de acesso durava 3 horas: longo para um token que não pode ser revogado, curto para o usuário não precisar entrar de novo várias vezes por dia.

## Decisão

- Token de acesso (JWT, cookie `access_token`) de 15 minutos.
- Refresh token opaco de 256 bits no cookie `refresh_token` (`HttpOnly`, `SameSite=Strict`, `Path=/api/auth`), válido por 7 dias.
- O Redis guarda só o hash SHA-256: `refresh:active:{hash}` → `userId` (TTL de 7 dias) e `refresh:user:{userId}` com os hashes ativos do usuário.
- `POST /api/auth/refresh` consome o token (GETDEL, atômico), marca o hash como usado (`refresh:used:{hash}`) e emite um novo par. O papel vem do banco, então uma mudança de papel vale na próxima renovação.
- Um hash usado que volta a aparecer revoga todas as sessões do usuário. Exceção: até 30 s depois da rotação o reuso só recebe `401`, porque duas abas renovando ao mesmo tempo mandam o mesmo token; o frontend repete a requisição com o cookie novo que a outra aba recebeu.
- O logout revoga só o token do navegador atual.
- Redis `redis:8-alpine` no Compose, com senha e sem persistência.

## Consequências

- Revogar a sessão passa a ser possível (logout, reuso), com atraso de até 15 minutos para o token de acesso já emitido.
- Reiniciar o Redis desloga todo mundo; aceitável num projeto que roda só em localhost.
- Mais um serviço e mais uma variável obrigatória (`REDIS_PASSWORD`).
- Testes de integração sobem também um Redis pelo Testcontainers.
