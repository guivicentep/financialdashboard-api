# Casos de Teste — Dashboard Financeiro

## Legenda

- **CT** = Caso de Teste
- **RN** = Regra de Negócio relacionada
- **Tipo** = Unitário (U) | Integração (I) | E2E (E)

---

## Back-end — Spring Boot

### Módulo: Transação

#### CT-001 — Criar transação de entrada válida

- **Tipo:** U
- **RN:** RN-001, RN-002, RN-005, RN-006
- **Dado:** amount=500, type=INCOME, category=Salário, date=hoje, description=null
- **Esperado:** Transação criada com sucesso, amount armazenado como positivo

#### CT-002 — Criar transação de saída válida

- **Tipo:** U
- **RN:** RN-001, RN-002, RN-005, RN-006
- **Dado:** amount=150, type=EXPENSE, category=Alimentação, date=hoje
- **Esperado:** Transação criada com sucesso, amount armazenado como positivo

#### CT-003 — Rejeitar transação com amount = 0

- **Tipo:** U
- **RN:** RN-001
- **Dado:** amount=0, type=INCOME, category=Salário
- **Esperado:** Exceção de validação lançada — "O valor deve ser maior que zero"

#### CT-004 — Rejeitar transação com amount negativo

- **Tipo:** U
- **RN:** RN-001, RN-002
- **Dado:** amount=-100, type=EXPENSE, category=Alimentação
- **Esperado:** Exceção de validação lançada — "O valor deve ser maior que zero"

#### CT-005 — Rejeitar transação com amount acima do limite

- **Tipo:** U
- **RN:** RN-001
- **Dado:** amount=99001, type=INCOME, category=Salário
- **Esperado:** Exceção de validação lançada — "O valor máximo permitido é R$ 99.000,00"

#### CT-006 — Aceitar transação com amount no limite máximo

- **Tipo:** U
- **RN:** RN-001
- **Dado:** amount=99000, type=INCOME, category=Salário
- **Esperado:** Transação criada com sucesso

#### CT-007 — Rejeitar transação com data futura

- **Tipo:** U
- **RN:** RN-003
- **Dado:** amount=100, type=INCOME, category=Salário, date=amanhã
- **Esperado:** Exceção de validação lançada — "A data não pode ser futura"

#### CT-008 — Aceitar transação com data passada

- **Tipo:** U
- **RN:** RN-003
- **Dado:** amount=100, type=INCOME, category=Salário, date=primeiro dia do mês atual
- **Esperado:** Transação criada com sucesso

#### CT-009 — Aceitar transação sem descrição

- **Tipo:** U
- **RN:** RN-004
- **Dado:** amount=100, type=INCOME, category=Salário, description=null
- **Esperado:** Transação criada com sucesso

#### CT-010 — Rejeitar descrição acima de 100 caracteres

- **Tipo:** U
- **RN:** RN-004
- **Dado:** description= string com 101 caracteres
- **Esperado:** Exceção de validação lançada — "A descrição deve ter no máximo 100 caracteres"

#### CT-011 — Aceitar descrição com exatamente 100 caracteres

- **Tipo:** U
- **RN:** RN-004
- **Dado:** description= string com 100 caracteres
- **Esperado:** Transação criada com sucesso

#### CT-012 — Rejeitar transação sem category_id

- **Tipo:** U
- **RN:** RN-005
- **Dado:** amount=100, type=INCOME, category_id=null
- **Esperado:** Exceção de validação lançada — "A categoria é obrigatória"

#### CT-013 — Rejeitar categoria incompatível com tipo da transação

- **Tipo:** U
- **RN:** RN-006
- **Dado:** type=INCOME, category=Alimentação (tipo EXPENSE)
- **Esperado:** Exceção de negócio lançada — "A categoria não é compatível com o tipo da transação"

#### CT-014 — Rejeitar categoria incompatível com tipo de saída

- **Tipo:** U
- **RN:** RN-006
- **Dado:** type=EXPENSE, category=Salário (tipo INCOME)
- **Esperado:** Exceção de negócio lançada — "A categoria não é compatível com o tipo da transação"

#### CT-015 — Editar todos os campos de uma transação

- **Tipo:** U
- **RN:** RN-007
- **Dado:** Transação existente, novos valores para todos os campos válidos
- **Esperado:** Transação atualizada com os novos valores, updated_at alterado

#### CT-016 — Editar transação com valores inválidos

- **Tipo:** U
- **RN:** RN-007, RN-001
- **Dado:** Transação existente, amount=0
- **Esperado:** Exceção de validação lançada, transação não alterada

#### CT-017 — Excluir transação existente

- **Tipo:** U
- **RN:** RN-008
- **Dado:** Transação existente do usuário autenticado
- **Esperado:** Transação removida permanentemente, não recuperável

#### CT-018 — Usuário não acessa transações de outro usuário

- **Tipo:** U
- **RN:** RN-009
- **Dado:** Usuário A tenta buscar transação criada pelo Usuário B
- **Esperado:** Exceção de autorização — transação não encontrada para esse usuário

---

### Módulo: Balanço e Dashboard

#### CT-019 — Calcular balanço do mês com entradas e saídas

- **Tipo:** U
- **RN:** RN-010, RN-011
- **Dado:** INCOME=3000, EXPENSE=1200, mês=atual
- **Esperado:** balanço = 1800, totalIncome=3000, totalExpense=1200

#### CT-020 — Calcular balanço com resultado negativo

- **Tipo:** U
- **RN:** RN-011, RN-013
- **Dado:** INCOME=500, EXPENSE=1200, mês=atual
- **Esperado:** balanço = -700 (negativo sinalizado)

#### CT-021 — Calcular balanço com apenas entradas

- **Tipo:** U
- **RN:** RN-011
- **Dado:** INCOME=2000, nenhum EXPENSE, mês=atual
- **Esperado:** balanço = 2000, totalExpense=0

#### CT-022 — Calcular balanço com apenas saídas

- **Tipo:** U
- **RN:** RN-011
- **Dado:** nenhum INCOME, EXPENSE=800, mês=atual
- **Esperado:** balanço = -800, totalIncome=0

#### CT-023 — Calcular balanço de mês sem transações

- **Tipo:** U
- **RN:** RN-011
- **Dado:** nenhuma transação no mês selecionado
- **Esperado:** balanço = 0, totalIncome=0, totalExpense=0

#### CT-024 — Balanço não inclui transações de outros meses

- **Tipo:** U
- **RN:** RN-010
- **Dado:** Transações em janeiro e fevereiro, filtro em janeiro
- **Esperado:** Balanço reflete apenas transações de janeiro

#### CT-025 — Agrupar gastos por categoria no mês

- **Tipo:** U
- **RN:** RN-012
- **Dado:** 3 transações EXPENSE em Alimentação (100, 200, 150), 1 em Transporte (80)
- **Esperado:** Alimentação=450, Transporte=80

#### CT-026 — Agrupar entradas por categoria no mês

- **Tipo:** U
- **RN:** RN-012
- **Dado:** 2 transações INCOME em Salário (3000, 500), 1 em Freelance (1000)
- **Esperado:** Salário=3500, Freelance=1000

#### CT-027 — Período padrão do dashboard é o mês corrente

- **Tipo:** U
- **RN:** RN-014
- **Dado:** Requisição sem parâmetro de mês/ano
- **Esperado:** Retorna dados do mês e ano atuais

---

### Módulo: Filtros e Ordenação

#### CT-028 — Filtrar transações por mês obrigatório

- **Tipo:** U
- **RN:** RN-015
- **Dado:** Requisição sem parâmetro month/year
- **Esperado:** Usa mês corrente como padrão

#### CT-029 — Filtrar por tipo INCOME

- **Tipo:** U
- **RN:** RN-015
- **Dado:** month=atual, type=INCOME
- **Esperado:** Retorna apenas transações do tipo INCOME do mês

#### CT-030 — Filtrar por tipo EXPENSE

- **Tipo:** U
- **RN:** RN-015
- **Dado:** month=atual, type=EXPENSE
- **Esperado:** Retorna apenas transações do tipo EXPENSE do mês

#### CT-031 — Filtrar por categoria

- **Tipo:** U
- **RN:** RN-016
- **Dado:** month=atual, category=Alimentação
- **Esperado:** Retorna apenas transações da categoria Alimentação no mês

#### CT-032 — Filtrar por valor igual

- **Tipo:** U
- **RN:** RN-016, RN-017
- **Dado:** month=atual, amountOperator=EQUAL, amount=100
- **Esperado:** Retorna apenas transações com amount=100

#### CT-033 — Filtrar por valor maior que

- **Tipo:** U
- **RN:** RN-017
- **Dado:** month=atual, amountOperator=GREATER_THAN, amount=500
- **Esperado:** Retorna transações com amount > 500

#### CT-034 — Filtrar por valor menor que

- **Tipo:** U
- **RN:** RN-017
- **Dado:** month=atual, amountOperator=LESS_THAN, amount=200
- **Esperado:** Retorna transações com amount < 200

#### CT-035 — Filtrar por intervalo de valor

- **Tipo:** U
- **RN:** RN-017
- **Dado:** month=atual, amountOperator=BETWEEN, amountMin=100, amountMax=500
- **Esperado:** Retorna transações com 100 ≤ amount ≤ 500

#### CT-036 — Filtros combinados (tipo + categoria + valor)

- **Tipo:** U
- **RN:** RN-016
- **Dado:** type=EXPENSE, category=Alimentação, amountOperator=LESS_THAN, amount=200
- **Esperado:** Retorna apenas transações que satisfazem todos os filtros (AND)

#### CT-037 — Ordenação por data ascendente

- **Tipo:** U
- **RN:** RN-018
- **Dado:** month=atual, orderBy=date, direction=ASC
- **Esperado:** Transações ordenadas da mais antiga para a mais recente

#### CT-038 — Ordenação por data descendente (padrão)

- **Tipo:** U
- **RN:** RN-018, RN-019
- **Dado:** month=atual, sem parâmetro de ordenação
- **Esperado:** Transações ordenadas da mais recente para a mais antiga

#### CT-039 — Ordenação por valor ascendente

- **Tipo:** U
- **RN:** RN-018
- **Dado:** month=atual, orderBy=amount, direction=ASC
- **Esperado:** Transações ordenadas do menor para o maior valor

#### CT-040 — Ordenação por valor descendente

- **Tipo:** U
- **RN:** RN-018
- **Dado:** month=atual, orderBy=amount, direction=DESC
- **Esperado:** Transações ordenadas do maior para o menor valor

---

### Módulo: Recorrência

#### CT-041 — Criar transação recorrente mensal válida

- **Tipo:** U
- **RN:** RN-020
- **Dado:** amount=1500, type=INCOME, category=Salário, recurrence_type=MONTHLY, recurrence_day=5, start_date=01/01/2026
- **Esperado:** Template criado, instâncias geradas para os meses a partir do start_date

#### CT-042 — Instância gerada vincula ao template

- **Tipo:** U
- **RN:** RN-021
- **Dado:** Template recorrente criado
- **Esperado:** Cada instância gerada possui recurring_transaction_id preenchido

#### CT-043 — Editar template preserva histórico passado

- **Tipo:** U
- **RN:** RN-022
- **Dado:** Template com instâncias em jan, fev, mar (passado) e abr, mai (futuro), editar amount
- **Esperado:** Instâncias de jan, fev, mar inalteradas; abr e mai regeradas com novo amount

#### CT-044 — Editar template regera apenas instâncias futuras

- **Tipo:** U
- **RN:** RN-022
- **Dado:** Template editado hoje
- **Esperado:** Instâncias com date >= hoje deletadas e regeradas; date < hoje intocadas

#### CT-045 — Desativar template para de gerar instâncias

- **Tipo:** U
- **RN:** RN-023
- **Dado:** Template ativo desativado (is_active=false)
- **Esperado:** Nenhuma nova instância gerada após desativação

#### CT-046 — Desativar template preserva instâncias passadas

- **Tipo:** U
- **RN:** RN-024
- **Dado:** Template desativado com instâncias passadas
- **Esperado:** Instâncias passadas permanecem no banco e visíveis na lista

#### CT-047 — Excluir template remove apenas instâncias futuras

- **Tipo:** U
- **RN:** RN-025
- **Dado:** Template com instâncias passadas e futuras
- **Esperado:** Instâncias futuras removidas; passadas preservadas com recurring_transaction_id=null

#### CT-048 — Editar instância individual não afeta template

- **Tipo:** U
- **RN:** RN-022
- **Dado:** Instância gerada por recorrência editada individualmente
- **Esperado:** Apenas aquela instância alterada, template e demais instâncias inalterados

---

### Módulo: Autenticação e Segurança

#### CT-049 — Requisição sem token retorna 401

- **Tipo:** I
- **RN:** RN-028
- **Dado:** GET /transactions sem header Authorization
- **Esperado:** HTTP 401 Unauthorized

#### CT-050 — Requisição com token expirado retorna 401

- **Tipo:** I
- **RN:** RN-028
- **Dado:** GET /transactions com JWT expirado
- **Esperado:** HTTP 401 Unauthorized

#### CT-051 — Requisição com token malformado retorna 401

- **Tipo:** I
- **RN:** RN-028
- **Dado:** GET /transactions com Authorization: Bearer token_invalido
- **Esperado:** HTTP 401 Unauthorized

#### CT-052 — Requisição com token válido retorna 200

- **Tipo:** I
- **RN:** RN-028
- **Dado:** GET /transactions com JWT válido do Clerk
- **Esperado:** HTTP 200 OK

#### CT-053 — Usuário A não acessa transação do Usuário B via GET

- **Tipo:** I
- **RN:** RN-009, RN-028
- **Dado:** GET /transactions/{id} com id pertencente ao Usuário B, autenticado como Usuário A
- **Esperado:** HTTP 404 Not Found (não expõe que o recurso existe)

#### CT-054 — Usuário A não deleta transação do Usuário B

- **Tipo:** I
- **RN:** RN-009
- **Dado:** DELETE /transactions/{id} com id pertencente ao Usuário B
- **Esperado:** HTTP 404 Not Found

#### CT-055 — Usuário A não edita transação do Usuário B

- **Tipo:** I
- **RN:** RN-009
- **Dado:** PUT /transactions/{id} com id pertencente ao Usuário B
- **Esperado:** HTTP 404 Not Found

---

### Módulo: Usuário

#### CT-056 — Criar usuário local no primeiro acesso

- **Tipo:** I
- **RN:** RN-026
- **Dado:** JWT válido do Clerk com clerk_id inexistente no banco
- **Esperado:** Registro criado na tabela users com o clerk_id extraído do token

#### CT-057 — Não duplicar usuário em acessos subsequentes

- **Tipo:** I
- **RN:** RN-026, RN-027
- **Dado:** JWT válido do Clerk com clerk_id já existente no banco
- **Esperado:** Nenhum novo registro criado, fluxo segue normalmente

---

### Módulo: Integração com Banco (Testcontainers)

#### CT-058 — Salvar e recuperar transação no banco

- **Tipo:** I
- **RN:** RN-001, RN-002
- **Dado:** Transação válida salva via repository
- **Esperado:** Transação recuperada com todos os campos íntegros

#### CT-059 — Busca por user_id retorna apenas transações do usuário

- **Tipo:** I
- **RN:** RN-009
- **Dado:** Dois usuários com transações distintas no banco
- **Esperado:** Query por user_id retorna apenas registros do respectivo usuário

#### CT-060 — Busca filtrada por mês/ano via query

- **Tipo:** I
- **RN:** RN-010
- **Dado:** Transações em meses distintos no banco
- **Esperado:** Query com month=3, year=2026 retorna apenas transações de março/2026

#### CT-061 — Agrupamento por categoria via query

- **Tipo:** I
- **RN:** RN-012
- **Dado:** Múltiplas transações de categorias distintas
- **Esperado:** Retorna soma agrupada por category_id corretamente

---

## Front-end — Next.js

### Módulo: Componente TransactionForm

#### CT-062 — Renderizar formulário de nova transação

- **Tipo:** U
- **RN:** RN-001, RN-005
- **Dado:** Componente renderizado sem props
- **Esperado:** Campos de tipo, categoria, valor, data e descrição visíveis

#### CT-063 — Exibir campo categoria compatível com tipo selecionado

- **Tipo:** U
- **RN:** RN-006
- **Dado:** Usuário seleciona type=EXPENSE
- **Esperado:** Dropdown de categoria exibe apenas categorias do tipo EXPENSE

#### CT-064 — Exibir categorias de INCOME ao selecionar entrada

- **Tipo:** U
- **RN:** RN-006
- **Dado:** Usuário seleciona type=INCOME
- **Esperado:** Dropdown de categoria exibe apenas categorias do tipo INCOME

#### CT-065 — Erro ao submeter formulário com valor zero

- **Tipo:** U
- **RN:** RN-001
- **Dado:** amount=0, demais campos válidos
- **Esperado:** Mensagem de erro exibida — campo não enviado à API

#### CT-066 — Erro ao submeter formulário sem categoria

- **Tipo:** U
- **RN:** RN-005
- **Dado:** category_id=null, demais campos válidos
- **Esperado:** Mensagem de erro exibida — campo não enviado à API

#### CT-067 — Erro ao submeter descrição com mais de 100 caracteres

- **Tipo:** U
- **RN:** RN-004
- **Dado:** description com 101 caracteres
- **Esperado:** Mensagem de erro exibida abaixo do campo

#### CT-068 — Submissão válida chama callback com dados corretos

- **Tipo:** U
- **RN:** RN-001
- **Dado:** Todos os campos válidos preenchidos
- **Esperado:** Função onSubmit chamada com os dados do formulário

---

### Módulo: Componente BalanceSummary

#### CT-069 — Exibir balanço positivo

- **Tipo:** U
- **RN:** RN-011
- **Dado:** totalIncome=3000, totalExpense=1200
- **Esperado:** Balanço exibe R$ 1.800,00 com indicador positivo (verde)

#### CT-070 — Exibir balanço negativo com sinalização visual

- **Tipo:** U
- **RN:** RN-013
- **Dado:** totalIncome=500, totalExpense=1200
- **Esperado:** Balanço exibe -R$ 700,00 com indicador negativo (vermelho)

#### CT-071 — Exibir balanço zero

- **Tipo:** U
- **RN:** RN-011
- **Dado:** totalIncome=0, totalExpense=0
- **Esperado:** Balanço exibe R$ 0,00 sem indicador de positivo/negativo

#### CT-072 — Exibir total de entradas e saídas separadamente

- **Tipo:** U
- **RN:** RN-011
- **Dado:** totalIncome=3000, totalExpense=1200
- **Esperado:** Componente exibe os três valores: entradas, saídas e saldo

---

### Módulo: Componente TransactionList

#### CT-073 — Renderizar lista de transações

- **Tipo:** U
- **RN:** RN-019
- **Dado:** Array com 5 transações mockadas
- **Esperado:** 5 itens renderizados na lista

#### CT-074 — Exibir mensagem quando lista está vazia

- **Tipo:** U
- **Dado:** Array vazio
- **Esperado:** Mensagem "Nenhuma transação encontrada" exibida

#### CT-075 — Exibir valor formatado em moeda brasileira

- **Tipo:** U
- **Dado:** amount=1500.50
- **Esperado:** Exibido como "R$ 1.500,50"

#### CT-076 — Exibir cor diferente para entrada e saída

- **Tipo:** U
- **RN:** RN-013
- **Dado:** Uma transação INCOME e uma EXPENSE
- **Esperado:** INCOME com cor verde, EXPENSE com cor vermelha

#### CT-077 — Aplicar filtro de tipo na lista

- **Tipo:** U
- **RN:** RN-016
- **Dado:** Lista mista, filtro type=INCOME aplicado
- **Esperado:** Apenas transações INCOME visíveis

#### CT-078 — Exibir estado de loading durante busca

- **Tipo:** U
- **Dado:** Estado isLoading=true
- **Esperado:** Skeleton ou spinner exibido no lugar da lista

#### CT-079 — Exibir mensagem de erro quando API falha

- **Tipo:** U
- **Dado:** Estado isError=true
- **Esperado:** Mensagem de erro amigável exibida

---

### Módulo: Integração Front-end com API (MSW)

#### CT-080 — Carregar dashboard com dados reais mockados

- **Tipo:** I
- **Dado:** MSW intercepta GET /transactions e retorna lista mockada
- **Esperado:** Dashboard exibe balanço e lista corretamente

#### CT-081 — Criar transação via formulário atualiza lista

- **Tipo:** I
- **RN:** RN-001
- **Dado:** Formulário preenchido, MSW intercepta POST /transactions com 201
- **Esperado:** Lista atualizada com a nova transação após criação

#### CT-082 — Exibir feedback de erro quando criação falha

- **Tipo:** I
- **Dado:** MSW intercepta POST /transactions e retorna 400
- **Esperado:** Mensagem de erro exibida ao usuário

#### CT-083 — Aplicar filtro dispara nova requisição com query params corretos

- **Tipo:** I
- **RN:** RN-016
- **Dado:** Usuário aplica filtro category=Alimentação
- **Esperado:** Request enviado com ?category=Alimentação, lista atualizada

#### CT-084 — Redirecionar para login se não autenticado

- **Tipo:** I
- **RN:** RN-028
- **Dado:** Usuário sem sessão acessa rota protegida
- **Esperado:** Redirecionamento para página de login do Clerk

#### CT-085 — Página protegida não renderiza sem sessão válida

- **Tipo:** I
- **RN:** RN-028
- **Dado:** Sessão ausente ou expirada
- **Esperado:** Conteúdo protegido não é renderizado antes do redirecionamento

---

## E2E — Playwright

#### CT-086 — Fluxo: acesso sem autenticação redireciona para login

- **Tipo:** E
- **RN:** RN-028
- **Dado:** Usuário não autenticado acessa "/"
- **Esperado:** Redirecionado para página de login

#### CT-087 — Fluxo: login com Clerk e acesso ao dashboard

- **Tipo:** E
- **RN:** RN-026, RN-027
- **Dado:** Usuário realiza login via Clerk
- **Esperado:** Redirecionado para dashboard, balanço do mês atual exibido

#### CT-088 — Fluxo: criar transação de entrada e ver balanço atualizar

- **Tipo:** E
- **RN:** RN-001, RN-011
- **Dado:** Usuário logado cria INCOME de R$ 1.000,00 categoria Salário
- **Esperado:** Balanço atualiza para +R$ 1.000,00, transação aparece na lista

#### CT-089 — Fluxo: criar transação de saída e ver balanço atualizar

- **Tipo:** E
- **RN:** RN-001, RN-011
- **Dado:** Usuário logado cria EXPENSE de R$ 150,00 categoria Alimentação
- **Esperado:** Balanço reduz R$ 150,00, transação aparece na lista com cor vermelha

#### CT-090 — Fluxo: filtrar lista por tipo INCOME

- **Tipo:** E
- **RN:** RN-016
- **Dado:** Lista com entradas e saídas, usuário aplica filtro INCOME
- **Esperado:** Apenas entradas visíveis na lista

#### CT-091 — Fluxo: filtrar lista por categoria

- **Tipo:** E
- **RN:** RN-016
- **Dado:** Transações de categorias distintas, filtro category=Alimentação
- **Esperado:** Apenas transações de Alimentação visíveis

#### CT-092 — Fluxo: excluir transação com confirmação

- **Tipo:** E
- **RN:** RN-008
- **Dado:** Usuário clica em excluir uma transação
- **Esperado:** Modal de confirmação exibido; após confirmar, transação removida da lista

#### CT-093 — Fluxo: excluir transação cancelando na confirmação

- **Tipo:** E
- **RN:** RN-008
- **Dado:** Usuário clica em excluir e cancela no modal
- **Esperado:** Transação permanece na lista

#### CT-094 — Fluxo: editar transação existente

- **Tipo:** E
- **RN:** RN-007
- **Dado:** Usuário edita amount de uma transação existente
- **Esperado:** Lista e balanço refletem o valor atualizado

#### CT-095 — Fluxo: isolamento entre usuários

- **Tipo:** E
- **RN:** RN-009, RN-028
- **Dado:** Usuário A cria transações, Usuário B faz login
- **Esperado:** Usuário B não visualiza nenhuma transação do Usuário A

#### CT-096 — Fluxo: criar transação recorrente e verificar instâncias

- **Tipo:** E
- **RN:** RN-020, RN-021
- **Dado:** Usuário cria recorrência mensal de R$ 1.500,00 categoria Salário
- **Esperado:** Instâncias geradas aparecem nos meses correspondentes

#### CT-097 — Fluxo: desativar recorrência e verificar que não gera novas instâncias

- **Tipo:** E
- **RN:** RN-023, RN-024
- **Dado:** Usuário desativa template recorrente
- **Esperado:** Instâncias passadas permanecem, nenhuma futura é gerada

---

## Sumário de Cobertura

| Módulo             | Unitários       | Integração      | E2E                             | Total  |
| ------------------ | --------------- | --------------- | ------------------------------- | ------ |
| Transação (back)   | CT-001 a CT-018 | CT-058 a CT-061 | CT-088, CT-089, CT-092 a CT-094 | 26     |
| Balanço (back)     | CT-019 a CT-027 | —               | CT-088, CT-089                  | 11     |
| Filtros (back)     | CT-028 a CT-040 | —               | CT-090, CT-091                  | 15     |
| Recorrência (back) | CT-041 a CT-048 | —               | CT-096, CT-097                  | 10     |
| Segurança (back)   | —               | CT-049 a CT-055 | CT-086, CT-087, CT-095          | 10     |
| Usuário (back)     | —               | CT-056, CT-057  | CT-087                          | 3      |
| Formulário (front) | CT-062 a CT-068 | —               | CT-088, CT-089                  | 9      |
| Balanço (front)    | CT-069 a CT-072 | —               | CT-088, CT-089                  | 6      |
| Lista (front)      | CT-073 a CT-079 | —               | CT-090, CT-091                  | 9      |
| Integração (front) | —               | CT-080 a CT-085 | —                               | 6      |
| **Total**          | **57**          | **18**          | **12**                          | **97** |
