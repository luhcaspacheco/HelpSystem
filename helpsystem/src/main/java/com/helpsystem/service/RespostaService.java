package com.helpsystem.service;

import com.helpsystem.model.Notificacao;
import com.helpsystem.model.Resposta;
import com.helpsystem.model.Solicitacao;
import com.helpsystem.model.Usuario;
import com.helpsystem.model.enums.StatusSolicitacao;
import com.helpsystem.repository.NotificacaoRepository;
import com.helpsystem.repository.RespostaRepository;
import com.helpsystem.repository.SolicitacaoRepository;
import com.helpsystem.web.dto.RespostaRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RespostaService {

    private final RespostaRepository respostaRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final NotificacaoRepository notificacaoRepository;

    public RespostaService(RespostaRepository respostaRepository,
                           SolicitacaoRepository solicitacaoRepository,
                           NotificacaoRepository notificacaoRepository) {
        this.respostaRepository = respostaRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    public List<Resposta> listarPorSolicitacao(Integer solicitacaoId) {
        return respostaRepository.findBySolicitacaoIdOrderByDataCriacaoAsc(solicitacaoId);
    }

    @Transactional
    public ResultadoOperacao responder(Integer solicitacaoId, RespostaRequest req, Usuario autor) {
        if (autor == null) {
            return ResultadoOperacao.erro("Usuário logado não encontrado.");
        }
        if (req == null || req.texto == null || req.texto.isBlank()) {
            return ResultadoOperacao.erro("O texto da resposta é obrigatório.");
        }

        Solicitacao solicitacao = solicitacaoRepository.findById(solicitacaoId).orElse(null);
        if (solicitacao == null) {
            return ResultadoOperacao.erro("Solicitação não encontrada.");
        }
        if (solicitacao.getStatus() == StatusSolicitacao.RESOLVIDA) {
            return ResultadoOperacao.erro("Não é possível responder a uma solicitação resolvida.");
        }

        Resposta resposta = respostaRepository.save(new Resposta(solicitacao, autor, req.texto.trim()));

        if (solicitacao.getStatus() == StatusSolicitacao.ABERTA) {
            solicitacao.setStatus(StatusSolicitacao.RESPONDIDA);
            solicitacaoRepository.save(solicitacao);
        }

        criarNotificacaoParaAutor(solicitacao, autor);

        return ResultadoOperacao.ok("Resposta registrada com sucesso!", resposta);
    }

    private void criarNotificacaoParaAutor(Solicitacao solicitacao, Usuario autorResposta) {
        Usuario autorSolicitacao = solicitacao.getAutor();
        if (autorSolicitacao == null || autorSolicitacao.getId() == autorResposta.getId()) {
            return;
        }

        String mensagem = "Sua solicitação #" + solicitacao.getId() + " recebeu uma nova resposta.";
        notificacaoRepository.save(new Notificacao(autorSolicitacao, solicitacao, mensagem));
    }
}
