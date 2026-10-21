# 00 — Visão

Status: rascunho para revisão

## Problema

Projeto de portfólio: uma cafeteria fictícia quer vender online sem depender de marketplaces: o cliente navega pelo cardápio, busca o que quer, monta um carrinho e faz o pedido para retirar ou receber em casa.

## Objetivo

CoffeeBabe é um site de cafeteria com quatro capacidades principais:

1. **Buscar** produtos (café, bebidas, doces) por nome e categoria.
2. **Montar o carrinho** com os produtos desejados.
3. **Comprar**: transformar o carrinho em um pedido, para retirada na loja ou entrega.
4. **Ter conta**: cadastro, login e acompanhamento dos próprios pedidos.

Existe também um papel de administrador, que mantém o catálogo e acompanha os pedidos.

## Quem usa

| Papel | O que faz |
|---|---|
| Visitante | Navega no catálogo e busca produtos. Não pode usar carrinho nem comprar. |
| Cliente (`USER`) | Tudo do visitante, mais carrinho, pedidos e perfil. |
| Administrador (`ADMIN`) | Gerencia produtos e atualiza o status dos pedidos. |

## Sinais de sucesso do MVP

- Um cliente novo consegue se cadastrar, encontrar um produto pela busca, colocá-lo no carrinho e concluir um pedido sem ajuda.
- Nenhum cliente consegue ver ou alterar o carrinho ou os pedidos de outro.
- O administrador consegue cadastrar um produto e ver o pedido chegando.

## Fora de escopo no MVP

- Pagamento online (o pagamento é feito na retirada ou na entrega).
- Estoque, cupons, avaliações, programa de fidelidade.
- Aplicativo móvel nativo.
- Múltiplas lojas.
- Hospedagem: o sistema roda só em localhost, como projeto de portfólio.

## Estado atual (resumo)

O projeto está pela metade. Existem catálogo básico, carrinho básico e telas do frontend, mas autenticação, pedidos e busca não estão implementados, e o frontend usa dados simulados em vários pontos. O detalhe está em [01-PRD](01-PRD.md), seção "Estado atual".
