package com.helpsystem.web.dto;

import com.helpsystem.model.Categoria;

public class CategoriaResponse {
    public int id;
    public String nome;

    public CategoriaResponse(Categoria categoria) {
        this.id = categoria.getId();
        this.nome = categoria.getNome();
    }
}
