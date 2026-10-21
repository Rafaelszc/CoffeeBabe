# ADR-0006 — Imagens de produto armazenadas no PostgreSQL

Status: aceita (confirmada pelo autor)

## Contexto

Hoje `product.image_url` aponta para `src/assets/...`, um caminho do frontend. O autor quer upload de imagem pelo administrador e persistência em banco de dados.

## Alternativas

1. **Coluna `BYTEA` no PostgreSQL** — um único armazenamento, backup e transação junto com o produto, sem volume extra no Compose.
2. **Sistema de arquivos em volume Docker** — mais eficiente para arquivos grandes, mas exige volume, backup separado e consistência entre banco e disco.
3. **Object storage (S3/MinIO)** — escala bem, mas é infraestrutura demais para um projeto local.

## Decisão

Opção 1, em tabela separada `product_image` (`product_id` único com `ON DELETE CASCADE`, `content_type`, `size_bytes`, `data BYTEA`, `updated_at`), para que listagens de produtos não carreguem os bytes. A imagem é servida por `GET /api/product/{id}/image`. Limite de 2 MB; JPEG, PNG e WebP validados pelo conteúdo.

## Consequências

- Listagens continuam leves; a imagem só é lida quando o navegador a pede, com `ETag` para cache.
- O banco cresce com as imagens; aceitável para um catálogo pequeno.
- Para migrar para disco ou S3 depois, basta trocar a implementação do gateway de imagem.
- Os testes de upload usam Postgres real via Testcontainers.
- As imagens do seed vivem em `backend/src/main/resources/seed/images/` e são carregadas na primeira subida por um runner idempotente (RF-CAT-8 no PRD).
