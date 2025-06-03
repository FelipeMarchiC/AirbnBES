# 🏡 AirbnBES

**Módulo de Aluguéis de Propriedades estilo Airbnb**
*Trabalho Prático de Verificação, Validação e Teste de Software*
**IFSP – Câmpus São Carlos** | Prof. Dr. Lucas Oliveira
**Grupo:** Felipe Marchi, Kayky Rocha, Gabriel Henrique

---

## 📖 Descrição do Projeto

**AirbnBES** é um módulo back-end para gerenciamento de aluguéis de propriedades, inspirado no modelo da plataforma Airbnb.

Este projeto faz parte do segundo marco da disciplina de **Verificação, Validação e Teste de Software**, com foco em boas práticas de engenharia de software, incluindo:

* 📜 Especificação de stories e cenários no estilo **BDD**
* 🧪 Desenvolvimento orientado a testes (**TDD**) com arquitetura **DDD**
* 🔀 Versionamento com **Conventional Commits**
* 🧰 Criação de suítes de testes **unitários** e **automatizados**
* 🌐 Implementação de controladores **REST** com **Spring Boot**

---

## ✅ Requisitos

Para rodar o projeto localmente, é necessário ter:

* **Java 17+**
* **Maven 3.6+** ou **Gradle 7+**
* **SQLite** (o driver JDBC já está incluso no projeto)
* **Node.js + npm** (para rodar o frontend)
* **Git** instalado
* **Docker (opcional)** para execução via container

---

## 🚀 Como Executar

### 🔧 Back-end (Spring Boot)

**Via terminal (sem Docker):**

1. Clone o projeto:

   ```bash
   git clone https://github.com/seu-usuario/airbnbes.git
   cd airbnbes
   ```

2. Compile e rode o projeto com Maven:

   ```bash
   ./mvnw spring-boot:run
   ```

3. Acesse:

    * **API:** [http://localhost:8080/api/v1](http://localhost:8080/api/v1)
    * **Swagger UI:** [http://localhost:8080/api/v1/api-docs](http://localhost:8080/api/v1/api-docs)

---

### 🐳 Executando com Docker

1. Clone o projeto e entre no diretório:

   ```bash
   git clone https://github.com/seu-usuario/airbnbes.git
   cd airbnbes
   ```

2. Construa a imagem:

   ```bash
   docker build -t airbnbes-app .
   ```

3. Rode o container:

   ```bash
   docker run -p 8080:8080 airbnbes-app
   ```

4. Acesse:

    * **Swagger UI:** [http://localhost:8080/api/v1/api-docs](http://localhost:8080/api/v1/api-docs)
    * Pronto! Basta começar a fazer requisições.

---

### 🖥️ Front-end

1. Acesse a pasta do frontend:

   ```bash
   cd frontend
   ```

2. Instale as dependências:

   ```bash
   npm install
   ```

3. Rode o servidor de desenvolvimento:

   ```bash
   npm run dev
   ```

4. Acesse no navegador:

    * **Frontend:** [http://localhost:5173](http://localhost:5173) 