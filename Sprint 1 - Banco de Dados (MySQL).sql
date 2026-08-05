-- =====================================================================
--  HELP SYSTEM  ·  Sprint 1 - Fundação
--  Banco de dados MySQL 8.x (compatível com MariaDB 10.x)
--  Projeto Scrum · Specialisterne
--
--  Como executar:
--    mysql -u root -p < "Sprint 1 - Banco de Dados (MySQL).sql"
--  ou cole o conteúdo no MySQL Workbench / DBeaver e execute.
--
--  Este script recria o banco do zero (DROP + CREATE). NÃO rode em
--  produção com dados reais.
-- =====================================================================

DROP DATABASE IF EXISTS help_system;
CREATE DATABASE help_system
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE help_system;

-- ---------------------------------------------------------------------
-- 1) DEPARTAMENTO
--    Setor ao qual o colaborador pertence. (apoio ao cadastro RF01)
-- ---------------------------------------------------------------------
CREATE TABLE departamento (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    nome  VARCHAR(80) NOT NULL,
    CONSTRAINT uq_departamento_nome UNIQUE (nome)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 2) USUARIO  (colaborador)
--    RF01: cadastro · RF01.3 e-mail único · RF01.4 senha em hash
--    RF02.1: tipo ADMIN ou COMUM controla o acesso
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    nome             VARCHAR(120) NOT NULL,
    email            VARCHAR(150) NOT NULL,
    senha_hash       VARCHAR(255) NOT NULL,               -- guarde SEMPRE o hash (BCrypt), nunca a senha pura
    tipo             ENUM('ADMIN','COMUM') NOT NULL DEFAULT 'COMUM',
    departamento_id  INT NULL,
    ativo            BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuario_email UNIQUE (email),           -- RF01.6: e-mail duplicado é barrado pelo banco
    CONSTRAINT fk_usuario_departamento
        FOREIGN KEY (departamento_id) REFERENCES departamento(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 3) CATEGORIA
--    RF06: categorias das solicitações · RF06.2 nome único (sem duplicata)
-- ---------------------------------------------------------------------
CREATE TABLE categoria (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    nome  VARCHAR(80) NOT NULL,
    CONSTRAINT uq_categoria_nome UNIQUE (nome)            -- RF06.2: duplicidade barrada pelo banco
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 4) SOLICITACAO  (a "pergunta")
--    RF03: criação · RF05: listagem/filtros/ordenação
--    RF05.3: ordenar por data OU prioridade · RF07.1: status resolvido
-- ---------------------------------------------------------------------
CREATE TABLE solicitacao (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    titulo         VARCHAR(150) NOT NULL,
    descricao      TEXT NOT NULL,
    autor_id       INT NOT NULL,                          -- quem criou a solicitação
    categoria_id   INT NULL,
    prioridade     ENUM('BAIXA','MEDIA','ALTA') NOT NULL DEFAULT 'MEDIA',
    status         ENUM('ABERTA','RESPONDIDA','RESOLVIDA') NOT NULL DEFAULT 'ABERTA',
    data_criacao   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_resolucao DATETIME NULL,                         -- preenchido quando vira RESOLVIDA (RF07.1)
    CONSTRAINT fk_solicitacao_autor
        FOREIGN KEY (autor_id) REFERENCES usuario(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_solicitacao_categoria
        FOREIGN KEY (categoria_id) REFERENCES categoria(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Índices para os filtros e a ordenação da listagem (RF05.2 / RF05.3)
CREATE INDEX ix_solicitacao_status      ON solicitacao (status);
CREATE INDEX ix_solicitacao_categoria   ON solicitacao (categoria_id);
CREATE INDEX ix_solicitacao_autor       ON solicitacao (autor_id);
CREATE INDEX ix_solicitacao_prioridade  ON solicitacao (prioridade);
CREATE INDEX ix_solicitacao_data        ON solicitacao (data_criacao);

-- ---------------------------------------------------------------------
-- 5) RESPOSTA
--    RF04.2: resposta vinculada à solicitação e a quem respondeu
-- ---------------------------------------------------------------------
CREATE TABLE resposta (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    solicitacao_id  INT NOT NULL,
    autor_id        INT NOT NULL,                         -- quem respondeu
    texto           TEXT NOT NULL,
    data_criacao    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resposta_solicitacao
        FOREIGN KEY (solicitacao_id) REFERENCES solicitacao(id)
        ON DELETE CASCADE ON UPDATE CASCADE,              -- apagou a solicitação, apaga as respostas
    CONSTRAINT fk_resposta_autor
        FOREIGN KEY (autor_id) REFERENCES usuario(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE INDEX ix_resposta_solicitacao ON resposta (solicitacao_id);

-- ---------------------------------------------------------------------
-- 6) NOTIFICACAO   (RF04.3 - prioridade BAIXA, opcional no MVP)
--    Avisa o autor da solicitação quando ela recebe uma resposta.
-- ---------------------------------------------------------------------
CREATE TABLE notificacao (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id      INT NOT NULL,                         -- destinatário (autor da solicitação)
    solicitacao_id  INT NOT NULL,
    mensagem        VARCHAR(255) NOT NULL,
    lida            BOOLEAN NOT NULL DEFAULT FALSE,
    data_criacao    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notificacao_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_notificacao_solicitacao
        FOREIGN KEY (solicitacao_id) REFERENCES solicitacao(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE INDEX ix_notificacao_usuario ON notificacao (usuario_id, lida);

-- =====================================================================
--  DADOS INICIAIS (seed) - para conseguir testar já no Sprint 1
-- =====================================================================

INSERT INTO departamento (nome) VALUES
    ('TI'), ('RH'), ('Financeiro'), ('Comercial'), ('Operações');

INSERT INTO categoria (nome) VALUES
    ('Sistemas'), ('Acesso e Login'), ('Recursos Humanos'),
    ('Financeiro'), ('Infraestrutura'), ('Outros');

-- Usuário administrador inicial.
-- Senha inicial: admin123
INSERT INTO usuario (nome, email, senha_hash, tipo, departamento_id) VALUES
    ('Administrador', 'admin@helpsystem.local',
     '$2a$12$.ZUB90JzeL8xMVHiqwjeq.cFoJEEtW008wbOdwYP9FDTrbyrgl5oq', 'ADMIN',
     (SELECT id FROM departamento WHERE nome = 'TI'));

-- =====================================================================
--  CONSULTAS DE EXEMPLO (referência para a frente Backend)
-- =====================================================================

-- Listagem de solicitações com autor e categoria (base do RF05.1)
--   SELECT s.id, s.titulo, s.prioridade, s.status, s.data_criacao,
--          u.nome AS autor, c.nome AS categoria
--   FROM solicitacao s
--   JOIN usuario u   ON u.id = s.autor_id
--   LEFT JOIN categoria c ON c.id = s.categoria_id
--   ORDER BY s.data_criacao DESC;         -- ou: ORDER BY FIELD(s.prioridade,'ALTA','MEDIA','BAIXA')

-- Filtro por status + categoria (RF05.2)
--   ... WHERE s.status = 'ABERTA' AND s.categoria_id = 1;

-- Marcar como resolvida (RF07.1) - a regra "só o autor" (RF07.2)
-- é validada NA APLICAÇÃO antes deste UPDATE:
--   UPDATE solicitacao SET status='RESOLVIDA', data_resolucao=NOW() WHERE id = ?;
