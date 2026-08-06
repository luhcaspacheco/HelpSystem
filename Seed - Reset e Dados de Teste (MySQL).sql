-- =========================================================
-- HelpSystem — Reset + dados de teste
-- ATENCAO: apaga TODAS as solicitacoes, respostas e notificacoes
-- e TODOS os usuarios, exceto admin@helpsystem.local.
-- Senha de todos os novos usuarios: senha123
-- =========================================================
USE help_system;
START TRANSACTION;

-- 1) Limpeza (ordem respeita as chaves estrangeiras)
DELETE FROM notificacao;
DELETE FROM resposta;
DELETE FROM solicitacao;
DELETE FROM usuario WHERE email <> 'admin@helpsystem.local';

-- Fallbacks caso algum departamento/categoria nao exista pelo nome
SET @dep_default := (SELECT id FROM departamento ORDER BY id LIMIT 1);
SET @cat_default := COALESCE((SELECT id FROM categoria WHERE nome='Outros' LIMIT 1), (SELECT id FROM categoria ORDER BY id LIMIT 1));

-- 2) Usuarios (2 admin + 8 comum)
INSERT INTO usuario (nome, email, senha_hash, tipo, departamento_id, ativo) VALUES
('Ana Souza','ana.souza@helpsystem.com','$2a$12$e/smPyi4TME3irM4Z2JYfOqAKkGpfk2eM9ONIP1RGwJp5m56nie1m','ADMIN',COALESCE((SELECT id FROM departamento WHERE nome='TI' LIMIT 1), @dep_default),TRUE),
('Bruno Lima','bruno.lima@helpsystem.com','$2a$12$oJ/WTzGP01t5cKZV187T3OFe8R3E0xswew88SOVbIQgbpYAVd6TOu','ADMIN',COALESCE((SELECT id FROM departamento WHERE nome='Operações' LIMIT 1), @dep_default),TRUE),
('Carla Mendes','carla.mendes@helpsystem.com','$2a$12$ZHNqRxGDIDT.c4BMb.HDhOZRQ92Nu43BeO6QC00DFIWJBAQJJ3lx.','COMUM',COALESCE((SELECT id FROM departamento WHERE nome='RH' LIMIT 1), @dep_default),TRUE),
('Diego Alves','diego.alves@helpsystem.com','$2a$12$XXgnDupvbdKTQurWjUmhSuBeSC9ulsDToFUbyupHlpi12yzIR3JYy','COMUM',COALESCE((SELECT id FROM departamento WHERE nome='Financeiro' LIMIT 1), @dep_default),TRUE),
('Erika Nunes','erika.nunes@helpsystem.com','$2a$12$wuAZCb4rot.vc9gvuwejP.uJrQUor8pvYhBNsA8W0NuKdh347j0ea','COMUM',COALESCE((SELECT id FROM departamento WHERE nome='TI' LIMIT 1), @dep_default),TRUE),
('Felipe Rocha','felipe.rocha@helpsystem.com','$2a$12$U0oP9X1jdoHdYecjXQ1SZ.qDBNdv.nxSzj3w5rFh8f8FK.0iFG1r.','COMUM',COALESCE((SELECT id FROM departamento WHERE nome='Operações' LIMIT 1), @dep_default),TRUE),
('Gabriela Dias','gabriela.dias@helpsystem.com','$2a$12$IXiUoBzPpnYot8Xd3yvjzeqZZ6snLynJQzxjrhaGLrJLMYJDLnFSy','COMUM',COALESCE((SELECT id FROM departamento WHERE nome='RH' LIMIT 1), @dep_default),TRUE),
('Henrique Costa','henrique.costa@helpsystem.com','$2a$12$esn15d6zyl9c2VFM/ioJ2OSbQirnnFxIQ4M7pUXFLzyv0ILymN8fm','COMUM',COALESCE((SELECT id FROM departamento WHERE nome='Financeiro' LIMIT 1), @dep_default),TRUE),
('Isabela Ramos','isabela.ramos@helpsystem.com','$2a$12$TofakDwAkY7dbsuCMcw1zOHGknJewlLxT4PwVXUyciHfyp1T6.X2O','COMUM',COALESCE((SELECT id FROM departamento WHERE nome='TI' LIMIT 1), @dep_default),TRUE),
('Joao Pereira','joao.pereira@helpsystem.com','$2a$12$Ob44fdJzS2YAMG6xGzEYmuEIYVu5oe8x5II3esWRcJu5vYMR.XAMC','COMUM',COALESCE((SELECT id FROM departamento WHERE nome='Operações' LIMIT 1), @dep_default),TRUE);

-- 3) Solicitacoes
INSERT INTO solicitacao (titulo, descricao, autor_id, categoria_id, prioridade, status, data_criacao, data_resolucao) VALUES
('Não consigo acessar o VPN','Ao tentar conectar no VPN aparece erro de autenticação desde ontem.',(SELECT id FROM usuario WHERE email='erika.nunes@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Acesso e Login' LIMIT 1), @cat_default),'ALTA','RESOLVIDA',(NOW() - INTERVAL 3 HOUR),(NOW() - INTERVAL 1 HOUR)),
('Solicitação de novo mouse','Meu mouse parou de funcionar, preciso de um novo.',(SELECT id FROM usuario WHERE email='carla.mendes@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Infraestrutura' LIMIT 1), @cat_default),'BAIXA','RESPONDIDA',(NOW() - INTERVAL 10 HOUR),NULL),
('Erro ao emitir nota fiscal','O sistema trava ao gerar a NF-e no fim do dia.',(SELECT id FROM usuario WHERE email='diego.alves@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Sistemas' LIMIT 1), @cat_default),'ALTA','RESPONDIDA',(NOW() - INTERVAL 17 HOUR),NULL),
('Como solicitar férias?','Qual o procedimento para pedir férias pelo sistema de RH?',(SELECT id FROM usuario WHERE email='henrique.costa@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Recursos Humanos' LIMIT 1), @cat_default),'MEDIA','RESOLVIDA',(NOW() - INTERVAL 24 HOUR),(NOW() - INTERVAL 20 HOUR)),
('Instalação do Office','Preciso do pacote Office instalado na minha máquina nova.',(SELECT id FROM usuario WHERE email='joao.pereira@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Sistemas' LIMIT 1), @cat_default),'MEDIA','RESOLVIDA',(NOW() - INTERVAL 31 HOUR),(NOW() - INTERVAL 27 HOUR)),
('Impressora não imprime','A impressora do 2º andar não responde.',(SELECT id FROM usuario WHERE email='felipe.rocha@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Infraestrutura' LIMIT 1), @cat_default),'MEDIA','ABERTA',(NOW() - INTERVAL 38 HOUR),NULL),
('Acesso ao sistema financeiro','Não tenho permissão para abrir o módulo financeiro.',(SELECT id FROM usuario WHERE email='diego.alves@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Acesso e Login' LIMIT 1), @cat_default),'ALTA','RESPONDIDA',(NOW() - INTERVAL 45 HOUR),NULL),
('Troca de monitor','Meu monitor está com falha na tela, aparecem linhas.',(SELECT id FROM usuario WHERE email='isabela.ramos@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Infraestrutura' LIMIT 1), @cat_default),'BAIXA','ABERTA',(NOW() - INTERVAL 52 HOUR),NULL),
('Dúvida sobre reembolso','Como lançar um reembolso de viagem no sistema?',(SELECT id FROM usuario WHERE email='henrique.costa@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Financeiro' LIMIT 1), @cat_default),'MEDIA','RESOLVIDA',(NOW() - INTERVAL 59 HOUR),(NOW() - INTERVAL 55 HOUR)),
('E-mail fora do ar','Não recebo e-mails desde a manhã de hoje.',(SELECT id FROM usuario WHERE email='carla.mendes@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Sistemas' LIMIT 1), @cat_default),'ALTA','RESPONDIDA',(NOW() - INTERVAL 66 HOUR),NULL),
('Senha expirada','Minha senha expirou e não consigo redefinir sozinho.',(SELECT id FROM usuario WHERE email='gabriela.dias@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Acesso e Login' LIMIT 1), @cat_default),'MEDIA','RESOLVIDA',(NOW() - INTERVAL 73 HOUR),(NOW() - INTERVAL 69 HOUR)),
('Cadeira quebrada','A cadeira da minha estação está com o encosto solto.',(SELECT id FROM usuario WHERE email='felipe.rocha@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Infraestrutura' LIMIT 1), @cat_default),'BAIXA','ABERTA',(NOW() - INTERVAL 80 HOUR),NULL),
('Erro 500 no portal','Ao salvar uma solicitação aparece erro 500.',(SELECT id FROM usuario WHERE email='isabela.ramos@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Sistemas' LIMIT 1), @cat_default),'ALTA','RESPONDIDA',(NOW() - INTERVAL 87 HOUR),NULL),
('Solicitar acesso ao CRM','Preciso de acesso ao CRM para o time comercial.',(SELECT id FROM usuario WHERE email='joao.pereira@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Acesso e Login' LIMIT 1), @cat_default),'MEDIA','ABERTA',(NOW() - INTERVAL 94 HOUR),NULL),
('Nota fiscal duplicada','Foi gerada uma NF duplicada, como cancelar?',(SELECT id FROM usuario WHERE email='diego.alves@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Financeiro' LIMIT 1), @cat_default),'ALTA','RESOLVIDA',(NOW() - INTERVAL 101 HOUR),(NOW() - INTERVAL 97 HOUR)),
('Atualização do sistema','Quando será a próxima atualização do sistema interno?',(SELECT id FROM usuario WHERE email='carla.mendes@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Sistemas' LIMIT 1), @cat_default),'BAIXA','RESPONDIDA',(NOW() - INTERVAL 108 HOUR),NULL),
('Problema com headset','O headset não capta áudio nas reuniões.',(SELECT id FROM usuario WHERE email='gabriela.dias@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Infraestrutura' LIMIT 1), @cat_default),'BAIXA','ABERTA',(NOW() - INTERVAL 115 HOUR),NULL),
('Dúvida sobre holerite','Onde encontro meu holerite do mês passado?',(SELECT id FROM usuario WHERE email='henrique.costa@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Recursos Humanos' LIMIT 1), @cat_default),'MEDIA','RESOLVIDA',(NOW() - INTERVAL 122 HOUR),(NOW() - INTERVAL 118 HOUR)),
('Acesso remoto lento','A conexão de acesso remoto está muito lenta.',(SELECT id FROM usuario WHERE email='erika.nunes@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Infraestrutura' LIMIT 1), @cat_default),'MEDIA','RESPONDIDA',(NOW() - INTERVAL 129 HOUR),NULL),
('Erro ao anexar arquivo','Não consigo anexar PDF na solicitação.',(SELECT id FROM usuario WHERE email='felipe.rocha@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Sistemas' LIMIT 1), @cat_default),'MEDIA','ABERTA',(NOW() - INTERVAL 136 HOUR),NULL),
('Solicitar segundo monitor','Gostaria de um segundo monitor para produtividade.',(SELECT id FROM usuario WHERE email='isabela.ramos@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Infraestrutura' LIMIT 1), @cat_default),'BAIXA','RESPONDIDA',(NOW() - INTERVAL 143 HOUR),NULL),
('Reset de senha do ERP','Esqueci a senha do ERP e preciso acessar hoje.',(SELECT id FROM usuario WHERE email='joao.pereira@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Acesso e Login' LIMIT 1), @cat_default),'ALTA','RESOLVIDA',(NOW() - INTERVAL 150 HOUR),(NOW() - INTERVAL 146 HOUR)),
('Teclado com teclas falhando','Algumas teclas do teclado não funcionam.',(SELECT id FROM usuario WHERE email='carla.mendes@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Infraestrutura' LIMIT 1), @cat_default),'BAIXA','ABERTA',(NOW() - INTERVAL 157 HOUR),NULL),
('Dúvida sobre vale-refeição','Como consultar o saldo do vale-refeição?',(SELECT id FROM usuario WHERE email='henrique.costa@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Recursos Humanos' LIMIT 1), @cat_default),'BAIXA','RESPONDIDA',(NOW() - INTERVAL 164 HOUR),NULL),
('Sistema lento à tarde','O sistema fica muito lento no período da tarde.',(SELECT id FROM usuario WHERE email='diego.alves@helpsystem.com' LIMIT 1),COALESCE((SELECT id FROM categoria WHERE nome='Sistemas' LIMIT 1), @cat_default),'MEDIA','RESOLVIDA',(NOW() - INTERVAL 171 HOUR),(NOW() - INTERVAL 167 HOUR));

-- 4) Respostas
INSERT INTO resposta (solicitacao_id, autor_id, texto, data_criacao) VALUES
((SELECT id FROM solicitacao WHERE titulo='Não consigo acessar o VPN' LIMIT 1),(SELECT id FROM usuario WHERE email='ana.souza@helpsystem.com' LIMIT 1),'Redefini seu acesso ao VPN, tente novamente com a nova senha.',(NOW() - INTERVAL 2 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='Não consigo acessar o VPN' LIMIT 1),(SELECT id FROM usuario WHERE email='erika.nunes@helpsystem.com' LIMIT 1),'Funcionou, obrigada!',(NOW() - INTERVAL 3 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='Solicitação de novo mouse' LIMIT 1),(SELECT id FROM usuario WHERE email='bruno.lima@helpsystem.com' LIMIT 1),'Já pedimos ao almoxarifado, chega em 2 dias úteis.',(NOW() - INTERVAL 2 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='Erro ao emitir nota fiscal' LIMIT 1),(SELECT id FROM usuario WHERE email='ana.souza@helpsystem.com' LIMIT 1),'Estamos analisando o log do servidor, retornamos em breve.',(NOW() - INTERVAL 2 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='Como solicitar férias?' LIMIT 1),(SELECT id FROM usuario WHERE email='gabriela.dias@helpsystem.com' LIMIT 1),'Acesse o menu RH > Férias e preencha o formulário.',(NOW() - INTERVAL 2 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='Como solicitar férias?' LIMIT 1),(SELECT id FROM usuario WHERE email='henrique.costa@helpsystem.com' LIMIT 1),'Consegui, valeu!',(NOW() - INTERVAL 3 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='Instalação do Office' LIMIT 1),(SELECT id FROM usuario WHERE email='erika.nunes@helpsystem.com' LIMIT 1),'Office instalado remotamente, reinicie a máquina.',(NOW() - INTERVAL 2 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='Acesso ao sistema financeiro' LIMIT 1),(SELECT id FROM usuario WHERE email='bruno.lima@helpsystem.com' LIMIT 1),'Liberamos seu acesso ao módulo, teste por favor.',(NOW() - INTERVAL 2 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='Dúvida sobre reembolso' LIMIT 1),(SELECT id FROM usuario WHERE email='diego.alves@helpsystem.com' LIMIT 1),'Use o menu Financeiro > Reembolsos e anexe as notas.',(NOW() - INTERVAL 2 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='Dúvida sobre reembolso' LIMIT 1),(SELECT id FROM usuario WHERE email='henrique.costa@helpsystem.com' LIMIT 1),'Perfeito, obrigado.',(NOW() - INTERVAL 3 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='E-mail fora do ar' LIMIT 1),(SELECT id FROM usuario WHERE email='ana.souza@helpsystem.com' LIMIT 1),'Houve instabilidade no servidor de e-mail, normalizando agora.',(NOW() - INTERVAL 2 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='Senha expirada' LIMIT 1),(SELECT id FROM usuario WHERE email='erika.nunes@helpsystem.com' LIMIT 1),'Enviamos uma nova senha temporária, troque no primeiro acesso.',(NOW() - INTERVAL 2 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='Erro 500 no portal' LIMIT 1),(SELECT id FROM usuario WHERE email='ana.souza@helpsystem.com' LIMIT 1),'Identificamos um bug, uma correção será publicada hoje.',(NOW() - INTERVAL 2 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='Nota fiscal duplicada' LIMIT 1),(SELECT id FROM usuario WHERE email='bruno.lima@helpsystem.com' LIMIT 1),'Cancelamos a NF duplicada no sistema, confira.',(NOW() - INTERVAL 2 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='Nota fiscal duplicada' LIMIT 1),(SELECT id FROM usuario WHERE email='diego.alves@helpsystem.com' LIMIT 1),'Confirmado, obrigado!',(NOW() - INTERVAL 3 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='Atualização do sistema' LIMIT 1),(SELECT id FROM usuario WHERE email='ana.souza@helpsystem.com' LIMIT 1),'A próxima versão está prevista para a semana que vem.',(NOW() - INTERVAL 2 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='Dúvida sobre holerite' LIMIT 1),(SELECT id FROM usuario WHERE email='gabriela.dias@helpsystem.com' LIMIT 1),'Menu RH > Documentos > Holerite, filtre pelo mês.',(NOW() - INTERVAL 2 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='Acesso remoto lento' LIMIT 1),(SELECT id FROM usuario WHERE email='ana.souza@helpsystem.com' LIMIT 1),'Aumentamos a banda do túnel, teste novamente por favor.',(NOW() - INTERVAL 2 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='Solicitar segundo monitor' LIMIT 1),(SELECT id FROM usuario WHERE email='bruno.lima@helpsystem.com' LIMIT 1),'Pedido registrado, aguardando aprovação do gestor.',(NOW() - INTERVAL 2 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='Reset de senha do ERP' LIMIT 1),(SELECT id FROM usuario WHERE email='erika.nunes@helpsystem.com' LIMIT 1),'Senha do ERP redefinida, verifique seu e-mail.',(NOW() - INTERVAL 2 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='Dúvida sobre vale-refeição' LIMIT 1),(SELECT id FROM usuario WHERE email='gabriela.dias@helpsystem.com' LIMIT 1),'O saldo aparece no app do benefício, posso te enviar o link.',(NOW() - INTERVAL 2 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='Sistema lento à tarde' LIMIT 1),(SELECT id FROM usuario WHERE email='ana.souza@helpsystem.com' LIMIT 1),'Otimizamos algumas consultas do banco, deve melhorar.',(NOW() - INTERVAL 2 HOUR)),
((SELECT id FROM solicitacao WHERE titulo='Sistema lento à tarde' LIMIT 1),(SELECT id FROM usuario WHERE email='diego.alves@helpsystem.com' LIMIT 1),'Melhorou bastante, obrigado!',(NOW() - INTERVAL 3 HOUR));

-- 5) Notificacoes (avisa o autor quando alguem respondeu)
INSERT INTO notificacao (usuario_id, solicitacao_id, mensagem, lida) VALUES
((SELECT id FROM usuario WHERE email='erika.nunes@helpsystem.com' LIMIT 1),(SELECT id FROM solicitacao WHERE titulo='Não consigo acessar o VPN' LIMIT 1),'Nova resposta na sua solicitacao: Não consigo acessar o VPN',TRUE),
((SELECT id FROM usuario WHERE email='carla.mendes@helpsystem.com' LIMIT 1),(SELECT id FROM solicitacao WHERE titulo='Solicitação de novo mouse' LIMIT 1),'Nova resposta na sua solicitacao: Solicitação de novo mouse',FALSE),
((SELECT id FROM usuario WHERE email='diego.alves@helpsystem.com' LIMIT 1),(SELECT id FROM solicitacao WHERE titulo='Erro ao emitir nota fiscal' LIMIT 1),'Nova resposta na sua solicitacao: Erro ao emitir nota fiscal',FALSE),
((SELECT id FROM usuario WHERE email='henrique.costa@helpsystem.com' LIMIT 1),(SELECT id FROM solicitacao WHERE titulo='Como solicitar férias?' LIMIT 1),'Nova resposta na sua solicitacao: Como solicitar férias?',TRUE),
((SELECT id FROM usuario WHERE email='joao.pereira@helpsystem.com' LIMIT 1),(SELECT id FROM solicitacao WHERE titulo='Instalação do Office' LIMIT 1),'Nova resposta na sua solicitacao: Instalação do Office',TRUE),
((SELECT id FROM usuario WHERE email='diego.alves@helpsystem.com' LIMIT 1),(SELECT id FROM solicitacao WHERE titulo='Acesso ao sistema financeiro' LIMIT 1),'Nova resposta na sua solicitacao: Acesso ao sistema financeiro',FALSE),
((SELECT id FROM usuario WHERE email='henrique.costa@helpsystem.com' LIMIT 1),(SELECT id FROM solicitacao WHERE titulo='Dúvida sobre reembolso' LIMIT 1),'Nova resposta na sua solicitacao: Dúvida sobre reembolso',TRUE),
((SELECT id FROM usuario WHERE email='carla.mendes@helpsystem.com' LIMIT 1),(SELECT id FROM solicitacao WHERE titulo='E-mail fora do ar' LIMIT 1),'Nova resposta na sua solicitacao: E-mail fora do ar',FALSE),
((SELECT id FROM usuario WHERE email='gabriela.dias@helpsystem.com' LIMIT 1),(SELECT id FROM solicitacao WHERE titulo='Senha expirada' LIMIT 1),'Nova resposta na sua solicitacao: Senha expirada',TRUE),
((SELECT id FROM usuario WHERE email='isabela.ramos@helpsystem.com' LIMIT 1),(SELECT id FROM solicitacao WHERE titulo='Erro 500 no portal' LIMIT 1),'Nova resposta na sua solicitacao: Erro 500 no portal',FALSE),
((SELECT id FROM usuario WHERE email='diego.alves@helpsystem.com' LIMIT 1),(SELECT id FROM solicitacao WHERE titulo='Nota fiscal duplicada' LIMIT 1),'Nova resposta na sua solicitacao: Nota fiscal duplicada',TRUE),
((SELECT id FROM usuario WHERE email='carla.mendes@helpsystem.com' LIMIT 1),(SELECT id FROM solicitacao WHERE titulo='Atualização do sistema' LIMIT 1),'Nova resposta na sua solicitacao: Atualização do sistema',FALSE),
((SELECT id FROM usuario WHERE email='henrique.costa@helpsystem.com' LIMIT 1),(SELECT id FROM solicitacao WHERE titulo='Dúvida sobre holerite' LIMIT 1),'Nova resposta na sua solicitacao: Dúvida sobre holerite',TRUE),
((SELECT id FROM usuario WHERE email='erika.nunes@helpsystem.com' LIMIT 1),(SELECT id FROM solicitacao WHERE titulo='Acesso remoto lento' LIMIT 1),'Nova resposta na sua solicitacao: Acesso remoto lento',FALSE),
((SELECT id FROM usuario WHERE email='isabela.ramos@helpsystem.com' LIMIT 1),(SELECT id FROM solicitacao WHERE titulo='Solicitar segundo monitor' LIMIT 1),'Nova resposta na sua solicitacao: Solicitar segundo monitor',FALSE),
((SELECT id FROM usuario WHERE email='joao.pereira@helpsystem.com' LIMIT 1),(SELECT id FROM solicitacao WHERE titulo='Reset de senha do ERP' LIMIT 1),'Nova resposta na sua solicitacao: Reset de senha do ERP',TRUE),
((SELECT id FROM usuario WHERE email='henrique.costa@helpsystem.com' LIMIT 1),(SELECT id FROM solicitacao WHERE titulo='Dúvida sobre vale-refeição' LIMIT 1),'Nova resposta na sua solicitacao: Dúvida sobre vale-refeição',FALSE),
((SELECT id FROM usuario WHERE email='diego.alves@helpsystem.com' LIMIT 1),(SELECT id FROM solicitacao WHERE titulo='Sistema lento à tarde' LIMIT 1),'Nova resposta na sua solicitacao: Sistema lento à tarde',TRUE);

COMMIT;

-- Conferencia rapida:
SELECT (SELECT COUNT(*) FROM usuario) AS usuarios, (SELECT COUNT(*) FROM solicitacao) AS solicitacoes, (SELECT COUNT(*) FROM resposta) AS respostas, (SELECT COUNT(*) FROM notificacao) AS notificacoes;