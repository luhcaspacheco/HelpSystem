# Help System — Dicionário de Dados (Sprint 1)

Modelo do banco `help_system` (MySQL 8.x / MariaDB 10.x). Cada tabela abaixo indica quais requisitos funcionais ela atende. O script executável está em **Sprint 1 - Banco de Dados (MySQL).sql**.

## Visão geral dos relacionamentos

- Um **departamento** tem muitos **usuários**.
- Um **usuário** cria muitas **solicitações** (é o autor) e escreve muitas **respostas**.
- Uma **categoria** classifica muitas **solicitações**.
- Uma **solicitação** recebe muitas **respostas**.
- Uma **solicitação** gera muitas **notificações** para o seu autor.

## Tabelas

### departamento
Setor do colaborador. Apoia o cadastro (RF01).

| Campo | Tipo | Regras |
|---|---|---|
| id | INT PK AI | — |
| nome | VARCHAR(80) | obrigatório, único |

### usuario
Colaborador do sistema. Atende **RF01** (cadastro) e **RF02.1** (tipo de acesso).

| Campo | Tipo | Regras |
|---|---|---|
| id | INT PK AI | — |
| nome | VARCHAR(120) | obrigatório |
| email | VARCHAR(150) | obrigatório, **único** (RF01.3 / RF01.6) |
| senha_hash | VARCHAR(255) | obrigatório — guarda o **hash BCrypt**, nunca a senha pura (RF01.4) |
| tipo | ENUM('ADMIN','COMUM') | padrão COMUM (RF02.1) |
| departamento_id | INT FK → departamento | opcional; SET NULL se o departamento for apagado |
| ativo | BOOLEAN | padrão TRUE (permite desativar sem apagar) |
| data_criacao | DATETIME | padrão data/hora atual |

### categoria
Assunto da solicitação. Atende **RF06**.

| Campo | Tipo | Regras |
|---|---|---|
| id | INT PK AI | — |
| nome | VARCHAR(80) | obrigatório, **único** (RF06.2 — sem duplicidade) |

### solicitacao
A "pergunta" criada pelo colaborador. Atende **RF03** (criação), **RF05** (listagem/filtros/ordenação) e **RF07** (resolução).

| Campo | Tipo | Regras |
|---|---|---|
| id | INT PK AI | — |
| titulo | VARCHAR(150) | obrigatório |
| descricao | TEXT | obrigatório |
| autor_id | INT FK → usuario | obrigatório; RESTRICT (não apaga usuário com solicitação) |
| categoria_id | INT FK → categoria | opcional; SET NULL |
| prioridade | ENUM('BAIXA','MEDIA','ALTA') | padrão MEDIA — usado na ordenação (RF05.3) |
| status | ENUM('ABERTA','RESPONDIDA','RESOLVIDA') | padrão ABERTA (RF07.1) |
| data_criacao | DATETIME | padrão data/hora atual |
| data_resolucao | DATETIME | preenchido ao virar RESOLVIDA |

Índices para os filtros e a ordenação (RF05.2 / RF05.3): `status`, `categoria_id`, `autor_id`, `prioridade`, `data_criacao`.

### resposta
Resposta de outro colaborador. Atende **RF04.2**.

| Campo | Tipo | Regras |
|---|---|---|
| id | INT PK AI | — |
| solicitacao_id | INT FK → solicitacao | obrigatório; CASCADE (apaga junto com a solicitação) |
| autor_id | INT FK → usuario | obrigatório; RESTRICT |
| texto | TEXT | obrigatório |
| data_criacao | DATETIME | padrão data/hora atual |

### notificacao
Aviso ao autor quando sua solicitação é respondida. Atende **RF04.3** (prioridade baixa — opcional no MVP).

| Campo | Tipo | Regras |
|---|---|---|
| id | INT PK AI | — |
| usuario_id | INT FK → usuario | destinatário; CASCADE |
| solicitacao_id | INT FK → solicitacao | CASCADE |
| mensagem | VARCHAR(255) | obrigatório |
| lida | BOOLEAN | padrão FALSE |
| data_criacao | DATETIME | padrão data/hora atual |

## Regras que ficam na aplicação (não no banco)

Alguns requisitos não são resolvidos por constraint SQL e precisam ser validados no código Java:

- **RF01.4 — Hash da senha:** o banco só guarda a string; gerar o hash com jBCrypt antes do INSERT (`BCrypt.hashpw(senha, BCrypt.gensalt())`).
- **RF02.2 / RF02.4 — Controle de acesso:** checar `usuario.tipo == ADMIN` antes de liberar ações administrativas.
- **RF07.2 — Só o autor resolve:** antes do `UPDATE ... SET status='RESOLVIDA'`, conferir que o usuário logado é o `autor_id` da solicitação.
- **Fluxo de status:** ao inserir uma resposta, a aplicação pode atualizar a solicitação para `RESPONDIDA` e criar a notificação.

## Seed incluído no script

- Departamentos: TI, RH, Financeiro, Comercial, Operações.
- Categorias: Sistemas, Acesso e Login, Recursos Humanos, Financeiro, Infraestrutura, Outros.
- Um usuário **Administrador** (`admin@helpsystem.local`) com hash placeholder — **substituir pelo hash real** gerado na aplicação.

## Definição de Pronto (DoD) desta tarefa

- [ ] Script roda sem erros no MySQL/MariaDB da equipe.
- [ ] As 6 tabelas e as chaves estrangeiras existem no banco.
- [ ] Backend consegue conectar (JDBC) e inserir/ler um usuário de teste.
- [ ] Hash placeholder do admin substituído pelo real.
