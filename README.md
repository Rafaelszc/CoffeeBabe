<p align="center">
  <img align src="frontend/public/favicon.svg" alt="Logo do CoffeeBabe" width="96" height="96">
</p>

<h1 align="center">CoffeeBabe</h1>

<p align="center">
  Cardápio online de uma cafeteria: busque, monte o carrinho e peça para retirar ou receber em casa.
</p>

CoffeeBabe é um projeto de portfólio com uma API REST em Spring Boot e uma interface em React. A cafeteria é fictícia e o sistema roda só em localhost. Não há pagamento online: o cliente paga na retirada ou na entrega.

## O que o sistema faz

Quem visita o site pode:

- ver o cardápio, buscar por nome e filtrar por categoria (café quente, café gelado, petiscos);
- ver os mais vendidos, calculados a partir dos pedidos concluídos.

Quem cria uma conta também pode:

- montar o carrinho, com subtotal, imposto de 3% e total calculados pelo servidor;
- finalizar o pedido para retirada na loja ou entrega (taxa fixa de R$ 20,00, com endereço);
- acompanhar os próprios pedidos e cancelar enquanto estiverem `PENDING` ou `CONFIRMED`.

O administrador (`ADMIN`) cadastra, edita e remove produtos, envia a imagem de cada um e muda o status dos pedidos.

| Papel | Acesso |
|---|---|
| Visitante | Catálogo e busca |
| Cliente (`USER`) | Carrinho, pedidos e perfil |
| Administrador (`ADMIN`) | Produtos (com upload de imagem) e status dos pedidos |

## Como rodar

Você só precisa de Docker e Docker Compose. Não é preciso instalar Java, Bun nem Postgres.

```bash
cp .env.example .env        # defina POSTGRES_PASSWORD, JWT_SECRET e REDIS_PASSWORD
docker compose up --build
```

Gere um segredo para o `JWT_SECRET` com `openssl rand -base64 32`. Para criar o primeiro administrador, preencha `ADMIN_EMAIL` e `ADMIN_PASSWORD` no `.env`.

| Serviço | Endereço |
|---|---|
| Frontend | http://localhost:5174 |
| API | http://localhost:8080 |
| Banco (Postgres) | 127.0.0.1:5433 |
| Redis | só na rede interna do Compose |

Para parar, use `docker compose down`. Para apagar também os dados do banco, use `docker compose down -v`. Rode o `down -v` ao trocar para uma branch com migrations diferentes, porque o Flyway falha se o checksum não bater.

## Tecnologias

| Camada | O que usa |
|---|---|
| Backend | Java 25, Spring Boot 4, Spring Security, Spring Data JPA, Bean Validation, Lombok |
| Banco | PostgreSQL com migrations Flyway; as imagens dos produtos ficam no próprio banco |
| Frontend | React 19, Vite, Tailwind CSS v4, shadcn/ui, react-hook-form, zod, react-router |
| Testes | JUnit 5, Mockito e Testcontainers no backend; Vitest e Testing Library no frontend |
| Sessões | Redis, para os refresh tokens |
| Infra | Docker Compose |

## Arquitetura

O backend segue a arquitetura hexagonal: as dependências apontam para dentro, de `adapter` e `infra` para `application` e `core`.

```
backend/src/main/java/com/coffeebabe/backend
├── core/          entidades, casos de uso (portas de entrada), gateways (portas de saída)
├── application/   implementação dos casos de uso
├── adapter/       controllers REST, DTOs e adaptadores de persistência
└── infra/         JPA, segurança, tratamento de erros, seeds e CORS
```

O frontend fica em `frontend/src`. Todas as chamadas à API passam por `api/apiClient.js`, e cada recurso tem um `*Service.js` fino por cima.

## Segurança

- Sessão por JWT (HS256) de 15 minutos em cookie `HttpOnly`; o frontend nunca lê o token.
- Renovação por refresh token rotativo, guardado no Redis só como hash. Reusar um token já trocado revoga todas as sessões da pessoa.
- Rotas negadas por padrão, com regras por papel.
- Cada cliente só enxerga o próprio carrinho e os próprios pedidos; o de outra pessoa responde `404`.
- Senhas com BCrypt e limite de tentativas de login por IP e por e-mail (`429`).
- Upload de imagem validado pela assinatura do arquivo (JPEG, PNG ou WebP, até 2 MB).
- Segredos vêm de variáveis de ambiente, nunca do repositório.

Os detalhes estão em [`specs/03-SECURITY.md`](specs/03-SECURITY.md).

## Desenvolvimento sem Docker

Backend (precisa de Postgres em `localhost:5432/coffeebabe` e Redis em `localhost:6379`):

```bash
cd backend
JWT_SECRET=$(openssl rand -base64 32) ./mvnw spring-boot:run
./mvnw test
```

Frontend:

```bash
cd frontend
bun install
bun run dev
bun run test
```

`BackendApplicationTests` usa Testcontainers e por isso exige Docker.

## Documentação

As decisões e os requisitos do projeto estão em [`specs/`](specs):

- [Visão](specs/00-VISION.md) e [PRD](specs/01-PRD.md)
- [Arquitetura](specs/02-ARCHITECTURE.md) e [decisões (ADRs)](specs/adr)
- [Segurança](specs/03-SECURITY.md)
- [Orquestração com Docker](specs/05-ORCHESTRATION.md)
- [Design da interface](specs/06-DESIGN.md)
- [Fluxo de Git](specs/04-GIT-FLOW.md)

## Licença

Veja o arquivo [LICENSE](LICENSE).
