# Tarefas

Formato: Acceptance = o que deve ser verdade; Verify = como confirmar; Files = onde mexe (≈ máximo 5).
Referências: RF-* no [PRD](../specs/01-PRD.md), SEC-* em [03-SECURITY](../specs/03-SECURITY.md).

## F1 — `feature/docker-orchestration`

- [x] T1.1 Validar o Compose já escrito
  - Acceptance: três serviços em execução; seed visível; dados persistem após `down`/`up`; sem `.env` falha pedindo `POSTGRES_PASSWORD`
  - Verify: critérios de `specs/05-ORCHESTRATION.md`, executados por quem tem acesso ao Docker
  - Files: `docker-compose.yml`, `backend/Dockerfile`, `frontend/Dockerfile` (ajustes se algo falhar)
- [x] T1.2 Atualizar `CLAUDE.md` com os comandos Docker
  - Acceptance: comandos de `docker compose` e do `.env` documentados
  - Verify: leitura
  - Files: `CLAUDE.md`

## F2 — `feature/identity-auth` (RF-ID-1..5, SEC-1..9, SEC-14)

- [x] T2.1 Dependências de teste e de segurança
  - Acceptance: `spring-boot-starter-security` e Testcontainers (Postgres) no `pom.xml`; um teste de integração sobe o contexto contra Postgres em contêiner
  - Verify: `./mvnw test`
  - Files: `backend/pom.xml`, `BackendApplicationTests.java`, config de teste
- [x] T2.2 Migration de `users` e validação de entrada
  - Acceptance: e-mail único case-insensitive; tamanhos coerentes com a entidade; hash cabe na coluna; DTOs com `@Valid` e erros `400` com campos (SEC-10, SEC-12)
  - Verify: testes de repositório e de `MockMvc` com corpo inválido
  - Files: `V5__*.sql`, `User.java`, `SignUpRequestDTO.java`, `RestExceptionAdvice.java`
- [x] T2.3 Cadastro e login (use cases)
  - Acceptance: senha com BCrypt; cadastro cria `USER`; e-mail repetido retorna erro; login com e-mail inexistente e senha errada responde igual (SEC-6)
  - Verify: testes unitários de `UserAuthImpl` com gateways simulados
  - Files: `UserAuthImpl.java`, `UserCrudGateway.java`, `UserCrudAdapter.java`, `TokenGateway.java`
- [x] T2.4 JWT em cookie `HttpOnly`, filtro e regras de rota
  - Acceptance: `SecurityFilterChain` nega por padrão; `/api/auth/**` e leitura de produtos públicos; `signin` define cookie `HttpOnly; SameSite=Lax`; `signout` limpa; `GET /api/auth/me`; 401/403 corretos
  - Verify: `MockMvc`: sem cookie → 401; `USER` em rota admin → 403; cookie sem `Secure` em localhost
  - Files: `infra/security/*`, `AuthController.java`, `WebConfig.java`
- [x] T2.5 Remover rota pública de criação de usuário e `userId` exposto
  - Acceptance: `POST /api/user/create` removido; `GET /api/user/get` só para o próprio usuário ou `ADMIN`; admin inicial criado por variável de ambiente ou seed
  - Verify: testes de autorização
  - Files: `UserController.java`, `application.properties`, `docker-compose.yml`, `.env.example`
- [x] T2.6 Segredos fora do repositório
  - Acceptance: `application.properties` sem segredo real; JWT/senha por variável de ambiente; instruções para rotacionar
  - Verify: `grep` no repositório; subida via Compose continua funcionando
  - Files: `application.properties`, `.env.example`, `specs/03-SECURITY.md`

## F3 — `feature/catalog-search` (RF-CAT-1..8)

- [ ] T3.1 Migration de produto: categoria, preço `NUMERIC(10,2)`, nome maior
  - Acceptance: enum `HOT_COFFEE | COLD_COFFEE | SNACK` obrigatório; seed categorizado; `Product.price` vira `BigDecimal`, `@Positive`
  - Verify: teste de repositório com Testcontainers; JSON de preço com 2 casas
  - Files: `V6__*.sql`, `Product.java`, DTOs de produto, `ProductCrudAdapter.java`
- [ ] T3.2 Busca e paginação
  - Acceptance: `GET /api/product?q=&category=&page=&size=` com `size` ≤ 50 e total de páginas; busca sem diferenciar maiúsculas
  - Verify: testes de repositório e de controller com combinações de filtros
  - Files: `ProductRepository.java`, `ProductCrudUseCase.java`, `ProductCrudImpl.java`, `ProductController.java`
- [ ] T3.3 CRUD de admin
  - Acceptance: criar, editar e remover só por `ADMIN`; nome único; erros de validação com campos
  - Verify: `MockMvc` com `USER` → 403 e `ADMIN` → 2xx
  - Files: `ProductController.java`, `ProductCrudImpl.java`, DTOs
- [ ] T3.4 Tabela e endpoints de imagem
  - Acceptance: `product_image` (BYTEA); upload multipart por `ADMIN` (≤ 2 MB, JPEG/PNG/WebP por assinatura); `GET /api/product/{id}/image` com `Content-Type`, `ETag`, `nosniff`; imagem some junto com o produto
  - Verify: testes: ida e volta dos mesmos bytes; PNG falso rejeitado; > 2 MB rejeitado
  - Files: `V7__*.sql`, `ProductImage.java`, gateway, adapter, `ProductImageController.java`
- [ ] T3.5 Carga das imagens do seed na primeira subida
  - Acceptance: com banco vazio, os três produtos têm imagem; segunda subida não duplica nem sobrescreve imagem enviada pelo admin
  - Verify: teste de integração rodando o runner duas vezes
  - Files: `resources/seed/images/*`, `infra/seed/SeedImageLoader.java`, `V2` (remover `image_url` do seed)
- [ ] T3.6 Mais vendidos
  - Acceptance: `/get/best` continua funcionando com o novo modelo
  - Verify: teste de controller
  - Files: `ProductController.java`, `ProductRepository.java`

## F4 — `feature/cart-per-user` (RF-CART-1..4, SEC-7, SEC-8)

- [ ] T4.1 Migration do carrinho com `quantity` e `UNIQUE (user_id, product_id)`
  - Acceptance: migração de linhas antigas agrupa por usuário e produto; `@NotNull` nos UUIDs
  - Verify: teste de migration com dados prévios
  - Files: `V8__*.sql`, `Cart.java`, `CartCrudGateway.java`, `CartCrudAdapter.java`, `CartRepository.java`
- [ ] T4.2 Carrinho do usuário autenticado
  - Acceptance: `userId` sai de parâmetros e corpo; add/decrease/delete/get operam no usuário do token; produto inexistente → 404
  - Verify: teste com dois usuários: A não vê nem altera o carrinho de B
  - Files: `CartController.java`, `ManageCartUseCase.java`, `ManageCartImpl.java`, DTOs
- [ ] T4.3 Totais do carrinho
  - Acceptance: resposta com itens agrupados, `subtotal`, `tax` (3%), `total`; `HALF_UP` com 2 casas
  - Verify: teste unitário do cálculo
  - Files: `ManageCartImpl.java`, `CartResponseDTO.java`, classe de cálculo de preço (reutilizada em F5)
- [ ] T4.4 CORS com `PATCH`/`PUT` e origem configurável
  - Acceptance: `decrease` funciona do navegador; origem vem de configuração
  - Verify: teste de preflight
  - Files: `WebConfig.java`, `docker-compose.yml`

## F5 — `feature/orders-checkout` (RF-ORD-1..7)

- [ ] T5.1 Migration e entidades de pedido
  - Acceptance: `orders`, `order_item` (nome e preço unitário gravados), endereço, tipo, totais, status
  - Verify: teste de repositório
  - Files: `V9__*.sql`, `Order.java`, `OrderItem.java`, `OrderStatus.java`, repositório
- [ ] T5.2 Criar pedido a partir do carrinho
  - Acceptance: preço recalculado no servidor; `DELIVERY` exige endereço e soma R$ 20,00; carrinho vazio → 400; carrinho limpo na mesma transação
  - Verify: exemplo do PRD (total R$ 32,88); teste de rollback
  - Files: `OrderUseCase.java`, `OrderImpl.java`, `OrderCrudGateway.java`, adapter, `OrderController.java`
- [ ] T5.3 Consulta e transições de status
  - Acceptance: cliente lista e vê só os próprios (404 nos de outros); transições inválidas → 409; `ADMIN` muda status
  - Verify: testes da máquina de estados e de autorização
  - Files: `OrderImpl.java`, `OrderController.java`, `OrderStatus.java`
- [ ] T5.4 Cancelamento pelo cliente
  - Acceptance: cancela em `PENDING` e `CONFIRMED`; em `READY` ou depois → 409
  - Verify: teste por status
  - Files: `OrderImpl.java`, `OrderController.java`

## F6 — `feature/frontend-api-integration`

- [x] T6.0 Redesign visual (`feature/frontend-design`)
  - Acceptance: tokens claro/escuro com contraste AA, Geist única, interface pt-BR, R$, layout responsivo sem rolagem horizontal, estados de carregando/vazio/erro (ver `specs/06-DESIGN.md`)
  - Verify: `bun run build`; capturas em 390px e 1280px nos dois temas; grep sem travessões, `h-screen` ou fontes antigas
  - Files: `index.css`, `index.html`, `main.jsx`, `components/site/*`, `pages/*`

- [ ] T6.1 Cliente HTTP único
  - Acceptance: um `apiClient` com `VITE_API_URL`, `credentials: 'include'` e tratamento do envelope; sem URL fixa nos serviços
  - Verify: `bun run lint`; teste com `fetch` simulado
  - Files: `src/api/apiClient.js`, `productService.js`, `userService.js`
- [ ] T6.2 Auth real
  - Acceptance: `signInUser`/`signUpUser` chamam a API; `AuthContext` usa `/me`; sem `document.cookie` nem `alert`; guarda de rota em `/checkout`, `/profile`, `/orders`
  - Verify: fluxo manual cadastro → login → logout; teste de componente do formulário
  - Files: `AuthContext.jsx`, `userService.js`, `SignIn`, `SignUp`, `App.jsx`
- [ ] T6.3 Catálogo com busca, categoria, paginação e imagem do banco
  - Acceptance: `Products` com campo de busca, filtro `HOT_COFFEE`/`COLD_COFFEE`/`SNACK`, paginação; imagens via `/api/product/{id}/image`; `mockData` deixa de ser usado
  - Verify: fluxo manual; `bun run lint`
  - Files: `Products.jsx`, `ProductCard.jsx`, `BestSellers.jsx`, `productService.js`
- [ ] T6.4 Carrinho e checkout
  - Acceptance: adicionar, diminuir, remover; resumo com `R$`, imposto, entrega; escolha de retirada/entrega e endereço; finalizar cria o pedido
  - Verify: fluxo manual de ponta a ponta
  - Files: `Checkout.jsx`, `ProductCartCard.jsx`, `productService.js`
- [ ] T6.5 Pedidos e administração
  - Acceptance: tela de pedidos do cliente com cancelamento; telas de admin para produtos (com upload) e status de pedidos
  - Verify: fluxo manual com os dois papéis
  - Files: novas páginas em `src/pages`, `App.jsx`
- [ ] T6.6 Testes de frontend
  - Acceptance: Vitest + Testing Library; testes de formulário de login e do carrinho
  - Verify: `bun run test`
  - Files: `package.json`, `vite.config.js`, testes
