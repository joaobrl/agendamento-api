# Agendamento API

Uma API para gerenciamento de agendamentos em um salão de beleza, desenvolvida com **Java** e **Spring Boot**.

## 🛠 Tecnologias Utilizadas

- **Java**
- **Spring Boot**
- **Maven**
- **Jakarta Validation**
- **Spring Security**
- **Lombok**
- **JPA/Hibernate**

## ✨ Funcionalidades

- **Agendar**: Permite que clientes, funcionários, recepcionistas ou administradores criem novos agendamentos.
- **Atualizar**: Permite que funcionários, recepcionistas ou administradores atualizem o status de um agendamento para concluído.
- **Cancelar**: Permite o cancelamento de agendamentos com regras de permissão específicas.
- **Validações**:
  - Horários permitidos: múltiplos de 30 minutos e dentro de intervalos específicos.
  - Um cliente não pode agendar para outro.
  - Cliente não pode ter mais de 2 agendamentos no mesmo dia.
  - Se o cliente desejar agendar mais de 3 horários no mesmo dia, deverá que entrar em contato com profissional.
  - Cancelamentos só podem ser feitos até 24 horas antes do horário agendado.

## 📦 Estrutura do Projeto

### Pacotes Principais

- `controller`: Contém os endpoints REST da API.
- `service`: Contém a lógica de negócios.
- `model`: Contém as entidades JPA.
- `dto`: Contém os objetos de transferência de dados.
- `repository`: Contém as interfaces para acesso ao banco de dados.

### Principais Classes

- **`AgendamentoController`**: Controlador responsável pelos endpoints de agendamento.
- **`AgendamentoService`**: Serviço que implementa as regras de negócio.
- **`Agendamento`**: Entidade que representa um agendamento no sistema.

## 📚 Endpoints

### Agendar

- **POST** `/agendamento/agendar`
- **Descrição**: Cria um novo agendamento.
- **Permissões**: CLIENTE, FUNCIONARIO, RECEPCIONISTA e ADMINISTRADOR.

### Atualizar

- **PATCH** `/agendamento/{agendamentoId}/atualizar`
- **Descrição**: Atualiza o status de um agendamento para concluído.
- **Permissões**: FUNCIONARIO, RECEPCIONISTA e ADMINISTRADOR.

### Cancelar

- **DELETE** `/agendamento/{agendamentoId}/cancelar`
- **Descrição**: Cancela um agendamento.
- **Permissões**: Baseadas no perfil do usuário (CLIENTE, FUNCIONARIO, RECEPCIONISTA e ADMINISTRADOR).

## 🧠 Regras de Negócio

- **Horários Permitidos**:
  - Múltiplos de 30 minutos (ex.: 10:00, 10:30).
  - Intervalos: 08:00-11:30 e 13:00-17:30.
- **Cancelamento**:
  - Apenas até 24 horas antes do horário agendado.
  - Respeita permissões baseadas no perfil do usuário.
- **Atualização de conclusão**:
  - Só pode ser feita 30 minutos após o horário agendado.

## 🚢 Executando com Docker

1. Clone o repositório:
   ```bash
   git clone https://github.com/joaobrl/agendamento-api.git
   ```
2. Navegue até o diretório do projeto:
   ```bash
   cd agendamento-api
   ```
3. Compile utilizando o Maven:
   ```bash
   mvn clean install
   ```
4. Construa a imagem Docker e execute o container:
   ```bash
    docker-compose up --build
   ```
5. Acesse a API em `http://localhost:8080`.

## Testes

Para executar os testes, utilize o comando:
```bash
mvn test
```

## Contribuição

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou enviar pull requests.

```
