package com.helpsystem.service;

import com.helpsystem.model.Departamento;
import com.helpsystem.model.Usuario;
import com.helpsystem.model.enums.TipoUsuario;
import com.helpsystem.repository.DepartamentoRepository;
import com.helpsystem.repository.UsuarioRepository;
import com.helpsystem.util.PasswordUtil;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final DepartamentoRepository departamentoRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, DepartamentoRepository departamentoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.departamentoRepository = departamentoRepository;
    }

    public ResultadoOperacao cadastrar(String nome, String email, String senhaPura,
                                       TipoUsuario tipo, Departamento departamento) {

        if (nome == null || nome.isBlank()) {
            return ResultadoOperacao.erro("O nome e obrigatorio.");
        }
        if (email == null || email.isBlank()) {
            return ResultadoOperacao.erro("O e-mail e obrigatorio.");
        }
        if (!emailValido(email)) {
            return ResultadoOperacao.erro("O e-mail informado nao e valido.");
        }
        if (senhaPura == null || senhaPura.isBlank()) {
            return ResultadoOperacao.erro("A senha e obrigatoria.");
        }
        if (departamento == null || departamento.getId() <= 0) {
            return ResultadoOperacao.erro("O departamento e obrigatorio.");
        }
        if (tipo == null) {
            tipo = TipoUsuario.COMUM;
        }

        Departamento departamentoEncontrado = departamentoRepository.findById(departamento.getId()).orElse(null);
        if (departamentoEncontrado == null) {
            return ResultadoOperacao.erro("Departamento nao encontrado.");
        }

        String emailNormalizado = email.trim().toLowerCase();

        try {
            if (usuarioRepository.existsByEmail(emailNormalizado)) {
                return ResultadoOperacao.erro("Ja existe um usuario com este e-mail.");
            }

            String senhaHash = PasswordUtil.gerarHash(senhaPura);
            Usuario novo = new Usuario(nome.trim(), emailNormalizado, senhaHash, tipo, departamentoEncontrado);
            usuarioRepository.save(novo);

            return ResultadoOperacao.ok("Usuario cadastrado com sucesso!", novo);

        } catch (DataIntegrityViolationException e) {
            return ResultadoOperacao.erro("Ja existe um usuario com este e-mail.");
        }
    }

    private boolean emailValido(String email) {
        return email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }
}
