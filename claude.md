# CLAUDE.md — Dashboard Financeiro

> Arquivo de contexto completo do projeto para uso em geração de código assistida por IA.
> Contém decisões de arquitetura, modelo de dados, regras de negócio e convenções.

---

## 1. Visão Geral do Projeto

Dashboard financeiro pessoal onde o usuário pode:

- Registrar transações de entrada (INCOME) e saída (EXPENSE)
- Visualizar balanço mensal (total entradas, total saídas, saldo)
- Detalhar gastos por categoria com visualização gráfica
- Filtrar e ordenar a lista de transações
- Gerenciar transações recorrentes (ex: salário mensal, aluguel)

---

## 2. Stack Tecnológica

### Back-end

- **Linguagem:** Java 21
- **Framework:** Spring Boot 4.0.x
- **Spring Framework:** 7.x
- **Gerenciador de dependências:** Maven
- **Persistência:** Spring Data JPA + Hibernate
- **Banco de dados:** PostgreSQL
- **Migrations:** Flyway
- **Autenticação:** Spring Security + OAuth2 Resource Server (validação de JWT do Clerk via JWKS)
- **Testes:** JUnit 5 + Mockito + Spring Boot Test + Testcontainers

### Front-end

- **Framework:** Next.js 14+ (App Router)
- **Linguagem:** TypeScript
- **Autenticação:** Clerk (SDK oficial)
- **Gerenciamento de estado do servidor:** TanStack Query (React Query)
- **Componentes UI:** shadcn/ui
- **Gráficos:** Recharts
- **HTTP Client:** Axios ou Fetch nativo
- **Testes:** Jest + Testing Library + MSW (Mock Service Worker)
- **Testes E2E:** Playwright

### Infraestrutura

- **Front-end:** Vercel (gratuito)
- **Back-end:** Railway ou Render (free tier)
- **Banco de dados:** Neon (PostgreSQL serverless, free tier)

---

## 3. Autenticação

### Fluxo

1. Usuário autentica via Clerk no Next.js
2. Clerk emite um JWT assinado
3. Next.js inclui o JWT no header `Authorization: Bearer <token>` em cada requisição
4. Spring Boot valida o JWT consultando o JWKS público do Clerk
5. O `clerkId` extraído do token é usado para identificar o usuário no banco

### Configuração Spring Boot

```properties
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://YOUR_CLERK_DOMAIN/.well-known/jwks.json
```

### Criação de usuário local (lazy creation)

Na primeira requisição autenticada, o back-end verifica se o `clerk_id` já existe na tabela `users`.
Se não existir, cria o registro automaticamente antes de processar a requisição.

---

## 4. Modelo de Dados

### Tabela: `users`

```sql
CREATE TABLE users (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clerk_id   VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
```

### Tabela: `categories`

```sql
CREATE TYPE transaction_type AS ENUM ('INCOME', 'EXPENSE');

CREATE TABLE categories (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(100) NOT NULL UNIQUE,
    type       transaction_type NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
```

**Valores fixos (populados via migration):**

```sql
-- INCOME
INSERT INTO categories (name, type) VALUES
  ('Salário', 'INCOME'),
  ('Freelance', 'INCOME'),
  ('Investimentos', 'INCOME'),
  ('Presente', 'INCOME'),
  ('Outros', 'INCOME');

-- EXPENSE
INSERT INTO categories (name, type) VALUES
  ('Alimentação', 'EXPENSE'),
  ('Transporte', 'EXPENSE'),
  ('Moradia', 'EXPENSE'),
  ('Saúde', 'EXPENSE'),
  ('Lazer', 'EXPENSE'),
  ('Educação', 'EXPENSE'),
  ('Outros', 'EXPENSE');
```

### Tabela: `recurring_transactions`

```sql
CREATE TYPE recurrence_type AS ENUM ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY');

CREATE TABLE recurring_transactions (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL REFERENCES users(id),
    category_id      UUID NOT NULL REFERENCES categories(id),
    type             transaction_type NOT NULL,
    amount           DECIMAL(9,2) NOT NULL CHECK (amount > 0 AND amount <= 99000),
    description      VARCHAR(100),
    recurrence_type  recurrence_type NOT NULL,
    recurrence_day   SMALLINT NOT NULL,
    start_date       DATE NOT NULL,
    end_date         DATE,
    is_active        BOOLEAN NOT NULL DEFAULT true,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now()
);
```

### Tabela: `transactions`

```sql
CREATE TABLE transactions (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                   UUID NOT NULL REFERENCES users(id),
    category_id               UUID NOT NULL REFERENCES categories(id),
    recurring_transaction_id  UUID REFERENCES recurring_transactions(id) ON DELETE SET NULL,
    type                      transaction_type NOT NULL,
    amount                    DECIMAL(9,2) NOT NULL CHECK (amount > 0 AND amount <= 99000),
    description               VARCHAR(100),
    date                      DATE NOT NULL,
    created_at                TIMESTAMP NOT NULL DEFAULT now(),
    updated_at                TIMESTAMP NOT NULL DEFAULT now()
);
```

---

## 5. Regras de Negócio

### Transação

- **RN-001:** `amount` deve ser > 0 e ≤ 99.000,00
- **RN-002:** `amount` é sempre armazenado como positivo; o sinal é derivado do `type`
- **RN-003:** `date` não pode ser futura (`date ≤ hoje`)
- **RN-004:** `description` é opcional, máximo 100 caracteres
- **RN-005:** `category_id` é obrigatório
- **RN-006:** `category.type` deve ser igual a `transaction.type`
- **RN-007:** Todos os campos são editáveis após a criação
- **RN-008:** Exclusão é permanente e irreversível
- **RN-009:** Usuário acessa apenas suas próprias transações

### Balanço e Dashboard

- **RN-010:** Balanço calculado por mês, baseado no campo `date` da transação
- **RN-011:** `balanço = Σ INCOME - Σ EXPENSE` do mês selecionado
- **RN-012:** Detalhamento agrupa transações por `category` dentro do mês
- **RN-013:** Saldo negativo deve ser sinalizado visualmente (cor vermelha)
- **RN-014:** Mês corrente é o período padrão ao abrir o dashboard

### Filtros e Ordenação

- **RN-015:** Filtro de mês/ano é obrigatório; usa mês corrente se não informado
- **RN-016:** Filtros de `type`, `category` e `amount` são opcionais e combináveis (AND)
- **RN-017:** Filtro de valor aceita os operadores: `EQUAL`, `GREATER_THAN`, `LESS_THAN`, `BETWEEN`
- **RN-018:** Ordenação configurável por `date` e `amount` nas direções `ASC` e `DESC`
- **RN-019:** Ordenação padrão: `date DESC`

### Recorrência

- **RN-020:** `recurring_transaction` é o template gerador — não aparece diretamente na lista
- **RN-021:** Instâncias geradas são registros em `transactions` vinculados via `recurring_transaction_id`
- **RN-022:** Ao editar um template: instâncias com `date >= hoje` são deletadas e regeradas; instâncias com `date < hoje` são preservadas
- **RN-023:** Ao desativar (`is_active = false`): nenhuma nova instância é gerada
- **RN-024:** Instâncias passadas permanecem mesmo após desativação do template
- **RN-025:** Ao excluir um template: instâncias futuras são removidas; instâncias passadas permanecem com `recurring_transaction_id = null`

### Usuário

- **RN-026:** Registro local criado automaticamente no primeiro acesso autenticado (lazy creation)
- **RN-027:** `clerk_id` é o vínculo entre a autenticação (Clerk) e os dados no banco
- **RN-028:** Todas as entidades são isoladas por `user_id`; endpoints protegidos retornam 401 sem token válido

---

## 6. Endpoints da API (Spring Boot)

### Autenticação

Todos os endpoints exigem `Authorization: Bearer <token>` válido emitido pelo Clerk.
Respostas para requisições não autenticadas: **HTTP 401**.
Respostas para acesso a recursos de outro usuário: **HTTP 404** (não expõe existência do recurso).

### Transactions

| Método   | Endpoint             | Descrição                                       |
| -------- | -------------------- | ----------------------------------------------- |
| `GET`    | `/transactions`      | Lista transações do mês com filtros e ordenação |
| `POST`   | `/transactions`      | Cria nova transação                             |
| `PUT`    | `/transactions/{id}` | Atualiza todos os campos de uma transação       |
| `DELETE` | `/transactions/{id}` | Exclui permanentemente uma transação            |

**Query params de GET /transactions:**
| Param | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `month` | int | Não (default: mês atual) | Mês de referência (1-12) |
| `year` | int | Não (default: ano atual) | Ano de referência |
| `type` | INCOME\|EXPENSE | Não | Filtro por tipo |
| `categoryId` | UUID | Não | Filtro por categoria |
| `amountOperator` | EQUAL\|GREATER_THAN\|LESS_THAN\|BETWEEN | Não | Operador do filtro de valor |
| `amount` | decimal | Não | Valor de referência |
| `amountMax` | decimal | Não | Valor máximo (somente para BETWEEN) |
| `orderBy` | date\|amount | Não (default: date) | Campo de ordenação |
| `direction` | ASC\|DESC | Não (default: DESC) | Direção da ordenação |

### Dashboard

| Método | Endpoint                 | Descrição                                                   |
| ------ | ------------------------ | ----------------------------------------------------------- |
| `GET`  | `/dashboard/summary`     | Retorna balanço do mês (totalIncome, totalExpense, balance) |
| `GET`  | `/dashboard/by-category` | Retorna totais agrupados por categoria no mês               |

### Recurring Transactions

| Método   | Endpoint                                  | Descrição                                    |
| -------- | ----------------------------------------- | -------------------------------------------- |
| `GET`    | `/recurring-transactions`                 | Lista templates recorrentes do usuário       |
| `POST`   | `/recurring-transactions`                 | Cria novo template e gera instâncias         |
| `PUT`    | `/recurring-transactions/{id}`            | Edita template e regera instâncias futuras   |
| `PATCH`  | `/recurring-transactions/{id}/deactivate` | Desativa template (para de gerar instâncias) |
| `DELETE` | `/recurring-transactions/{id}`            | Exclui template e remove instâncias futuras  |

### Categories

| Método | Endpoint      | Descrição                                                    |
| ------ | ------------- | ------------------------------------------------------------ |
| `GET`  | `/categories` | Lista todas as categorias (opcionalmente filtradas por type) |

---

## 7. Estrutura de Pacotes — Spring Boot

```
src/main/java/com/financedashboard/
├── config/
│   ├── SecurityConfig.java          # Spring Security + OAuth2 Resource Server
│   └── SchedulerConfig.java         # Spring Scheduler para recorrências
├── controller/
│   ├── TransactionController.java
│   ├── DashboardController.java
│   ├── RecurringTransactionController.java
│   └── CategoryController.java
├── service/
│   ├── TransactionService.java
│   ├── DashboardService.java
│   ├── RecurringTransactionService.java
│   ├── CategoryService.java
│   └── UserService.java             # Lazy creation do usuário local
├── repository/
│   ├── TransactionRepository.java
│   ├── RecurringTransactionRepository.java
│   ├── CategoryRepository.java
│   └── UserRepository.java
├── domain/
│   ├── entity/
│   │   ├── Transaction.java
│   │   ├── RecurringTransaction.java
│   │   ├── Category.java
│   │   └── User.java
│   └── enums/
│       ├── TransactionType.java     # INCOME, EXPENSE
│       └── RecurrenceType.java      # DAILY, WEEKLY, MONTHLY, YEARLY
├── dto/
│   ├── request/
│   │   ├── CreateTransactionRequest.java
│   │   ├── UpdateTransactionRequest.java
│   │   ├── CreateRecurringTransactionRequest.java
│   │   └── TransactionFilterRequest.java
│   └── response/
│       ├── TransactionResponse.java
│       ├── DashboardSummaryResponse.java
│       ├── CategorySummaryResponse.java
│       └── RecurringTransactionResponse.java
├── exception/
│   ├── GlobalExceptionHandler.java  # @ControllerAdvice
│   ├── BusinessException.java
│   └── ResourceNotFoundException.java
└── scheduler/
    └── RecurringTransactionScheduler.java
```

---

## 8. Estrutura de Páginas — Next.js

```
src/
├── app/
│   ├── (auth)/
│   │   └── sign-in/
│   │       └── page.tsx             # Página de login (Clerk)
│   ├── (dashboard)/
│   │   ├── layout.tsx               # Layout protegido (verifica sessão)
│   │   ├── page.tsx                 # Dashboard principal (balanço + lista)
│   │   ├── transactions/
│   │   │   └── page.tsx             # Lista completa com filtros
│   │   └── recurring/
│   │       └── page.tsx             # Gestão de recorrências
│   └── layout.tsx                   # Root layout (ClerkProvider)
├── components/
│   ├── dashboard/
│   │   ├── BalanceSummary.tsx
│   │   └── CategoryChart.tsx        # Gráfico de gastos por categoria
│   ├── transactions/
│   │   ├── TransactionList.tsx
│   │   ├── TransactionItem.tsx
│   │   ├── TransactionForm.tsx
│   │   ├── TransactionFilters.tsx
│   │   └── DeleteConfirmModal.tsx
│   └── recurring/
│       ├── RecurringList.tsx
│       └── RecurringForm.tsx
├── hooks/
│   ├── useTransactions.ts           # TanStack Query hooks
│   ├── useDashboard.ts
│   └── useRecurring.ts
├── services/
│   └── api.ts                       # Configuração do Axios + interceptor JWT
├── types/
│   └── index.ts                     # Tipos TypeScript globais
└── middleware.ts                     # Clerk middleware (proteção de rotas)
```

---

## 9. Convenções de Código

### Back-end (Java)

- Nomenclatura: `camelCase` para variáveis e métodos, `PascalCase` para classes
- Entidades não expõem diretamente nos controllers — sempre usar DTOs
- Services recebem o `clerkId` via `SecurityContextHolder` e resolvem o `userId` internamente
- Toda exceção de negócio usa `BusinessException`; recurso não encontrado usa `ResourceNotFoundException`
- `GlobalExceptionHandler` mapeia exceções para respostas HTTP padronizadas
- Respostas de erro seguem o padrão: `{ "error": "mensagem", "status": 400 }`

### Front-end (TypeScript)

- Nomenclatura: `camelCase` para variáveis, `PascalCase` para componentes e tipos
- Componentes são funcionais com hooks
- Chamadas à API centralizadas em `services/api.ts`
- Interceptor do Axios injeta o token do Clerk automaticamente em toda requisição
- Formulários utilizam `react-hook-form` com validação via `zod`

---

## 10. Testes

### Back-end

- **Unitários:** JUnit 5 + Mockito — testam `Service` com repositórios mockados
- **Integração:** `@SpringBootTest` + Testcontainers (PostgreSQL real) — testam controllers e repositories
- **Segurança:** `SecurityMockMvcRequestPostProcessors.jwt()` para simular tokens do Clerk

### Front-end

- **Unitários:** Jest + Testing Library — testam componentes isolados
- **Integração:** Jest + MSW — interceptam chamadas HTTP e simulam a API
- **E2E:** Playwright — testam fluxos completos em browser real

### Arquivo de referência

Todos os 97 casos de teste estão documentados em `TESTS.md`.

---

## 11. Decisões Arquiteturais Relevantes

| Decisão              | Escolha                                      | Motivo                                                       |
| -------------------- | -------------------------------------------- | ------------------------------------------------------------ |
| Banco de dados       | PostgreSQL                                   | Open-source, robusto, suportado pelo Neon                    |
| Autenticação         | Clerk + JWT                                  | Segurança gerenciada, integração simples com Spring via JWKS |
| Usuário local        | Tabela `users` com `clerk_id`                | Integridade referencial e independência do Clerk no banco    |
| Categorias           | Fixas no sistema                             | Simplicidade para MVP, sem entidade extra por usuário        |
| Recorrência          | Entidade separada (`recurring_transactions`) | Rastreabilidade, histórico preservado, edição segura         |
| Edição de recorrente | Regera instâncias futuras, preserva passado  | Histórico imutável, comportamento intuitivo                  |
| Exclusão             | Hard delete                                  | Simplicidade; confirmação na UI previne exclusão acidental   |
| Filtro de período    | Mês obrigatório                              | Evita queries sem escopo definido; mês atual como padrão     |
| Retorno 404 vs 403   | 404 para recurso de outro usuário            | Não expõe existência de recursos de outros usuários          |
| Spring Boot          | 4.0.x                                        | Versão estável mais recente, requer Java 21 (já adotado)     |
