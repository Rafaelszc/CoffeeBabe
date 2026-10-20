# ADR-0003 — Entidades JPA dentro do `core`

Status: aceita como dívida técnica

## Contexto

As classes em `core/entities` (`User`, `Product`, `Cart`) carregam anotações JPA (`@Entity`, `@Table`) e de Bean Validation. Uma arquitetura hexagonal estrita separaria o modelo de domínio do modelo de persistência.

## Decisão

Para o MVP, manter as entidades JPA no `core`. Não criar uma camada de mapeamento domínio ↔ persistência agora.

## Consequências

- Menos código e menos mapeadores para um projeto pequeno.
- O `core` depende de `jakarta.persistence` e `jakarta.validation`.
- Regras de formato de entrada (por exemplo, tamanho da senha em texto puro) pertencem ao DTO, não à entidade, porque a entidade guarda o hash.
- Revisar se o domínio crescer (por exemplo, regras complexas de `Order`) ou se o modelo de dados divergir muito do modelo de negócio.
