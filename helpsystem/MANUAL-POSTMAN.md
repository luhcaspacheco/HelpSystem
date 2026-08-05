# Manual de teste da API no Postman

Este guia mostra como testar a API do HelpSystem usando a coleção Postman do projeto.

Arquivo da coleção:

```text
HelpSystem API.postman_collection.json
```

Esse arquivo fica na pasta principal do projeto:

```text
C:\Users\luhca\Desktop\Help Desk
```

## Antes de começar

Confirme se a API está rodando.

No ambiente local, a URL padrão é:

```text
http://localhost:8080
```

No Postman, confira a variável:

```text
base_url
```

Ela deve apontar para o endereço da API.

Valor esperado:

```text
http://localhost:8080
```

## Variáveis da coleção

A coleção usa estas variáveis:

```text
base_url
token
solicitacao_id
notificacao_id
```

O Postman salva algumas automaticamente durante os testes:

- `token`: salvo depois do login.
- `solicitacao_id`: salvo depois de criar uma solicitação.
- `notificacao_id`: salvo depois de listar notificações, se houver alguma.

## Ordem recomendada para testar

Use esta ordem para evitar erro por falta de token ou falta de solicitação criada.

## 1. Listar departamentos

Requisição:

```http
GET {{base_url}}/api/departamentos
```

Resultado esperado:

```text
200 OK
```

Use um dos IDs retornados como `departamentoId` no cadastro de usuário.

## 2. Cadastrar usuário

Requisição:

```http
POST {{base_url}}/api/usuarios
```

Body:

```json
{
  "nome": "Maria Teste",
  "email": "maria.teste@empresa.com",
  "senha": "senha123",
  "tipo": "COMUM",
  "departamentoId": 2
}
```

Resultado esperado:

```text
201 Created
```

Observação:

Se esse e-mail já estiver cadastrado, a API vai retornar erro de e-mail duplicado. Nesse caso, troque o e-mail no body.

## 3. Login

Requisição:

```http
POST {{base_url}}/api/login
```

Body:

```json
{
  "email": "admin@helpsystem.local",
  "senha": "admin123"
}
```

Resultado esperado:

```text
200 OK
```

Resposta esperada:

```json
{
  "sucesso": true,
  "mensagem": "Login efetuado com sucesso!",
  "dado": {
    "token": "...",
    "usuario": {
      "id": 1,
      "nome": "Administrador",
      "email": "admin@helpsystem.local",
      "tipo": "ADMIN",
      "admin": true
    }
  }
}
```

A coleção salva o token automaticamente na variável:

```text
token
```

As próximas rotas usam:

```http
Authorization: Bearer {{token}}
```

## 4. Listar categorias

Requisição:

```http
GET {{base_url}}/api/categorias
```

Resultado esperado:

```text
200 OK
```

Essa rota retorna as categorias usadas na criação da solicitação.

## 5. Criar categoria

Requisição:

```http
POST {{base_url}}/api/categorias
```

Headers:

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

Body:

```json
{
  "nome": "Impressoras"
}
```

Resultado esperado:

```text
201 Created
```

Observações:

- Apenas usuário admin pode criar categoria.
- Se a categoria já existir, a API retorna erro informando duplicidade.

## 6. Criar solicitação

Requisição:

```http
POST {{base_url}}/api/solicitacoes
```

Headers:

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

Body:

```json
{
  "titulo": "Erro ao acessar o VPN",
  "descricao": "Nao conecto no VPN desde ontem.",
  "categoriaId": 1,
  "prioridade": "ALTA"
}
```

Resultado esperado:

```text
201 Created
```

A coleção salva automaticamente o ID da solicitação criada na variável:

```text
solicitacao_id
```

## 7. Listar solicitações

Requisição:

```http
GET {{base_url}}/api/solicitacoes
```

Headers:

```http
Authorization: Bearer {{token}}
```

Resultado esperado:

```text
200 OK
```

Também é possível testar filtros manualmente:

```http
GET {{base_url}}/api/solicitacoes?status=ABERTA
GET {{base_url}}/api/solicitacoes?categoriaId=1
GET {{base_url}}/api/solicitacoes?autorId=2
GET {{base_url}}/api/solicitacoes?status=ABERTA&categoriaId=1
GET {{base_url}}/api/solicitacoes?status=ABERTA&categoriaId=1&autorId=2
GET {{base_url}}/api/solicitacoes?ordenarPor=prioridade
```

Valores aceitos para `ordenarPor`:

```text
data
prioridade
```

## 8. Responder solicitação

Requisição:

```http
POST {{base_url}}/api/solicitacoes/{{solicitacao_id}}/respostas
```

Headers:

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

Body:

```json
{
  "texto": "Resposta de teste enviada pela API."
}
```

Resultado esperado:

```text
201 Created
```

Quando uma solicitação `ABERTA` recebe resposta, o backend muda o status para:

```text
RESPONDIDA
```

Observação importante:

A notificação só é criada quando quem responde é uma pessoa diferente do autor da solicitação.

Se você criar e responder com o mesmo usuário, não haverá notificação para listar.

## 9. Listar respostas

Requisição:

```http
GET {{base_url}}/api/solicitacoes/{{solicitacao_id}}/respostas
```

Headers:

```http
Authorization: Bearer {{token}}
```

Resultado esperado:

```text
200 OK
```

## 10. Resolver solicitação

Requisição:

```http
PATCH {{base_url}}/api/solicitacoes/{{solicitacao_id}}/resolver
```

Headers:

```http
Authorization: Bearer {{token}}
```

Resultado esperado:

```text
200 OK
```

Regra:

Somente o autor da solicitação pode marcar como resolvida.

## 11. Listar notificações

Requisição:

```http
GET {{base_url}}/api/notificacoes
```

Headers:

```http
Authorization: Bearer {{token}}
```

Resultado esperado:

```text
200 OK
```

Também é possível filtrar:

```http
GET {{base_url}}/api/notificacoes?lida=false
GET {{base_url}}/api/notificacoes?lida=true
```

Se houver notificações, a coleção salva o ID da primeira na variável:

```text
notificacao_id
```

## 12. Total de notificações não lidas

Requisição:

```http
GET {{base_url}}/api/notificacoes/nao-lidas/total
```

Headers:

```http
Authorization: Bearer {{token}}
```

Resultado esperado:

```text
200 OK
```

Resposta esperada:

```json
{
  "sucesso": true,
  "mensagem": "Total de notificacoes nao lidas.",
  "dado": {
    "total": 1
  }
}
```

## 13. Marcar notificação como lida

Requisição:

```http
PATCH {{base_url}}/api/notificacoes/{{notificacao_id}}/lida
```

Headers:

```http
Authorization: Bearer {{token}}
```

Resultado esperado:

```text
200 OK
```

Se `notificacao_id` estiver vazio, é porque nenhuma notificação foi criada/listada.

## 14. Logout

Requisição:

```http
POST {{base_url}}/api/logout
```

Headers:

```http
Authorization: Bearer {{token}}
```

Resultado esperado:

```text
200 OK
```

Depois do logout, o token deixa de ser válido.

## Como testar notificações corretamente

Para testar notificações de forma completa, use dois usuários.

Fluxo recomendado:

1. Faça login com Maria ou outro usuário comum.
2. Crie uma solicitação.
3. Guarde o ID da solicitação.
4. Faça login com o admin.
5. Responda a solicitação criada pela Maria.
6. Faça login novamente com a Maria.
7. Liste notificações.

Assim a Maria deve receber uma notificação dizendo que a solicitação dela recebeu uma resposta.

## Erros comuns

### 401 Unauthorized

Normalmente significa:

- não fez login;
- token não foi salvo;
- token expirou porque a API foi reiniciada;
- header `Authorization` não foi enviado.

Solução:

Faça login novamente.

### 403 Forbidden

Normalmente significa:

- usuário comum tentando criar categoria;
- usuário tentando resolver solicitação de outra pessoa;
- usuário tentando marcar notificação de outra pessoa como lida.

### 404 Not Found

Normalmente significa:

- `solicitacao_id` não existe;
- `notificacao_id` não existe;
- a variável está vazia no Postman.

### 400 Bad Request

Normalmente significa que faltou algum campo obrigatório no body.

Exemplos:

- solicitação sem título;
- resposta sem texto;
- categoria sem nome;
- prioridade inválida.
