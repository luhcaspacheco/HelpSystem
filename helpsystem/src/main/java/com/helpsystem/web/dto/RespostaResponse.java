package com.helpsystem.web.dto;

import com.helpsystem.model.Resposta;
import com.helpsystem.model.Usuario;

import java.time.LocalDateTime;

public class RespostaResponse {
    public int id;
    public int solicitacaoId;
    public String texto;
    public LocalDateTime dataCriacao;
    public Integer autorId;
    public String autorNome;

    public RespostaResponse(Resposta resposta) {
        Usuario autor = resposta.getAutor();

        this.id = resposta.getId();
        this.solicitacaoId = resposta.getSolicitacao().getId();
        this.texto = resposta.getTexto();
        this.dataCriacao = resposta.getDataCriacao();
        this.autorId = autor != null ? autor.getId() : null;
        this.autorNome = autor != null ? autor.getNome() : null;
    }
}
