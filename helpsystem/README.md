# HelpSystem

Sistema interno de perguntas e respostas entre colaboradores. Projeto Java (Maven) com banco MySQL.

## Requisitos

- Java 17 ou superior (JDK)
- Maven 3.9+
- MySQL 8.x com o banco `help_system` criado (rode o script `Sprint 1 - Banco de Dados (MySQL).sql`)

## Estrutura de pacotes

```
helpsystem/
├── pom.xml                         Dependências (driver MySQL, jBCrypt)
└── src/main/java/com/helpsystem/
    ├── App.java                    Ponto de entrada provisório (teste do Sprint 1)
    ├── model/
    │   ├── Departamento.java       Entidades: mapeiam as tabelas do banco
    │   ├── Categoria.java
    │   ├── Usuario.java
    │   ├── Solicitacao.java
    │   ├── Resposta.java
    │   ├── Notificacao.java
    │   └── enums/
    │       ├── TipoUsuario.java    ADMIN, COMUM (RF02.1)
    │       ├── StatusSolicitacao.java  ABERTA, RESPONDIDA, RESOLVIDA (RF07.1)
    │       └── Prioridade.java     BAIXA, MEDIA, ALTA (RF05.3)
    └── util/
        ├── ConexaoBanco.java       Conexão JDBC com o help_system
        └── PasswordUtil.java       Hash de senha BCrypt (RF01.4)
```

As próximas camadas (a criar nas próximas tarefas): `dao/` (persistência), `service/` (regras de negócio) e `view/` (telas/console).

## Como configurar e rodar

1. Ajuste a senha do MySQL em `util/ConexaoBanco.java` (constante `SENHA`).
2. Compile e baixe as dependências:
   ```
   mvn clean compile
   ```
3. Rode o teste do Sprint 1:
   ```
   mvn exec:java -Dexec.mainClass=com.helpsystem.App
   ```
   Ou rode a classe `App` direto pela sua IDE (IntelliJ, Eclipse ou NetBeans importando como projeto Maven).

Se a conexão aparecer como **OK**, o setup do Sprint 1 está completo.

## Observações de arquitetura

- As entidades usam **referências a objetos** (ex.: `Solicitacao` tem um `Usuario autor`), não apenas ids — os DAOs farão o mapeamento com o banco.
- Regras que não ficam no banco e serão validadas em código: hash de senha (RF01.4), controle de acesso ADMIN/COMUM (RF02.2) e "só o autor resolve" (RF07.2).
