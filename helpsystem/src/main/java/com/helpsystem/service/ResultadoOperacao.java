package com.helpsystem.service;

import com.helpsystem.model.Usuario;
import com.helpsystem.model.Categoria;
import com.helpsystem.model.Notificacao;
import com.helpsystem.model.Resposta;
import com.helpsystem.model.Solicitacao;

public class ResultadoOperacao {

    private final boolean sucesso;
    private final String mensagem;
    private final Object dado;

    private ResultadoOperacao(boolean sucesso, String mensagem, Object dado) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.dado = dado;
    }

    public static ResultadoOperacao ok(String mensagem) {
        return new ResultadoOperacao(true, mensagem, null);
    }

    public static ResultadoOperacao ok(String mensagem, Object dado) {
        return new ResultadoOperacao(true, mensagem, dado);
    }

    public static ResultadoOperacao erro(String mensagem) {
        return new ResultadoOperacao(false, mensagem, null);
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public Object getDado() {
        return dado;
    }

    public Usuario getUsuario() {
        return (dado instanceof Usuario) ? (Usuario) dado : null;
    }

    public Categoria getCategoria() {
        return (dado instanceof Categoria) ? (Categoria) dado : null;
    }

    public Solicitacao getSolicitacao() {
        return (dado instanceof Solicitacao) ? (Solicitacao) dado : null;
    }

    public Resposta getResposta() {
        return (dado instanceof Resposta) ? (Resposta) dado : null;
    }

    public Notificacao getNotificacao() {
        return (dado instanceof Notificacao) ? (Notificacao) dado : null;
    }
}
