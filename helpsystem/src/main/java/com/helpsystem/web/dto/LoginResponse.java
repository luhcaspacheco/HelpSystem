package com.helpsystem.web.dto;

import com.helpsystem.model.Usuario;

public class LoginResponse {
    public String token;
    public UsuarioResponse usuario;

    public LoginResponse(String token, Usuario usuario) {
        this.token = token;
        this.usuario = new UsuarioResponse(usuario);
    }
}
