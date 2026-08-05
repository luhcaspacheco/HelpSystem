package com.helpsystem.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpsystem.model.Usuario;
import com.helpsystem.service.SessaoService;
import com.helpsystem.web.dto.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String USUARIO_LOGADO = "usuarioLogado";

    private final SessaoService sessoes;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(SessaoService sessoes, ObjectMapper objectMapper) {
        this.sessoes = sessoes;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (isPublico(request)) {
            return true;
        }

        String token = extrairToken(request.getHeader("Authorization"));
        Optional<Usuario> usuario = sessoes.buscarUsuario(token);
        if (usuario.isEmpty()) {
            escreverErro(response, HttpStatus.UNAUTHORIZED, "Token invalido ou expirado.");
            return false;
        }

        request.setAttribute(USUARIO_LOGADO, usuario.get());
        return true;
    }

    private boolean isPublico(HttpServletRequest request) {
        String metodo = request.getMethod();
        String caminho = request.getRequestURI();

        return "OPTIONS".equalsIgnoreCase(metodo)
                || ("POST".equalsIgnoreCase(metodo) && "/api/login".equals(caminho))
                || ("POST".equalsIgnoreCase(metodo) && "/api/usuarios".equals(caminho))
                || ("GET".equalsIgnoreCase(metodo) && "/api/departamentos".equals(caminho))
                || ("GET".equalsIgnoreCase(metodo) && "/api/categorias".equals(caminho));
    }

    private String extrairToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7).trim();
    }

    private void escreverErro(HttpServletResponse response, HttpStatus status, String mensagem) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(new ApiResponse(false, mensagem, null)));
    }
}
