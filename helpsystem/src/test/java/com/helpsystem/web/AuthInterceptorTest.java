package com.helpsystem.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpsystem.model.Usuario;
import com.helpsystem.service.SessaoService;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthInterceptorTest {

    private final SessaoService sessoes = mock(SessaoService.class);
    private final AuthInterceptor interceptor = new AuthInterceptor(sessoes, new ObjectMapper());

    @Test
    void bloqueiaRotaProtegidaSemToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/solicitacoes");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean liberado = interceptor.preHandle(request, response, null);

        assertFalse(liberado);
        assertEquals(401, response.getStatus());
    }

    @Test
    void liberaRotaProtegidaComTokenValido() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(7);
        when(sessoes.buscarUsuario("token-valido")).thenReturn(Optional.of(usuario));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/solicitacoes");
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean liberado = interceptor.preHandle(request, response, null);

        assertTrue(liberado);
        assertSame(usuario, request.getAttribute(AuthInterceptor.USUARIO_LOGADO));
    }
}
