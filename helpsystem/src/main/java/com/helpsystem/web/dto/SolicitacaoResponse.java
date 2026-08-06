package com.helpsystem.web.dto;

import com.helpsystem.model.Categoria;
import com.helpsystem.model.Solicitacao;
import com.helpsystem.model.Usuario;

import java.time.LocalDateTime;

public class SolicitacaoResponse {
    public int id;
    public String titulo;
    public String descricao;
    public String prioridade;
    public String status;
    public LocalDateTime dataCriacao;
    public LocalDateTime dataResolucao;
    public Integer autorId;
    public String autorNome;
    public String autorDepartamento;
    public Integer categoriaId;
    public String categoriaNome;

    public SolicitacaoResponse(Solicitacao solicitacao) {
        Usuario autor = solicitacao.getAutor();
        Categoria categoria = solicitacao.getCategoria();

        this.id = solicitacao.getId();
        this.titulo = solicitacao.getTitulo();
        this.descricao = solicitacao.getDescricao();
        this.prioridade = solicitacao.getPrioridade().name();
        this.status = solicitacao.getStatus().name();
        this.dataCriacao = solicitacao.getDataCriacao();
        this.dataResolucao = solicitacao.getDataResolucao();
        this.autorId = autor != null ? autor.getId() : null;
        this.autorNome = autor != null ? autor.getNome() : null;
        this.autorDepartamento = (autor != null && autor.getDepartamento() != null)
                ? autor.getDepartamento().getNome() : null;
        this.categoriaId = categoria != null ? categoria.getId() : null;
        this.categoriaNome = categoria != null ? categoria.getNome() : null;
    }
}
