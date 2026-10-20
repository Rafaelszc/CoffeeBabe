# ADR-0001 — Arquitetura limpa/hexagonal no backend

Status: aceita (já adotada no código)

## Contexto

O backend já separa `core` (entidades, use cases, gateways), `application` (implementações) e `adapter`/`infra` (web e persistência). O projeto também serve como estudo, e a separação facilita testar regras de negócio sem banco.

## Decisão

Manter a estrutura. Dependências apontam para dentro: `adapter`/`infra` → `application` → `core`. Use cases dependem de gateways (interfaces), nunca de repositórios Spring Data.

## Consequências

- Regras de negócio testáveis com gateways simulados.
- Mais arquivos por funcionalidade (use case, impl, gateway, adapter, DTO).
- Cada módulo novo segue o mesmo molde.
