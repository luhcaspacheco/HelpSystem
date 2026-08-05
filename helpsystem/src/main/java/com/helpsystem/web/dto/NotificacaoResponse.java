package com.helpsystem.web.dto;

import com.helpsystem.model.Notificacao;
import com.helpsystem.model.Solicitacao;

import java.time.LocalDateTime;

public class NotificacaoResponse {
    public int id;
    public String mensagem;
    public boolean lida;
    public LocalDateTime dataCriacao;
    public Integer solicitacaoId;
    public String solicitacaoTitulo;

    public NotificacaoResponse(Notificacao notificacao) {
        Solicitacao solicitacao = notificacao.getSolicitacao();

        this.id = notificacao.getId();
        this.mensagem = notificacao.getMensagem();
        this.lida = notificacao.isLida();
        this.dataCriacao = notificacao.getDataCriacao();
        this.solicitacaoId = solicitacao != null ? solicitacao.getId() : null;
        this.solicitacaoTitulo = solicitacao != null ? solicitacao.getTitulo() : null;
    }
}
