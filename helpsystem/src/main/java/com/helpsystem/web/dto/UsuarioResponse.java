package com.helpsystem.web.dto;

import com.helpsystem.model.Usuario;
import com.helpsystem.model.enums.TipoUsuario;

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
    public boolean ativo;

    public UsuarioResponse(Usuario u) {
        TipoUsuario tipoUsuario = u.getTipo() != null ? u.getTipo() : TipoUsuario.COMUM;

        this.id = u.getId();
        this.nome = u.getNome();
        this.email = u.getEmail();
        this.tipo = tipoUsuario.name();
        this.admin = tipoUsuario == TipoUsuario.ADMIN;
        this.ativo = u.isAtivo();
    }
}
