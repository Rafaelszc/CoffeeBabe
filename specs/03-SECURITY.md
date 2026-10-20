# 03 — Segurança

Status: rascunho para revisão (revisão 2)

## 1. Achados no código atual

Ordenados por gravidade. Cada um vira tarefa na feature indicada.

| # | Achado | Gravidade | Feature |
|---|---|---|---|
| S1 | Nenhuma rota exige autenticação; não há Spring Security. | Crítica | `identity` |
| S2 | `POST /api/user/create` é público e cria sempre `ADMIN`. | Crítica | `identity` |
| S3 | Endpoints do carrinho aceitam `userId` do cliente (IDOR): qualquer pessoa altera o carrinho de outra. `GET /api/user/get?id=` expõe dados de qualquer usuário. | Crítica | `identity`, `cart` |
| S4 | `POST /api/product/create` é público. | Alta | `catalog` |
| S5 | Segredo JWT e senha do banco (`postgres/123`) estão commitados em `application.properties`. | Alta | `identity` |
| S6 | Senha não é hasheada (a entidade limita a 30 caracteres, incompatível com BCrypt). | Alta | `identity` |
| S7 | `UserController.getUserById` usa `@RequestMapping` sem método (aceita qualquer verbo). | Média | `identity` |
| S8 | Frontend guarda "sessão" em cookie legível por JavaScript e faz `alert` com dados de login e cadastro. | Média | `identity` |
| S9 | Sem validação em controllers (`@Valid` ausente), e `@NotBlank` em `UUID`/`double` não valida nada. | Média | todas |
| S10 | `RestExceptionAdvice` devolve a classe da exceção no campo `errors`, o que vaza detalhe interno. | Baixa | todas |
| S11 | `UserNotAuthorizedException` responde "User not found!". Mensagens de login podem permitir enumerar contas. | Baixa | `identity` |

O segredo JWT atual deve ser considerado comprometido, porque já está no histórico do Git. Ele precisa ser rotacionado antes de qualquer deploy, e o valor antigo nunca pode ser usado fora de desenvolvimento local.

Rotação: gere um novo valor com `openssl rand -base64 32`, coloque em `JWT_SECRET` no `.env` e reinicie o backend. Todas as sessões existentes deixam de valer e os usuários precisam entrar de novo.

## 2. Modelo de ameaça (resumo)

| Ativo | Ameaça | Controle |
|---|---|---|
| Conta do cliente | Força bruta, vazamento de senha | BCrypt, limite de tentativas, mensagem genérica de erro |
| Sessão | Roubo por XSS | JWT em cookie `HttpOnly`, CSP restritiva no frontend |
| Sessão | CSRF (cookie é enviado automaticamente) | `SameSite=Lax` ou `Strict`, validação de `Origin` nas rotas que alteram estado, CORS com origem explícita |
| Carrinho e pedidos | Acesso a dados de outro usuário (IDOR) | Identidade só do token; consultas sempre filtradas por `userId` |
| Catálogo e pedidos (admin) | Escalada de privilégio | Papel vem do token; `hasRole('ADMIN')` nas rotas de escrita; cadastro público nunca cria `ADMIN` |
| Preço do pedido | Adulteração pelo cliente | Preço e total calculados no servidor a partir do banco |
| Dados de entrega | Exposição de endereço | Só o dono e `ADMIN` leem; não registrar endereço em log |
| Upload | Arquivo malicioso disfarçado de imagem, arquivo gigante | Validação por assinatura, limite de 2 MB, só `ADMIN`, sem SVG |
| Banco | Injeção SQL | Só consultas parametrizadas (Spring Data); busca por texto sem concatenar SQL |

## 3. Requisitos

### Autenticação e sessão
- SEC-1: Spring Security com `SecurityFilterChain` que nega por padrão; rotas públicas listadas explicitamente (`/api/auth/**`, leitura de `/api/product/**`).
- SEC-2: senhas com BCrypt (custo ≥ 10). Política mínima: 8 caracteres. A validação de tamanho fica no DTO de entrada, não na entidade que guarda o hash.
- SEC-3: JWT assinado (HS256 com segredo de pelo menos 256 bits, ou RS256), contendo `sub` (id do usuário), `role`, `iat`, `exp`. Expiração curta (hoje 3 horas é o valor configurado; revisar).
- SEC-4: cookie `HttpOnly`, `SameSite=Lax`, `Secure` apenas quando houver HTTPS (propriedade configurável; em localhost fica desligado), `Path=/`, `Max-Age` igual à expiração do token.
- SEC-5: login e cadastro com limite de tentativas por IP e por e-mail.
- SEC-6: login devolve a mesma mensagem e o mesmo status para e-mail inexistente e senha errada.

### Autorização
- SEC-7: `userId` nunca vem de parâmetro, caminho ou corpo em rotas de carrinho, pedido ou perfil.
- SEC-8: recurso de outro usuário responde `404`.
- SEC-9: escrita em catálogo e mudança de status de pedido exigem `ADMIN`.

### Entrada e saída
- SEC-10: `@Valid` em todos os `@RequestBody`; `@Positive` em preço, `@NotNull` em UUIDs; tamanhos máximos coerentes com as colunas.
- SEC-11: `size` de paginação limitado a 50.
- SEC-12: erros não expõem nome de classe, stack trace nem SQL. O campo `errors` carrega mensagens de campo.
- SEC-13: upload de imagem só por `ADMIN`; limite de 2 MB (também configurado em `spring.servlet.multipart`); tipo validado pela assinatura do conteúdo (JPEG, PNG, WebP), nunca pelo nome ou pelo `Content-Type` enviado; a imagem é servida com `Content-Type` fixado pelo servidor e `X-Content-Type-Options: nosniff`; o nome do arquivo enviado nunca é usado como caminho. SVG não é aceito (pode conter script). O frontend nunca renderiza HTML vindo do backend.

### Configuração e infraestrutura
- SEC-14: segredos (`JWT_SECRET`, `POSTGRES_PASSWORD`, `DB_URL`, `DB_USER`) vêm de variáveis de ambiente (no Compose, de um arquivo `.env` local não versionado, com `.env.example` versionado); `application.properties` mantém só placeholders com valores de desenvolvimento óbvios (`${JWT_SECRET:dev-only-change-me}`) ou um perfil `dev` separado. `.env` do frontend e do backend ficam no `.gitignore`.
- SEC-15: CORS com origem vinda de configuração (não fixa em código), `allowCredentials(true)` somente para essa origem, métodos incluindo `PATCH` e `PUT` quando usados.
- SEC-16: cabeçalhos de segurança básicos (`X-Content-Type-Options`, `Referrer-Policy`). CSP e HSTS ficam como melhoria opcional, já que não há deploy.
- SEC-17: HTTPS não é exigido (só localhost). Se o projeto for hospedado um dia, torna-se obrigatório e `Secure` e HSTS passam a valer.
- SEC-18: logs sem senha, token nem endereço.
- SEC-19: dependências verificadas (`./mvnw dependency-check` ou Dependabot) e `bun audit` no frontend antes de cada release.

## 4. Critérios de aceite de segurança por módulo

- `identity`: requisição sem cookie a `/api/cart/*` retorna `401`; usuário `USER` em `POST /api/product` retorna `403`; resposta de `/api/auth/me` não contém senha; `application.properties` não contém segredo real.
- `cart`: teste com dois usuários prova que A não acessa o carrinho de B.
- `orders`: teste prova que o total gravado ignora qualquer preço enviado no corpo; A não vê pedido de B.
- Geral: nenhum teste ou resposta expõe stack trace.

## 5. Perguntas em aberto

1. Haverá recuperação de senha por e-mail? (adiciona serviço de e-mail e token de uso único)
2. Haverá refresh token, ou sessão simples que expira e exige novo login?
3. (Resolvida) Sistema só em localhost, sem dados reais de clientes: LGPD fora de escopo, mas os dados de endereço continuam tratados como se fossem reais.
4. (Resolvida) Mesma máquina, portas diferentes: `SameSite=Lax` basta.
