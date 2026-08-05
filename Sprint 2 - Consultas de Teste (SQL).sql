-- =====================================================================
--  HELP SYSTEM  ·  Consultas de Teste + Criação direto no banco
--  Rode no MySQL Workbench. A PARTE 1 cria dados de teste (inclusive
--  resposta, que ainda não tem endpoint). A PARTE 2 são as consultas
--  de verificação.
-- =====================================================================

USE help_system;

-- =====================================================================
--  PARTE 1 — CRIAÇÃO DIRETO NO BANCO (dados de teste)
--  Rode de cima para baixo. Pode rodar várias vezes: o usuário é criado
--  só uma vez (INSERT IGNORE); solicitação e resposta são criadas a cada
--  execução.
-- =====================================================================

-- 1) Usuário de teste
--    A senha é 'admin123' (reaproveitamos o mesmo hash BCrypt do admin),
--    então dá até para logar com este usuário depois.
INSERT IGNORE INTO usuario (nome, email, senha_hash, tipo, departamento_id)
VALUES ('Maria Teste', 'maria.sql@empresa.com',
        '$2a$12$IQQND3AtBygNaVRRM.pwye1p6DX6NZnzZjSlLz/q5VSz.zNNujPFu',
        'COMUM',
        (SELECT id FROM departamento WHERE nome = 'RH'));

SET @autor_id = (SELECT id FROM usuario WHERE email = 'maria.sql@empresa.com');
SET @respondente_id = (SELECT id FROM usuario WHERE email = 'admin@helpsystem.local');

-- 2) (Opcional) Categoria de teste — criada só se ainda não existir
INSERT IGNORE INTO categoria (nome) VALUES ('Teste SQL');

-- 3) Solicitação de teste (autor = Maria; categoria = Sistemas; status ABERTA)
INSERT INTO solicitacao (titulo, descricao, autor_id, categoria_id, prioridade, status)
VALUES ('Solicitação criada via SQL',
        'Descrição de teste inserida direto no banco.',
        @autor_id,
        (SELECT id FROM categoria WHERE nome = 'Sistemas'),
        'ALTA', 'ABERTA');

SET @solic_id = LAST_INSERT_ID();

-- 4) Resposta de teste, vinculada à solicitação acima (respondente = admin)
INSERT INTO resposta (solicitacao_id, autor_id, texto)
VALUES (@solic_id, @respondente_id, 'Resposta de teste inserida via SQL.');

-- 5) Como já tem resposta, marca a solicitação como RESPONDIDA
UPDATE solicitacao SET status = 'RESPONDIDA' WHERE id = @solic_id;

-- =====================================================================
--  PARTE 2 — CONSULTAS DE VERIFICAÇÃO
-- =====================================================================

-- A) Visão geral: quantos registros há em cada tabela
SELECT 'departamento' AS tabela, COUNT(*) AS total FROM departamento
UNION ALL SELECT 'categoria',    COUNT(*) FROM categoria
UNION ALL SELECT 'usuario',      COUNT(*) FROM usuario
UNION ALL SELECT 'solicitacao',  COUNT(*) FROM solicitacao
UNION ALL SELECT 'resposta',     COUNT(*) FROM resposta
UNION ALL SELECT 'notificacao',  COUNT(*) FROM notificacao;

-- B) Categorias em ordem alfabética
SELECT id, nome FROM categoria ORDER BY nome;

-- C) Solicitações com autor e categoria (prévia da tela de listagem)
SELECT s.id, s.titulo, s.prioridade, s.status,
       u.nome AS autor, c.nome AS categoria, s.data_criacao
FROM solicitacao s
JOIN usuario u        ON u.id = s.autor_id
LEFT JOIN categoria c ON c.id = s.categoria_id
ORDER BY s.data_criacao DESC;

-- D) Respostas com a solicitação e o autor da resposta
SELECT r.id, r.solicitacao_id, s.titulo AS solicitacao,
       u.nome AS autor_resposta, r.texto, r.data_criacao
FROM resposta r
JOIN solicitacao s ON s.id = r.solicitacao_id
JOIN usuario u     ON u.id = r.autor_id
ORDER BY r.data_criacao DESC;
