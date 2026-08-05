package com.helpsystem.web;

import com.helpsystem.service.UsuarioService;
import com.helpsystem.web.dto.CadastroRequest;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class UsuarioControllerTest {

    private final UsuarioService usuarioService = mock(UsuarioService.class);
    private final UsuarioController controller = new UsuarioController(usuarioService);

    @Test
    void bloqueiaCadastroPublicoComoAdmin() {
        CadastroRequest request = new CadastroRequest();
        request.nome = "Admin indevido";
        request.email = "admin2@empresa.com";
        request.senha = "senha123";
        request.tipo = "ADMIN";
        request.departamentoId = 1;

        ResponseEntity<?> response = controller.cadastrar(request);

        assertEquals(403, response.getStatusCode().value());
        verify(usuarioService, never()).cadastrar(null, null, null, null, null);
    }
}
