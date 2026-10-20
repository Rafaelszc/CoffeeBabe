# ADR-0005 — Git flow com `develop` e `feature/*`

Status: aceita (confirmada pelo autor)

## Contexto

O repositório tinha apenas `main`. O projeto está pela metade e vai receber várias features em sequência.

## Decisão

Adotar git flow simplificado: `main` estável, `develop` de integração, `feature/<nome>` a partir de `develop`, `hotfix/<nome>` a partir de `main`. Detalhes em [04-GIT-FLOW](../04-GIT-FLOW.md). `develop` foi criada a partir de `main` e publicada em `origin/develop`.

## Consequências

- Todo trabalho novo passa por PR para `develop`.
- `main` só muda em releases e hotfixes.
- Sem branches `release/*` por enquanto; entram se houver estabilização longa.
