package com.helpsystem.service;

import com.helpsystem.model.Departamento;
import com.helpsystem.model.Usuario;
import com.helpsystem.model.enums.TipoUsuario;
import com.helpsystem.repository.DepartamentoRepository;
import com.helpsystem.repository.UsuarioRepository;
import com.helpsystem.util.PasswordUtil;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

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
            return ResultadoOperacao.erro("O nome é obrigatório.");
        }
        if (email == null || email.isBlank()) {
            return ResultadoOperacao.erro("O e-mail é obrigatório.");
        }
        if (!emailValido(email)) {
            return ResultadoOperacao.erro("O e-mail informado não é válido.");
        }
        if (senhaPura == null || senhaPura.isBlank()) {
            return ResultadoOperacao.erro("A senha é obrigatória.");
        }
        if (departamento == null || departamento.getId() <= 0) {
            return ResultadoOperacao.erro("O departamento é obrigatório.");
        }
        if (tipo == null) {
            tipo = TipoUsuario.COMUM;
        }

        Departamento departamentoEncontrado = departamentoRepository.findById(departamento.getId()).orElse(null);
        if (departamentoEncontrado == null) {
            return ResultadoOperacao.erro("Departamento não encontrado.");
        }

        String emailNormalizado = email.trim().toLowerCase();

        try {
            if (usuarioRepository.existsByEmail(emailNormalizado)) {
                return ResultadoOperacao.erro("Já existe um usuário com este e-mail.");
            }

            String senhaHash = PasswordUtil.gerarHash(senhaPura);
            Usuario novo = new Usuario(nome.trim(), emailNormalizado, senhaHash, tipo, departamentoEncontrado);
            usuarioRepository.save(novo);

            return ResultadoOperacao.ok("Usuário cadastrado com sucesso!", novo);

        } catch (DataIntegrityViolationException e) {
            return ResultadoOperacao.erro("Já existe um usuário com este e-mail.");
        }
    }

    public List<Usuario> listar(Usuario usuarioLogado) {
        if (usuarioLogado == null || !usuarioLogado.isAdmin()) {
            return List.of();
        }
        return usuarioRepository.findAllByOrderByNomeAsc();
    }

    public ResultadoOperacao alterarTipo(Integer id, String tipo, Usuario usuarioLogado) {
        if (usuarioLogado == null || !usuarioLogado.isAdmin()) {
            return ResultadoOperacao.erro("Apenas administradores podem alterar o tipo de usuário.");
        }
        if (id == null || id <= 0) {
            return ResultadoOperacao.erro("Usuário não encontrado.");
        }
        if (tipo == null || tipo.isBlank()) {
            return ResultadoOperacao.erro("Tipo de usuário inválido (use ADMIN ou COMUM).");
        }

        TipoUsuario novoTipo;
        try {
            novoTipo = TipoUsuario.valueOf(tipo.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResultadoOperacao.erro("Tipo de usuário inválido (use ADMIN ou COMUM).");
        }

        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            return ResultadoOperacao.erro("Usuário não encontrado.");
        }
        if (usuario.getId() == usuarioLogado.getId() && novoTipo == TipoUsuario.COMUM) {
            return ResultadoOperacao.erro("O administrador não pode remover o próprio acesso.");
        }

        usuario.setTipo(novoTipo);
        usuarioRepository.save(usuario);
        return ResultadoOperacao.ok("Tipo de usuário atualizado com sucesso.", usuario);
    }

    public ResultadoOperacao excluir(Integer id, Usuario usuarioLogado) {
        if (usuarioLogado == null || !usuarioLogado.isAdmin()) {
            return ResultadoOperacao.erro("Apenas administradores podem excluir usuários.");
        }
        if (id == null || id <= 0) {
            return ResultadoOperacao.erro("Usuário não encontrado.");
        }

        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            return ResultadoOperacao.erro("Usuário não encontrado.");
        }
        if (usuario.getId() == usuarioLogado.getId()) {
            return ResultadoOperacao.erro("O administrador não pode excluir o próprio usuário.");
        }
        if (!usuario.isAtivo()) {
            return ResultadoOperacao.erro("Usuário já está inativo.");
        }

        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
        return ResultadoOperacao.ok("Usuário excluído com sucesso.", usuario);
    }

    public ResultadoOperacao reativar(Integer id, Usuario usuarioLogado) {
        if (usuarioLogado == null || !usuarioLogado.isAdmin()) {
            return ResultadoOperacao.erro("Apenas administradores podem reativar usuários.");
        }
        if (id == null || id <= 0) {
            return ResultadoOperacao.erro("Usuário não encontrado.");
        }

        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            return ResultadoOperacao.erro("Usuário não encontrado.");
        }
        if (usuario.isAtivo()) {
            return ResultadoOperacao.erro("Usuário já está ativo.");
        }

        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
        return ResultadoOperacao.ok("Usuário reativado com sucesso.", usuario);
    }

    private boolean emailValido(String email) {
        return email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }
}
