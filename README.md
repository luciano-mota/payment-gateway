# payment-gateway

## 📖 Sobre o Projeto

O **Payment Gateway** é uma API RESTful desenvolvida em Java com Spring Boot que simula um sistema de pagamentos simplificado. Ele permite que usuários criem cobranças, realizem pagamentos (com saldo em conta ou cartão), façam depósitos e gerenciem suas transações.

A aplicação foi projetada para como sendo parte do desafio da empresa Nimble Baas.

### ✨ Funcionalidades Principais

- **Criação de Cobranças**: Gere uma cobrança para outro usuário.
- **Pagamento de Cobranças**: Pague com saldo interno ou via "cartão de crédito" (simulado).
- **Depósitos**: Adicione fundos à sua conta.
- **Cancelamento e Estorno**: Cancele cobranças pendentes ou estorne transações já pagas.
- **Consulta de Histórico**: Liste cobranças enviadas e recebidas com filtros por status.
- **Autorização Externa**: Integra-se a um serviço externo para autorizar pagamentos com cartão e depósitos.

## 🚀 Como Executar o Projeto

Para executar o projeto localmente, você precisará ter o **Docker** e o **Docker Compose** instalados.
No projeto já existem os arquivos Dockerfile e docker-compose.yaml para executar o projeto localmente.
Basta simplesmente executar o comando `docker-compose up` no terminal dentro da pasta raiz do projeto para startar o projeto.


## ⚙️ Uso da API

A API requer um token de autenticação para identificar o usuário. Nos exemplos abaixo, presuma que um mecanismo de autenticação (como JWT) está em vigor e que o `userId` é extraído do token.

**Endpoints Principais:**

- `POST /api/v1/charges`: Cria uma nova cobrança.
- `GET /api/v1/charges/sent`: Lista as cobranças enviadas pelo usuário.
- `GET /api/v1/charges/received`: Lista as cobranças recebidas pelo usuário.
- `POST /api/v1/charges/{id}/pay/balance`: Paga uma cobrança com saldo.
- `POST /api/v1/charges/{id}/pay/card`: Paga uma cobrança com cartão.
- `POST /api/v1/charges/{id}/cancel`: Cancela uma cobrança.
- `POST /api/v1/charges/deposit`: Realiza um depósito na conta do usuário.

> Para mais detalhes sobre os `payloads` de requisição e os formatos de resposta, consulte a implementação no `ChargeController` e nos DTOs associados.
> 
> O swagger da aplicação está configurada pode ser acessado nessa url: http://localhost:8080/swagger-ui/index.html
> 
> Para auxílio, quando necessitar usar o cpf, pode gerá-lo com o site https://www.4devs.com.br/gerador_de_cpf

---
