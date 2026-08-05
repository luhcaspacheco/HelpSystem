package com.helpsystem.web.dto;

import com.helpsystem.model.Departamento;

public class DepartamentoResponse {
    public int id;
    public String nome;

    public DepartamentoResponse(Departamento departamento) {
        this.id = departamento.getId();
        this.nome = departamento.getNome();
    }
}
