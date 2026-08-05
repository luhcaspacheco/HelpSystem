# Guia de integração do Frontend com a API HelpSystem

A ideia é deixar claro quais rotas já existem, quais dados precisam ser enviados e como o frontend deve tratar as respostas do backend.

## Endereço da API

Durante o desenvolvimento local, a API roda em:

```text
http://localhost:8080
```

Todas as rotas começam com `/api`.

Exemplo:

```text
http://localhost:8080/api/login
```

## Como a API responde

Todas as respostas seguem o mesmo formato:

```json
{
  "sucesso": true,
  "mensagem": "Mensagem explicando o resultado.",
  "dado": {}
}
```

Quando algo dá errado, a resposta vem assim:

```json
{
  "sucesso": false,
  "mensagem": "Mensagem explicando o erro.",
  "dado": null
}
```

No React, o ideal é sempre olhar o campo `sucesso`. Se ele vier `false`, a mensagem do backend pode ser exibida para o usuário.

Exemplo:

```js
if (!json.sucesso) {
  alert(json.mensagem);
}
```

## Headers

Sempre que o frontend enviar JSON no corpo da requisição, usar:

```http
Content-Type: application/json
```

Nas rotas que exigem login, também é necessário enviar o token:

```http
Authorization: Bearer TOKEN_AQUI
```

Exemplo:

```js
const token = localStorage.getItem("token");

const headers = {
  "Content-Type": "application/json",
  Authorization: `Bearer ${token}`
};
```

## Login

Rota:

```http
POST /api/login
```

Corpo enviado:

```json
{
  "email": "admin@helpsystem.local",
  "senha": "admin123"
}
```

Resposta esperada:

```json
{
  "sucesso": true,
  "mensagem": "Login efetuado com sucesso!",
  "dado": {
    "token": "token-gerado-pela-api",
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

Depois do login, o frontend deve guardar:

- `dado.token`
- `dado.usuario`

Um exemplo simples:

```js
async function login(email, senha) {
  const response = await fetch("http://localhost:8080/api/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, senha })
  });

  const json = await response.json();

  if (!json.sucesso) {
    throw new Error(json.mensagem);
  }

  localStorage.setItem("token", json.dado.token);
  localStorage.setItem("usuario", JSON.stringify(json.dado.usuario));

  return json.dado;
}
```

## Logout

Rota:

```http
POST /api/logout
```

Essa rota precisa do token no header:

```http
Authorization: Bearer TOKEN_AQUI
```

Exemplo:

```js
async function logout() {
  const token = localStorage.getItem("token");

  await fetch("http://localhost:8080/api/logout", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  localStorage.removeItem("token");
  localStorage.removeItem("usuario");
}
```

Mesmo se a API retornar erro no logout, o frontend pode remover o token localmente para limpar a sessão do usuário.

## Cadastro de usuário

Antes de montar o formulário de cadastro, o frontend deve buscar os departamentos disponíveis.

Rota:

```http
GET /api/departamentos
```

Resposta:

```json
{
  "sucesso": true,
  "mensagem": "Departamentos listados com sucesso.",
  "dado": [
    { "id": 1, "nome": "TI" },
    { "id": 2, "nome": "RH" }
  ]
}
```

Rota:

```http
POST /api/usuarios
```

Corpo enviado:

```json
{
  "nome": "Maria Teste",
  "email": "maria@empresa.com",
  "senha": "senha123",
  "departamentoId": 2
}
```

Observações importantes:

- O cadastro público cria apenas usuários comuns.
- O frontend não deve enviar `tipo: "ADMIN"`.
- Se tentar cadastrar como admin pela tela pública, a API retorna `403`.
- `departamentoId` é obrigatório e precisa existir no banco.

Resposta de sucesso:

```json
{
  "sucesso": true,
  "mensagem": "Usuário cadastrado com sucesso!",
  "dado": {
    "id": 2,
    "nome": "Maria Teste",
    "email": "maria@empresa.com",
    "tipo": "COMUM",
    "admin": false
  }
}
```

## Categorias

As categorias são usadas no formulário de criação de solicitação.

### Listar categorias

Rota:

```http
GET /api/categorias
```

Essa rota é pública, então não precisa de token.

Resposta:

```json
{
  "sucesso": true,
  "mensagem": "Categorias listadas com sucesso.",
  "dado": [
    { "id": 1, "nome": "Sistemas" },
    { "id": 2, "nome": "Acesso e Login" }
  ]
}
```

Exemplo:

```js
async function listarCategorias() {
  const response = await fetch("http://localhost:8080/api/categorias");
  const json = await response.json();

  if (!json.sucesso) {
    throw new Error(json.mensagem);
  }

  return json.dado;
}
```

### Criar categoria

Rota:

```http
POST /api/categorias
```

Essa rota é só para usuário admin.

Corpo enviado:

```json
{
  "nome": "Impressoras"
}
```

Headers:

```http
Content-Type: application/json
Authorization: Bearer TOKEN_AQUI
```

Se o usuário logado não for admin, a API retorna `403`.

## Solicitações

### Criar solicitação

Rota:

```http
POST /api/solicitacoes
```

Essa rota exige login.

Corpo enviado:

```json
{
  "titulo": "Erro ao acessar o VPN",
  "descricao": "Não consigo conectar desde ontem.",
  "categoriaId": 1,
  "prioridade": "ALTA"
}
```

Valores aceitos para `prioridade`:

```text
BAIXA
MEDIA
ALTA
```

Se a prioridade não for enviada, o backend usa `MEDIA`.

Resposta de sucesso:

```json
{
  "sucesso": true,
  "mensagem": "Solicitação criada com sucesso!",
  "dado": {
    "id": 10,
    "titulo": "Erro ao acessar o VPN",
    "descricao": "Não consigo conectar desde ontem.",
    "prioridade": "ALTA",
    "status": "ABERTA",
    "dataCriacao": "2026-08-05T09:00:00",
    "dataResolucao": null,
    "autorId": 2,
    "autorNome": "Maria Teste",
    "categoriaId": 1,
    "categoriaNome": "Sistemas"
  }
}
```

Exemplo:

```js
async function criarSolicitacao(dados) {
  const token = localStorage.getItem("token");

  const response = await fetch("http://localhost:8080/api/solicitacoes", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`
    },
    body: JSON.stringify(dados)
  });

  const json = await response.json();

  if (!json.sucesso) {
    throw new Error(json.mensagem);
  }

  return json.dado;
}
```

### Listar solicitações

Rota:

```http
GET /api/solicitacoes
```

Essa rota exige login.

É possível listar tudo ou aplicar filtros.

Exemplos:

```http
GET /api/solicitacoes
GET /api/solicitacoes?status=ABERTA
GET /api/solicitacoes?categoriaId=1
GET /api/solicitacoes?autorId=2
GET /api/solicitacoes?status=ABERTA&categoriaId=1
GET /api/solicitacoes?status=ABERTA&categoriaId=1&autorId=2
GET /api/solicitacoes?ordenarPor=prioridade
```

Valores aceitos para `status`:

```text
ABERTA
RESPONDIDA
RESOLVIDA
```

Valores aceitos para `ordenarPor`:

```text
data
prioridade
```

Se `ordenarPor` não for enviado, o backend ordena por data de criação, da mais recente para a mais antiga.

Quando `ordenarPor=prioridade`, a ordem fica: `ALTA`, `MEDIA`, `BAIXA`. Em caso de empate, as mais recentes aparecem primeiro.

Exemplo no React:

```js
async function listarSolicitacoes(filtros = {}) {
  const token = localStorage.getItem("token");
  const params = new URLSearchParams();

  if (filtros.status) {
    params.set("status", filtros.status);
  }

  if (filtros.categoriaId) {
    params.set("categoriaId", filtros.categoriaId);
  }

  if (filtros.autorId) {
    params.set("autorId", filtros.autorId);
  }

  if (filtros.ordenarPor) {
    params.set("ordenarPor", filtros.ordenarPor);
  }

  const query = params.toString();
  const url = `http://localhost:8080/api/solicitacoes${query ? `?${query}` : ""}`;

  const response = await fetch(url, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  const json = await response.json();

  if (!json.sucesso) {
    throw new Error(json.mensagem);
  }

  return json.dado;
}
```

### Buscar uma solicitação

Rota:

```http
GET /api/solicitacoes/{id}
```

Exemplo:

```http
GET /api/solicitacoes/10
```

Essa rota retorna os dados completos da solicitação.

### Marcar solicitação como resolvida

Rota:

```http
PATCH /api/solicitacoes/{id}/resolver
```

Exemplo:

```http
PATCH /api/solicitacoes/10/resolver
```

Regra importante:

- Somente o autor da solicitação pode marcar como resolvida.

Exemplo:

```js
async function resolverSolicitacao(id) {
  const token = localStorage.getItem("token");

  const response = await fetch(`http://localhost:8080/api/solicitacoes/${id}/resolver`, {
    method: "PATCH",
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  const json = await response.json();

  if (!json.sucesso) {
    throw new Error(json.mensagem);
  }

  return json.dado;
}
```

## Respostas

Depois que uma solicitação é criada, usuários logados podem responder enquanto ela ainda não estiver resolvida.

Quando uma solicitação com status `ABERTA` recebe resposta, o backend muda o status para `RESPONDIDA`.

Se quem respondeu não for o autor da solicitação, o backend também cria uma notificação para o autor.

### Criar resposta

Rota:

```http
POST /api/solicitacoes/{id}/respostas
```

Exemplo:

```http
POST /api/solicitacoes/10/respostas
```

Corpo enviado:

```json
{
  "texto": "Verifiquei aqui e o acesso foi liberado novamente."
}
```

Resposta de sucesso:

```json
{
  "sucesso": true,
  "mensagem": "Resposta registrada com sucesso!",
  "dado": {
    "id": 5,
    "solicitacaoId": 10,
    "texto": "Verifiquei aqui e o acesso foi liberado novamente.",
    "dataCriacao": "2026-08-05T09:30:00",
    "autorId": 1,
    "autorNome": "Administrador"
  }
}
```

Exemplo no React:

```js
async function responderSolicitacao(solicitacaoId, texto) {
  const token = localStorage.getItem("token");

  const response = await fetch(`http://localhost:8080/api/solicitacoes/${solicitacaoId}/respostas`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`
    },
    body: JSON.stringify({ texto })
  });

  const json = await response.json();

  if (!json.sucesso) {
    throw new Error(json.mensagem);
  }

  return json.dado;
}
```

### Listar respostas de uma solicitação

Rota:

```http
GET /api/solicitacoes/{id}/respostas
```

Exemplo:

```http
GET /api/solicitacoes/10/respostas
```

Exemplo no React:

```js
async function listarRespostas(solicitacaoId) {
  const token = localStorage.getItem("token");

  const response = await fetch(`http://localhost:8080/api/solicitacoes/${solicitacaoId}/respostas`, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  const json = await response.json();

  if (!json.sucesso) {
    throw new Error(json.mensagem);
  }

  return json.dado;
}
```

## Notificações

Quando uma solicitação recebe resposta de outra pessoa, o backend cria uma notificação para o autor da solicitação.

### Listar notificações

Rota:

```http
GET /api/notificacoes
```

Essa rota exige login.

Também é possível filtrar:

```http
GET /api/notificacoes?lida=false
GET /api/notificacoes?lida=true
```

Resposta:

```json
{
  "sucesso": true,
  "mensagem": "Notificacoes listadas com sucesso.",
  "dado": [
    {
      "id": 1,
      "mensagem": "Sua solicitacao #10 recebeu uma nova resposta.",
      "lida": false,
      "dataCriacao": "2026-08-05T09:40:00",
      "solicitacaoId": 10,
      "solicitacaoTitulo": "Erro ao acessar o VPN"
    }
  ]
}
```

Exemplo no React:

```js
async function listarNotificacoes(lida) {
  const token = localStorage.getItem("token");
  const params = new URLSearchParams();

  if (lida !== undefined) {
    params.set("lida", lida);
  }

  const query = params.toString();
  const url = `http://localhost:8080/api/notificacoes${query ? `?${query}` : ""}`;

  const response = await fetch(url, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  const json = await response.json();

  if (!json.sucesso) {
    throw new Error(json.mensagem);
  }

  return json.dado;
}
```

### Total de não lidas

Rota:

```http
GET /api/notificacoes/nao-lidas/total
```

Resposta:

```json
{
  "sucesso": true,
  "mensagem": "Total de notificacoes nao lidas.",
  "dado": {
    "total": 3
  }
}
```

Essa rota é útil para badge no ícone de notificações.

### Marcar como lida

Rota:

```http
PATCH /api/notificacoes/{id}/lida
```

Exemplo:

```http
PATCH /api/notificacoes/1/lida
```

Somente o dono da notificação pode marcá-la como lida.

Exemplo no React:

```js
async function marcarNotificacaoComoLida(id) {
  const token = localStorage.getItem("token");

  const response = await fetch(`http://localhost:8080/api/notificacoes/${id}/lida`, {
    method: "PATCH",
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  const json = await response.json();

  if (!json.sucesso) {
    throw new Error(json.mensagem);
  }

  return json.dado;
}
```

## Códigos HTTP mais comuns

```text
200 OK                 Operação realizada com sucesso
201 Created            Registro criado com sucesso
400 Bad Request        Dados inválidos ou faltando
401 Unauthorized       Login incorreto, token ausente ou token inválido
403 Forbidden          Usuário sem permissão para aquela ação
404 Not Found          Registro não encontrado
500 Internal Error     Erro interno da API
```

## Sugestão de organização no React

Uma estrutura simples já ajuda bastante:

```text
src/
  services/
    api.js
    authService.js
    departamentoService.js
    categoriaService.js
    solicitacaoService.js
```

O arquivo `api.js` pode centralizar a URL base e os headers:

```js
export const API_BASE_URL = "http://localhost:8080/api";

export function getAuthHeaders() {
  const token = localStorage.getItem("token");

  return token
    ? { Authorization: `Bearer ${token}` }
    : {};
}

export function getJsonHeaders() {
  return {
    "Content-Type": "application/json",
    ...getAuthHeaders()
  };
}
```

Assim, os outros services ficam menores e mais fáceis de manter.

## Pontos combinados com o backend

- Quando `sucesso` vier `false`, mostrar `mensagem` para o usuário.
- Quando a API retornar `401`, mandar o usuário para a tela de login.
- Telas administrativas devem verificar `usuario.admin`.
- Mesmo que o frontend esconda botões de admin, o backend também valida permissão.
- O token pode ficar no `localStorage` neste MVP.
- No logout, remover token e usuário salvos.
- Respostas e notificações já estão disponíveis para integração.
