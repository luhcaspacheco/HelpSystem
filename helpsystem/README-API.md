# HelpSystem — API REST (Spring Boot)

API que o front React consome. Reaproveita as camadas `model`, `dao` e `service`;
por cima delas há os controllers REST em `com.helpsystem.web`.

## Pré-requisitos

- Java 17+ e Maven
- MySQL com o banco `help_system` criado (script `Sprint 1 - Banco de Dados (MySQL).sql`)
- Variavel de ambiente `DB_PASSWORD` com a senha do MySQL. Opcionalmente, use `DB_USERNAME` se o usuario nao for `root`.

## Como rodar a API

Na pasta `helpsystem`:

```
mvn spring-boot:run
```

No Windows CMD, antes de rodar a API:

```
set DB_PASSWORD=sua_senha_do_mysql
```

No PowerShell:

```
$env:DB_PASSWORD="sua_senha_do_mysql"
```

Ou rode a classe `com.helpsystem.HelpSystemApplication` pela IDE.
A API sobe em `http://localhost:8080`. Na primeira vez, o Maven baixa o Spring Boot (pode demorar).

## Autenticação (token simples)

1. `POST /api/login` com e-mail e senha devolve um **token**.
2. Nas rotas protegidas, envie o header: `Authorization: Bearer <token>`.
3. `POST /api/logout` invalida o token.

O admin da seed é `admin@helpsystem.local` / `admin123`.

## Endpoints

| Método | Rota | Auth | Descrição | RF |
|---|---|---|---|---|
| GET | `/api/departamentos` | não | Lista departamentos para cadastro | RF01 |
| POST | `/api/usuarios` | não | Cadastro de usuário | RF01 |
| POST | `/api/login` | não | Login, devolve token | RF02 |
| POST | `/api/logout` | token | Invalida o token | RF02 |
| GET | `/api/categorias` | não | Lista categorias | RF06.1 |
| POST | `/api/categorias` | admin | Cria categoria | RF06.2 |
| GET | `/api/solicitacoes` | token | Lista solicitacoes; aceita filtros `status`, `categoriaId`, `autorId` e `ordenarPor` | RF05 |
| GET | `/api/solicitacoes/{id}` | token | Consulta uma solicitacao | RF05 |
| POST | `/api/solicitacoes` | logado | Cria solicitacao | RF03 |
| GET | `/api/solicitacoes/{id}/respostas` | token | Lista respostas da solicitacao | RF04 |
| POST | `/api/solicitacoes/{id}/respostas` | token | Responde uma solicitacao e atualiza status | RF04 |
| PATCH | `/api/solicitacoes/{id}/resolver` | autor | Marca solicitacao como resolvida | RF07 |
| GET | `/api/notificacoes` | token | Lista notificacoes do usuario logado | RF04.3 |
| GET | `/api/notificacoes/nao-lidas/total` | token | Total de notificacoes nao lidas | RF04.3 |
| PATCH | `/api/notificacoes/{id}/lida` | dono | Marca notificacao como lida | RF04.3 |

Respostas seguem o envelope: `{ "sucesso": true/false, "mensagem": "...", "dado": ... }`.

Códigos: `201` criado, `200` ok, `400` validação, `401` login inválido / sem token,
`403` acesso restrito (não-admin), `500` erro interno.

## Exemplos de corpo (JSON)

Cadastro publico cria sempre usuario comum:
```json
{ "nome": "Maria", "email": "maria@empresa.com", "senha": "senha123", "departamentoId": 2 }
```
Login:
```json
{ "email": "admin@helpsystem.local", "senha": "admin123" }
```
Resposta do login:
```json
{ "sucesso": true, "mensagem": "Login efetuado com sucesso!", "dado": { "token": "...", "usuario": { "id": 1, "nome": "Administrador", "email": "admin@helpsystem.local", "tipo": "ADMIN", "admin": true } } }
```
Solicitação:
```json
{ "titulo": "Erro no VPN", "descricao": "Nao conecta", "categoriaId": 1, "prioridade": "ALTA" }
```

## Testar com Postman

Importe o arquivo **`HelpSystem API.postman_collection.json`** (na pasta Help Desk) no Postman.
Rode na ordem: **Login** (salva o token automaticamente) → as demais requisições já usam
`{{token}}` no header. A variável `base_url` aponta para `http://localhost:8080`.

## CORS

Liberado para qualquer origem em `/api/**` (desenvolvimento). Em produção, restrinja para o
domínio do front em `web/WebConfig.java`.
