# ADR-0004 — Pedido sem pagamento online no MVP

Status: aceita (confirmada pelo autor)

## Contexto

O objetivo do site inclui "compra". Integrar um gateway de pagamento traz webhooks, segredos adicionais, conciliação e requisitos de conformidade.

## Decisão

O MVP cria o pedido sem cobrar online. O pagamento acontece na retirada ou na entrega. O pedido suporta `PICKUP` e `DELIVERY`. Preços e total são sempre calculados no servidor.

## Consequências

- Não há estados `PAID`/`REFUNDED` nem integração externa.
- O modelo de `Order` deve permitir adicionar `payment` depois, sem reescrever o pedido (por exemplo, tabela separada).
- Risco de pedidos não retirados: o fluxo de status precisa permitir cancelamento por `ADMIN`.
- Pagamento online fica como candidato para a segunda fase.
