# CoffeeBabe

Documentação oficial do projeto **CoffeeBabe**.

## 1) Visão geral do software

O CoffeeBabe é uma aplicação de e-commerce para cafeteria, com foco em:

- **Catálogo de produtos** (cafés, doces e itens relacionados).
- **Autenticação de usuários**.
- **Gestão de carrinho** (adicionar, diminuir quantidade e remover itens).

### Arquitetura em alto nível

O sistema está dividido em três camadas principais:

1. **Frontend (React + Vite)**
   - Renderiza a interface da loja.
   - Faz chamadas HTTP para a API do backend.
2. **Backend (Spring Boot)**
   - Expõe endpoints REST para autenticação, usuários, produtos e carrinho.
   - Contém regras de negócio (use cases) e camada de persistência.
3. **Banco de dados (PostgreSQL)**
   - Armazena usuários, produtos, carrinho e demais tabelas.
   - Schema versionado com Flyway.

---

## 2) Tecnologias utilizadas

### Backend

- Java 25
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Flyway (migrations)
- PostgreSQL
- Lombok
- Maven

As dependências do backend estão em `backend/pom.xml`.

### Frontend

- React 19
- Vite 8
- React Router DOM
- Tailwind CSS 4
- Radix UI
- React Hook Form + Zod
- ESLint

As dependências do frontend estão em `frontend/package.json`.

---

## 3) Organização do backend

Controllers expostos:

- `AuthController` → base `/api/auth`
- `UserController` → base `/api/user`
- `ProductController` → base `/api/product`
- `CartController` → base `/api/cart`

Formato padrão de resposta da API:

```json
{
  "success": true,
  "message": "Mensagem da operação",
  "data": {},
  "errors": []
}
```

> Estrutura definida por `ApiResponseEntity<T>`.

---

## 4) Rotas do backend (com request/response)

> **Base URL local (exemplo):** `http://localhost:8080`

## 4.1 Auth (`/api/auth`)

### `POST /api/auth/signin`
Realiza login e retorna token.

**Request body**
```json
{
  "email": "user@email.com",
  "password": "123456"
}
```

**Response de sucesso (`202 Accepted`)**
```json
{
  "success": true,
  "message": "SignIn success",
  "data": {
    "token": "jwt-token-aqui"
  },
  "errors": []
}
```

---

### `POST /api/auth/signup`
Cria um novo usuário.

**Request body**
```json
{
  "username": "john",
  "email": "john@email.com",
  "password": "123456"
}
```

**Response de sucesso (`201 Created`)**
```json
{
  "success": true,
  "message": "SignUp success",
  "data": null,
  "errors": []
}
```

---

## 4.2 User (`/api/user`)

### `GET /api/user/get?id={uuid}`
Busca usuário por ID.

**Query param**
- `id` (UUID)

**Response de sucesso (`302 Found`)**
```json
{
  "success": true,
  "message": "User from id: 00000000-0000-0000-0000-000000000000",
  "data": {
    "username": "john",
    "email": "john@email.com",
    "role": "ADMIN"
  },
  "errors": []
}
```

---

### `POST /api/user/create`
Cria usuário via endpoint administrativo (role definida como `ADMIN` no controller).

**Request body**
```json
{
  "username": "adminUser",
  "email": "admin@email.com",
  "password": "123456"
}
```

**Response de sucesso (`201 Created`)**
```json
{
  "success": true,
  "message": "adminUser created.",
  "data": null,
  "errors": []
}
```

---

## 4.3 Product (`/api/product`)

### `POST /api/product/create`
Publica/cadastra produto.

**Request body**
```json
{
  "name": "Cappuccino",
  "imageUrl": "https://site.com/cappuccino.jpg",
  "description": "Café com leite vaporizado",
  "price": 15.9
}
```

**Response de sucesso (`201 Created`)**
```json
{
  "success": true,
  "message": "Cappuccino published successfully",
  "data": null,
  "errors": []
}
```

---

### `GET /api/product/get/all`
Lista todos os produtos.

**Response de sucesso (`302 Found`)**
```json
{
  "success": true,
  "message": "Get all products successfully",
  "data": [
    {
      "id": "11111111-1111-1111-1111-111111111111",
      "name": "Cappuccino",
      "imageUrl": "https://site.com/cappuccino.jpg",
      "description": "Café com leite vaporizado",
      "price": 15.9
    }
  ],
  "errors": []
}
```

---

### `GET /api/product/get?id={uuid}`
Busca produto por ID.

**Query param**
- `id` (UUID)

**Response de sucesso (`302 Found`)**
```json
{
  "success": true,
  "message": "Product find successfully",
  "data": {
    "id": "11111111-1111-1111-1111-111111111111",
    "name": "Cappuccino",
    "imageUrl": "https://site.com/cappuccino.jpg",
    "description": "Café com leite vaporizado",
    "price": 15.9
  },
  "errors": []
}
```

---

## 4.4 Cart (`/api/cart`)

### `POST /api/cart/add`
Adiciona produto ao carrinho do usuário.

**Request body**
```json
{
  "productId": "11111111-1111-1111-1111-111111111111",
  "userId": "22222222-2222-2222-2222-222222222222"
}
```

**Response de sucesso (`202 Accepted`)**
```json
{
  "success": true,
  "message": "11111111-1111-1111-1111-111111111111 added to cart",
  "data": null,
  "errors": []
}
```

---

### `PATCH /api/cart/decrease?productId={uuid}&userId={uuid}`
Diminui a quantidade do produto no carrinho.

**Response de sucesso (`202 Accepted`)**
```json
{
  "success": true,
  "message": "11111111-1111-1111-1111-111111111111 decreased from cart",
  "data": null,
  "errors": []
}
```

---

### `DELETE /api/cart/delete?productId={uuid}&userId={uuid}`
Remove produto do carrinho.

**Response de sucesso (`202 Accepted`)**
```json
{
  "success": true,
  "message": "11111111-1111-1111-1111-111111111111 removed from cart",
  "data": null,
  "errors": []
}
```

---

### `GET /api/cart/get?userId={uuid}`
Retorna itens do carrinho do usuário.

**Response de sucesso (`302 Found`)**
```json
{
  "success": true,
  "message": "All items returned successfully",
  "data": [
    {
      "id": "11111111-1111-1111-1111-111111111111",
      "name": "Cappuccino",
      "imageUrl": "https://site.com/cappuccino.jpg",
      "description": "Café com leite vaporizado",
      "price": 15.9
    }
  ],
  "errors": []
}
```

---

## 5) Respostas de erro (padrão)

O backend possui tratamento centralizado de exceções via `RestExceptionAdvice`.

Exemplo de erro:

```json
{
  "success": false,
  "message": "User not found!",
  "data": null,
  "errors": [
    "class com.coffeebabe.backend.core.exceptions.UserNotFoundException"
  ]
}
```

Possíveis status no tratamento:

- `400 Bad Request`
- `401 Unauthorized`
- `404 Not Found`

---

## 6) Configuração

Arquivo principal: `backend/src/main/resources/application.properties`

Propriedades relevantes:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `spring.flyway.baseline-on-migrate`
- `spring.flyway.schemas`
- `security.jwt.token`
- `security.jwt.token.expirationHours`

---

## 7) Como executar localmente

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

---

## 8) Migrations de banco

As migrations SQL do backend estão em:

- `backend/src/main/resources/db/migration/V1__create-users-table.sql`
- `backend/src/main/resources/db/migration/V2__create-table-products.sql`
- `backend/src/main/resources/db/migration/V3__create-table-best-sellers.sql`
- `backend/src/main/resources/db/migration/V4__create-table-cart.sql`
