# URL Shortner API

API REST para encurtamento de URLs desenvolvida como solução para um
desafio técnico. Permite criar URLs curtas, redirecionar para a URL
original, consultar detalhes e listar URLs com paginação.

------------------------------------------------------------------------

# Stack utilizada

-   Java 21
-   Spring Boot
-   Spring Web
-   Spring Data JPA
-   PostgreSQL
-   Flyway (migrações de banco)
-   H2 Database (testes)
-   JUnit 5
-   MockMvc (testes de integração)
-   Maven

------------------------------------------------------------------------

## Arquitetura

A aplicação segue uma arquitetura simples MVC.

### Responsabilidades

| Camada | Responsabilidade |
|------|----------------|
| Controller | Recebe requisições HTTP |
| Service | Regras de negócio |
| Repository | Acesso ao banco de dados |
| Domain | Entidades JPA |
| DTO | Objetos de entrada e saída da API |

Essa abordagem foi escolhida para manter o projeto simples e evitar
**overengineering**, conforme solicitado no desafio.

------------------------------------------------------------------------

# Como rodar o projeto

## 1 - Clonar o repositório

git clone https://github.com/marcosalexandre100/urlshortner.git

## 2 - Subir o banco de dados e pgAdmin com Docker

docker-compose up -d

## 3 - Rodar aplicação

mvn spring-boot:run

ou

./mvnw spring-boot:run

A API ficará disponível em:

http://localhost:8080

As tabelas são criadas automaticamente via Flyway na inicialização da aplicação.

Caso queira visualizar os dados do banco,acessar o pgAdmin ficará disponível em:

http://localhost:15432

# Como rodar os testes

Os testes utilizam **H2 em memória**, não sendo necessário PostgreSQL.

mvn test

ou

./mvnw test

------------------------------------------------------------------------

# Autenticação

Para criação de URLs é necessário enviar o header:

X-API-Key

A chave é configurada no `application.yml`:

app: api-key: my-secret-key

------------------------------------------------------------------------

# Endpoints

## Criar URL encurtada

### POST /v1/urls

Header obrigatório:

X-API-Key: my-secret-key

### Request

```json
{ 
  "originalUrl": "https://www.google.com", 
  "expirationDate": "2027-12-31T23:59:59Z" 
}
```

### Response
```json
{  
   "id": "abc123", 
   "shortUrl": "http://localhost:8080/abc123",
   "originalUrl": "https://www.google.com", 
   "createdAt": "2026-03-08T10:00:00Z", 
   "expirationDate": "2027-12-31T23:59:59Z",
   "clickCount": 0 
}
```

### Regras de validação

- originalUrl é obrigatório.

- originalUrl deve ser uma URL válida iniciando com http ou https.

- expirationDate é opcional. Caso expirationDate seja informada, a data deve estar no futuro e o formato da data deve seguir o padrão ISO-8601.

------------------------------------------------------------------------

## Redirecionar URL

Exemplo:

### GET /v1/urls/redirect/{id}

Resposta:

200 OK

Redireciona automaticamente para a URL original.

------------------------------------------------------------------------

## Consultar detalhes da URL

### GET /v1/urls/{id}

### Response
```json
{
  "id": "abc123",
  "shortUrl": "http://localhost:8080/abc123",
  "originalUrl": "https://www.google.com",
  "createdAt": "2026-03-08T10:00:00Z",
  "expirationDate": "2027-12-31T23:59:59Z",
  "clickCount": 5
}
```
------------------------------------------------------------------------

## Listar URLs (paginado)

### GET /v1/urls

Parâmetros opcionais:

page size sort

Exemplo:

### GET /v1/urls?size=1

### Response
```json
{
"content": [
  {
    "id": "abc123",
    "shortUrl": "http://localhost:8080/abc123",
    "originalUrl": "https://www.google.com",
    "createdAt": "2026-03-08T10:00:00Z",
    "expirationDate": "2027-12-31T23:59:59Z",
    "clickCount": 2
  }
],
  "number": 0,
  "size": 10,
  "totalElements": 1
}
```
------------------------------------------------------------------------

# Decisões de arquitetura e negócios

### Geração do código curto

O identificador curto (`shortCode`) é gerado aleatoriamente utilizando
caracteres alfanuméricos. Para evitar colisões, o sistema verifica se o
código já existe antes de persistir.

### Persistência

Os dados são persistidos utilizando:

-   Spring Data JPA
-   PostgreSQL

Migrações são controladas via **Flyway**.

### Controle de expiração

A API permite definir uma data de expiração opcional. Durante o
redirecionamento é verificado se a URL já expirou.

Caso esteja expirada, é retornado:

410 Gone

### Contador de cliques

Cada redirecionamento incrementa o campo:

clickCount

permitindo acompanhar o número de acessos da URL curta.

------------------------------------------------------------------------

# Autor

Marcos Alexandre Gomes de Souza.
