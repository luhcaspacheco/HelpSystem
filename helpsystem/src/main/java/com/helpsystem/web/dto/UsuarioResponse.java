package com.helpsystem.web.dto;

import com.helpsystem.model.Usuario;

/**
 * Representação segura do usuário para a resposta JSON.
 * NUNCA expõe a senha nem o hash.
 */
public class UsuarioResponse {
    public int id;
    public String nome;
    public String email;
    public String tipo;
    public boolean admin;

    public UsuarioResponse(Usuario u) {
        this.id = u.getId();
        this.nome = u.getNome();
        this.email = u.getEmail();
        this.tipo = u.getTipo().name();
        this.admin = u.isAdmin();
    }
}
