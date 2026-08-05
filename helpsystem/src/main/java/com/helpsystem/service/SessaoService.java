package com.helpsystem.service;

import com.helpsystem.model.Usuario;
import com.helpsystem.repository.UsuarioRepository;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class SessaoService {

    private final UsuarioRepository usuarioRepository;
    private final ConcurrentMap<String, Integer> sessoes = new ConcurrentHashMap<>();

    public SessaoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public String criarToken(Usuario usuario) {
        String token = UUID.randomUUID().toString();
        sessoes.put(token, usuario.getId());
        return token;
    }

    public Optional<Usuario> buscarUsuario(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        Integer usuarioId = sessoes.get(token);
        if (usuarioId == null) {
            return Optional.empty();
        }

        Optional<Usuario> usuario = usuarioRepository.findById(usuarioId);
        if (usuario.isEmpty() || !usuario.get().isAtivo()) {
            sessoes.remove(token);
            return Optional.empty();
        }

        return usuario;
    }

    public boolean invalidar(String token) {
        return token != null && sessoes.remove(token) != null;
    }
}
