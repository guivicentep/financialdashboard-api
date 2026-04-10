# Financial Dashboard

API para gestao financeira pessoal, com foco em transacoes, balanco mensal e recorrencias.

## Proposito

Este projeto foi pensado para ajudar usuarios a controlar a vida financeira de forma simples:

- Registrar entradas (`INCOME`) e saidas (`EXPENSE`)
- Consultar balanco mensal (entradas, saidas e saldo)
- Filtrar e ordenar transacoes por periodo, tipo, categoria e valor
- Trabalhar com transacoes recorrentes (ex.: salario, aluguel)
- Manter historico financeiro com isolamento por usuario

## Estado atual do repositorio

- Este repositorio contem **principalmente o backend Spring Boot**.
- A visao completa do produto (incluindo frontend Next.js) esta detalhada em `claude.md`.
- A API atualmente usa `X-User-Id` nos endpoints de dados para escopo de usuario.

## Stack

### Backend (implementado)

- Java 21
- Spring Boot 4.0.5
- Spring Web MVC
- Spring Data JPA + Hibernate
- Spring Security
- OAuth2 Resource Server (dependencia presente)
- Flyway
- PostgreSQL
- Maven (com `mvnw` / `mvnw.cmd`)
- Lombok

### Testes (backend)

- JUnit 5
- Mockito
- Spring Boot Test
- Spring Security Test
- Testcontainers (PostgreSQL)

### Frontend (arquitetura prevista)

Conforme `claude.md`:

- Next.js 14+ (App Router)
- TypeScript
- Clerk
- TanStack Query
- shadcn/ui
- Recharts
- Jest + Testing Library + MSW
- Playwright

## Estrutura do projeto

```text
financialdashboard/
|- claude.md
|- TESTS.md
|- pom.xml
|- docker-compose.yml
|- mvnw
|- mvnw.cmd
|- src/
|  |- main/
|  |  |- java/com/gvp/financialdashboard/
|  |  |  |- config/
|  |  |  |  |- SecurityConfig.java
|  |  |  |- controller/
|  |  |  |  |- CategoryController.java
|  |  |  |  |- DashboardController.java
|  |  |  |  |- RecurringTransactionController.java
|  |  |  |  |- TransactionController.java
|  |  |  |  |- UserController.java
|  |  |  |- domain/
|  |  |  |  |- dto/
|  |  |  |  |- entity/
|  |  |  |  |- enums/
|  |  |  |- exception/
|  |  |  |- repository/
|  |  |  |  |- spec/
|  |  |  |- service/
|  |  |  |  |- impl/
|  |  |  |- FinancialdashboardApplication.java
|  |  |- resources/
|  |     |- application.properties
|  |     |- db/migration/
|  |        |- V1__create_enums.sql
|  |        |- V2__create_users.sql
|  |        |- V3__create_categories.sql
|  |        |- V4__seed_categories.sql
|  |        |- V5__create_recurring_transactions.sql
|  |        |- V6__create_transactions.sql
|  |        |- V7__alter_recurring_transactions_add_ocurrences.sql
|  |- test/java/com/gvp/financialdashboard/
|     |- domain/dto/CreateTransactionRequestValidationTest.java
|     |- service/
|     |  |- BalanceServiceTest.java
|     |  |- DashboardServiceTest.java
|     |  |- RecurrenceGeneratorTest.java
|     |  |- RecurringTransactionServiceTest.java
|     |  |- TransactionFilterServiceTest.java
|     |  |- TransactionServiceTest.java
|     |- FinancialdashboardApplicationTests.java
|     |- TestcontainersConfiguration.java
|     |- TestFinancialdashboardApplication.java
```

## Modelo de dados (alto nivel)

Entidades principais:

- `users`
- `categories`
- `transactions`
- `recurring_transactions`

Migrations em `src/main/resources/db/migration` criam enums, tabelas e seed de categorias.

## API (estado atual)

### Transactions

- `POST /transactions`
- `GET /transactions`
- `PUT /transactions/{id}`
- `DELETE /transactions/{id}`

### Dashboard

- `GET /dashboard/summary`

### Recurring Transactions

- `POST /recurring-transactions`
- `GET /recurring-transactions`
- `PUT /recurring-transactions/{id}`
- `DELETE /recurring-transactions/{id}`

### Categories

- `POST /categories`
- `GET /categories`
- `DELETE /categories/{id}`

### Users

- `POST /users`

## Como rodar localmente

### 1) Subir o PostgreSQL com Docker

```powershell
docker compose up -d
```

### 2) Rodar a aplicacao

```powershell
.\mvnw.cmd spring-boot:run
```

A API deve subir na porta padrao do Spring Boot (`8080`), salvo alteracao local.

## Como rodar testes

```powershell
.\mvnw.cmd test
```

## Configuracao

Arquivo principal: `src/main/resources/application.properties`

Pontos importantes:

- `spring.datasource.url=jdbc:postgresql://localhost:5432/financedashboard`
- `spring.datasource.username=admin`
- `spring.datasource.password=admin`
- `spring.jpa.hibernate.ddl-auto=validate`
- `spring.flyway.enabled=true`

## Regras de negocio e cobertura

- Regras de negocio detalhadas em `claude.md`
- Catalogo de 97 casos de teste em `TESTS.md`


