package com.helpsystem.service;

import com.helpsystem.model.Usuario;
import com.helpsystem.repository.UsuarioRepository;
import com.helpsystem.util.PasswordUtil;

import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;

    public AutenticacaoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public ResultadoOperacao login(String email, String senha) {
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            return ResultadoOperacao.erro("Informe e-mail e senha.");
        }
        if (!email.trim().toLowerCase().contains("@helpsystem.")) {
            return ResultadoOperacao.erro("Acesso permitido apenas com e-mail corporativo do domínio @helpsystem.");
        }

        Usuario usuario = usuarioRepository.findByEmail(email.trim().toLowerCase()).orElse(null);

        if (usuario == null || !PasswordUtil.conferir(senha, usuario.getSenhaHash())) {
            return ResultadoOperacao.erro("E-mail ou senha inválidos.");
        }
        if (!usuario.isAtivo()) {
            return ResultadoOperacao.erro("Usuário inativo. Procure o administrador.");
        }

        return ResultadoOperacao.ok("Login efetuado com sucesso!", usuario);
    }
}
