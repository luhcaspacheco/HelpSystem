package com.helpsystem.web.dto;

import com.helpsystem.model.Usuario;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UsuarioResponseTest {

    @Test
    void trataUsuarioSemTipoComoComum() {
        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setNome("Usuario antigo");
        usuario.setEmail("antigo@empresa.com");
        usuario.setTipo(null);

        UsuarioResponse response = new UsuarioResponse(usuario);

        assertEquals("COMUM", response.tipo);
        assertFalse(response.admin);
    }
}
