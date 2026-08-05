package com.helpsystem.web;

import com.helpsystem.model.Usuario;
import com.helpsystem.model.enums.TipoUsuario;
import com.helpsystem.service.CategoriaService;
import com.helpsystem.web.dto.CategoriaRequest;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategoriaControllerTest {

    private final CategoriaService categoriaService = mock(CategoriaService.class);
    private final CategoriaController controller = new CategoriaController(categoriaService);

    @Test
    void bloqueiaUsuarioComumAoCriarCategoria() {
        Usuario usuarioComum = new Usuario();
        usuarioComum.setTipo(TipoUsuario.COMUM);

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getAttribute(AuthInterceptor.USUARIO_LOGADO)).thenReturn(usuarioComum);

        CategoriaRequest request = new CategoriaRequest();
        request.nome = "Categoria restrita";

        ResponseEntity<?> response = controller.criar(request, servletRequest);

        assertEquals(403, response.getStatusCode().value());
        verify(categoriaService, never()).criar(request.nome);
    }
}
