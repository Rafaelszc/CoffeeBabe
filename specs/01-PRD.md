# 01 — PRD

Status: rascunho para revisão (revisão 3). Decisões confirmadas com o autor: pedido sem pagamento online, retirada e entrega, JWT em cookie HttpOnly, busca por nome, categoria e paginação, categorias `HOT_COFFEE`, `COLD_COFFEE` e `SNACK`, entrega fixa R$ 20,00, imposto 3%, cancelamento pelo cliente, execução só em localhost (portfólio).

## 1. Estado atual (levantado no código)

| Área | Backend | Frontend |
|---|---|---|
| Catálogo | `GET /api/product/get/all`, `/get?id=`, `/get/best`, `POST /create`. `removeProduct` existe no use case mas não tem endpoint. | `fetchProducts` retorna `mockData`. |
| Carrinho | `POST /api/cart/add`, `DELETE /delete`, `PATCH /decrease`, `GET /get`. Todos recebem `userId` do cliente. Quantidade = número de linhas. | `fetchCart` chama `http://localhost:8080/api/cart` (rota que não existe). |
| Auth | `UserAuthImpl.signUp` vazio, `signIn` lança `RuntimeException`. Sem Spring Security. Senha não é hasheada. | `signInUser` e `signUpUser` usam `alert` e um cookie falso. |
| Pedidos | Não existe. | `Checkout` mostra o carrinho; sem botão de finalizar. |
| Busca | Não existe. | Não existe. |
| Usuário | `POST /api/user/create` cria sempre `ADMIN`, sem proteção. | `Profile` existe. |

Problemas conhecidos que as features abaixo devem corrigir:

- `CartController` usa `PATCH`, mas o CORS de `WebConfig` só permite `GET, POST, DELETE, OPTIONS`.
- Respostas `GET` usam `302 FOUND` em vez de `200 OK`.
- `@NotBlank` aplicado a `UUID` (`Cart`) e a `double` (`Product.price`) não funciona; falta `@NotNull`/`@Positive`.
- `User.password` limitado a 30 caracteres na entidade, o que impede salvar um hash BCrypt.
- Migration: `username VARCHAR(20)` contra `@Size(max = 30)` na entidade; `email VARCHAR(30)`; `product.name VARCHAR(15)`.
- `UserNotAuthorizedException` responde com a mensagem "User not found!".
- `image_url` do seed aponta para `src/assets/...` (caminho do frontend); será substituído por imagem no banco (RF-CAT-7).
- Segredo JWT e senha do banco estão commitados em `application.properties`.

## 2. Mapa de capacidades

| Módulo | Responsabilidade | Depende de |
|---|---|---|
| `identity` | Cadastro, login, sessão, papéis | — |
| `catalog` | Produtos, categorias, busca, paginação, mais vendidos | `identity` (só para rotas de admin) |
| `cart` | Carrinho do cliente autenticado | `identity`, `catalog` |
| `orders` | Checkout, pedidos, status, retirada/entrega | `identity`, `cart`, `catalog` |

Ordem de construção: `identity` → `catalog` → `cart` → `orders`.
Cada módulo vira uma ou mais branches `feature/<nome>` a partir de `develop` (ver [04-GIT-FLOW](04-GIT-FLOW.md)).

## 3. Requisitos por módulo

### 3.1 `identity`

- RF-ID-1: cadastro com nome de usuário (3–30), e-mail válido e senha (mínimo 8). Cadastro sempre cria `USER`.
- RF-ID-2: login por e-mail e senha. Em caso de sucesso, define cookie `HttpOnly` com o JWT. Em caso de falha, mensagem genérica (não revela se o e-mail existe).
- RF-ID-3: logout limpa o cookie.
- RF-ID-4: `GET /api/auth/me` devolve o usuário logado (sem senha).
- RF-ID-5: remover `POST /api/user/create` público; criação de `ADMIN` só por seed/migration ou por outro `ADMIN`.

Critérios de aceite:
- Senha nunca aparece em resposta nem em log, e é armazenada com BCrypt.
- Requisição sem cookie válido a rota protegida retorna `401`; papel insuficiente retorna `403`.

### 3.2 `catalog`

- RF-CAT-1: listar produtos com paginação (`page`, `size`; `size` máximo 50).
- RF-CAT-2: buscar por texto no nome e na descrição (`q`), sem diferenciar maiúsculas.
- RF-CAT-3: filtrar por categoria (`category`).
- RF-CAT-4: produto tem categoria obrigatória, enum com três valores: `HOT_COFFEE` (café quente), `COLD_COFFEE` (café gelado) e `SNACK` (petiscos). Os produtos do seed recebem categoria na migration (Cappuccino e Mocha em `HOT_COFFEE`, Cake em `SNACK`).
- RF-CAT-7: `ADMIN` envia a imagem do produto por upload (`multipart/form-data`). O arquivo é gravado no PostgreSQL (coluna `BYTEA`) e servido por `GET /api/product/{id}/image` com o `Content-Type` correto e cache (`ETag`). Formatos: JPEG, PNG e WebP; máximo de 2 MB. O tipo é validado pelo conteúdo do arquivo (assinatura inicial), não só pela extensão ou pelo cabeçalho. O produto não tem mais `image_url`; a resposta traz a URL da imagem derivada do id.
- RF-CAT-8: na primeira subida com banco vazio, o backend carrega os produtos do seed (Cappuccino, Mocha, Cake) com suas imagens (`product-cappuccino.jpg`, `product-mocha.jpg`, `product-cake.jpg`) para o Postgres. As imagens ficam em `backend/src/main/resources/seed/images/` e são lidas por um `ApplicationRunner` idempotente: só insere o que ainda não existe (por nome do produto), então reiniciar não duplica nem sobrescreve imagens enviadas pelo admin. A coluna `image_url` é removida pela migration `V7`; a `V2`, já aplicada, não é editada.
- RF-CAT-5: `ADMIN` cria, edita e remove produtos; leitura é pública.
- RF-CAT-6: mais vendidos continua em `/get/best`. A origem do ranking (tabela `best_seller` manual ou calculada por pedidos) fica para depois que `orders` existir.

Critérios de aceite:
- `GET /api/product?q=cap&category=HOT_COFFEE&page=0&size=12` retorna só produtos que casam com os dois filtros e informa total de páginas.
- Preço deve ser positivo; nome único; erro de validação retorna `400` com lista de campos.
- Upload de arquivo maior que 2 MB ou de tipo não permitido retorna `400` (ou `413` para tamanho). Arquivo `.png` cujo conteúdo não é PNG é rejeitado.
- Imagem enviada volta idêntica (mesmos bytes) em `GET /api/product/{id}/image`.
- Remover o produto remove a imagem.

### 3.3 `cart`

- RF-CART-1: o carrinho pertence ao usuário do token. `userId` deixa de ser parâmetro.
- RF-CART-2: adicionar produto, diminuir uma unidade, remover o item, listar.
- RF-CART-3: a resposta de listagem já traz itens agrupados com quantidade, `subtotal`, `tax` e `total` (o `Checkout` do frontend espera `products`, `subtotal`, `tax`). Regras de cálculo em "Regras de preço" abaixo.
- RF-CART-4: o carrinho é limpo ao concluir um pedido.

Critérios de aceite:
- Usuário A nunca lê nem altera o carrinho do usuário B.
- Adicionar produto inexistente retorna `404`.

### 3.4 `orders`

- RF-ORD-1: o cliente cria um pedido a partir do carrinho, escolhendo `PICKUP` (retirada) ou `DELIVERY` (entrega).
- RF-ORD-2: `DELIVERY` exige endereço e tem taxa de entrega fixa de R$ 20,00; `PICKUP` não tem taxa.
- RF-ORD-3: o pedido guarda um snapshot dos itens (nome, preço unitário, quantidade) para que mudar o catálogo não altere pedidos antigos.
- RF-ORD-4: o preço é sempre calculado no servidor, nunca aceito do cliente.
- RF-ORD-5: status: `PENDING` → `CONFIRMED` → `READY` → (`OUT_FOR_DELIVERY` só em entrega) → `COMPLETED`; `CANCELLED` a partir de `PENDING` ou `CONFIRMED`.
- RF-ORD-6: o cliente lista e vê os próprios pedidos e pode cancelar o próprio pedido enquanto estiver `PENDING` ou `CONFIRMED`; `ADMIN` lista todos, muda o status e também pode cancelar.
- RF-ORD-7: pagamento é feito na retirada ou na entrega; o sistema não processa pagamento.

#### Regras de preço (carrinho e pedido)

- Moeda: real (BRL), exibido como `R$`. O frontend hoje mostra `$` e precisa ser ajustado.
- `subtotal` = soma de `preço × quantidade` dos produtos.
- `tax` (imposto) = 3% do `subtotal`, calculado só sobre os produtos, não sobre a taxa de entrega.
- `deliveryFee` = R$ 20,00 se `DELIVERY`, senão R$ 0,00.
- `total` = `subtotal + tax + deliveryFee`.
- Valores monetários usam `BigDecimal` com 2 casas e arredondamento `HALF_UP`. Hoje `Product.price` é `double` e a coluna é `REAL`; ambos devem virar `NUMERIC(10,2)`/`BigDecimal` por causa de erros de ponto flutuante.
- O pedido grava `subtotal`, `tax`, `deliveryFee` e `total` calculados no momento da criação.
- Exemplo: 2 × R$ 4,00 + 1 × R$ 4,50 = subtotal R$ 12,50; tax R$ 0,38 (0,375 arredondado); entrega R$ 20,00; total R$ 32,88.

Critérios de aceite:
- O exemplo acima é um teste unitário do cálculo.
- Cliente cancela o próprio pedido `PENDING` ou `CONFIRMED`; em `READY` ou posterior retorna `409`.
- Pedido com carrinho vazio retorna `400`.
- Transição de status inválida retorna `409`.
- Cliente não consegue ver pedido de outro (`404`, não `403`, para não revelar existência).

## 4. Requisitos não funcionais

- RNF-1: toda resposta segue o envelope `ApiResponseEntity` (`success`, `message`, `data`, `errors`).
- RNF-2: schema do banco só muda por migration Flyway nova; migrations já aplicadas não são editadas depois de entrarem em `main`.
- RNF-3: configuração sensível vem de variáveis de ambiente (ver [03-SECURITY](03-SECURITY.md)).
- RNF-4: listagens são paginadas; nenhum endpoint devolve tabela inteira sem limite.
- RNF-5: o frontend funciona em telas de celular (já há `BottomNavbar`).
- RNF-6: `docker compose up --build` sobe banco, backend e frontend do zero, sem instalar Java, Bun ou Postgres na máquina (ver [05-ORCHESTRATION](05-ORCHESTRATION.md)).

## 5. Estratégia de testes

- Backend: JUnit 5 + Spring Boot Test. Regras de negócio em `application/` com testes unitários usando gateways simulados; controllers e segurança com `@WebMvcTest`/`MockMvc`; repositórios, migrations e upload de imagem com Postgres real via Testcontainers (decisão confirmada; exige Docker, já requisito do projeto).
- Hoje existe apenas `BackendApplicationTests`, que depende de um banco ao vivo.
- Frontend: sem testes configurados. Proposta: Vitest + Testing Library para formulários e carrinho, depois de `identity` estar pronto.
- Cada feature só entra em `develop` com testes dos critérios de aceite do seu módulo.

## 6. Perguntas em aberto

Resolvidas pelo autor: categorias (`HOT_COFFEE`, `COLD_COFFEE`), taxa de entrega fixa de R$ 20,00, imposto de 3% sobre os produtos, cancelamento pelo cliente até `CONFIRMED`, sistema só em localhost (projeto de portfólio), categoria `SNACK` (petiscos), imagem por upload gravada no banco, imagens do seed carregadas na primeira subida, Testcontainers, orquestração com Docker Compose.

Nenhuma. As duas últimas foram resolvidas: Testcontainers para testes de integração e carga das imagens do seed na primeira subida (RF-CAT-8).
