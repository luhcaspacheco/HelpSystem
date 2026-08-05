package com.helpsystem.service;

import com.helpsystem.model.Notificacao;
import com.helpsystem.model.Usuario;
import com.helpsystem.repository.NotificacaoRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    public List<Notificacao> listar(Usuario usuarioLogado, Boolean lida) {
        if (usuarioLogado == null) {
            return List.of();
        }

        if (lida != null) {
            return notificacaoRepository.findByUsuarioIdAndLidaOrderByDataCriacaoDesc(usuarioLogado.getId(), lida);
        }

        return notificacaoRepository.findByUsuarioIdOrderByLidaAscDataCriacaoDesc(usuarioLogado.getId());
    }

    public long contarNaoLidas(Usuario usuarioLogado) {
        if (usuarioLogado == null) {
            return 0;
        }

        return notificacaoRepository.countByUsuarioIdAndLidaFalse(usuarioLogado.getId());
    }

    public ResultadoOperacao marcarComoLida(Integer id, Usuario usuarioLogado) {
        if (usuarioLogado == null) {
            return ResultadoOperacao.erro("Usuário logado não encontrado.");
        }

        Notificacao notificacao = notificacaoRepository.findById(id).orElse(null);
        if (notificacao == null) {
            return ResultadoOperacao.erro("Notificação não encontrada.");
        }
        if (notificacao.getUsuario() == null || notificacao.getUsuario().getId() != usuarioLogado.getId()) {
            return ResultadoOperacao.erro("Você não tem permissão para alterar esta notificação.");
        }

        notificacao.setLida(true);
        notificacaoRepository.save(notificacao);
        return ResultadoOperacao.ok("Notificação marcada como lida.", notificacao);
    }
}
