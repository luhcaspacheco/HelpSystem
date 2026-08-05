package com.helpsystem.service;

import com.helpsystem.model.Usuario;
import com.helpsystem.model.enums.TipoUsuario;
import com.helpsystem.repository.DepartamentoRepository;
import com.helpsystem.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioServiceTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final DepartamentoRepository departamentoRepository = mock(DepartamentoRepository.class);
    private final UsuarioService service = new UsuarioService(usuarioRepository, departamentoRepository);

    @Test
    void permiteAdminPromoverUsuarioComum() {
        Usuario admin = new Usuario();
        admin.setId(1);
        admin.setTipo(TipoUsuario.ADMIN);

        Usuario usuario = new Usuario();
        usuario.setId(2);
        usuario.setTipo(TipoUsuario.COMUM);

        when(usuarioRepository.findById(2)).thenReturn(Optional.of(usuario));

        ResultadoOperacao resultado = service.alterarTipo(2, "ADMIN", admin);

        assertTrue(resultado.isSucesso());
        assertEquals(TipoUsuario.ADMIN, usuario.getTipo());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void bloqueiaUsuarioComumAoAlterarTipo() {
        Usuario comum = new Usuario();
        comum.setId(1);
        comum.setTipo(TipoUsuario.COMUM);

        ResultadoOperacao resultado = service.alterarTipo(2, "ADMIN", comum);

        assertFalse(resultado.isSucesso());
        verify(usuarioRepository, never()).findById(2);
    }

    @Test
    void bloqueiaAdminAoRemoverProprioAcesso() {
        Usuario admin = new Usuario();
        admin.setId(1);
        admin.setTipo(TipoUsuario.ADMIN);

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(admin));

        ResultadoOperacao resultado = service.alterarTipo(1, "COMUM", admin);

        assertFalse(resultado.isSucesso());
        assertEquals(TipoUsuario.ADMIN, admin.getTipo());
        verify(usuarioRepository, never()).save(admin);
    }

    @Test
    void permiteAdminExcluirOutroUsuario() {
        Usuario admin = new Usuario();
        admin.setId(1);
        admin.setTipo(TipoUsuario.ADMIN);

        Usuario usuario = new Usuario();
        usuario.setId(2);
        usuario.setAtivo(true);

        when(usuarioRepository.findById(2)).thenReturn(Optional.of(usuario));

        ResultadoOperacao resultado = service.excluir(2, admin);

        assertTrue(resultado.isSucesso());
        assertFalse(usuario.isAtivo());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void bloqueiaUsuarioComumAoExcluirUsuario() {
        Usuario comum = new Usuario();
        comum.setId(1);
        comum.setTipo(TipoUsuario.COMUM);

        ResultadoOperacao resultado = service.excluir(2, comum);

        assertFalse(resultado.isSucesso());
        verify(usuarioRepository, never()).findById(2);
    }

    @Test
    void bloqueiaAdminAoExcluirProprioUsuario() {
        Usuario admin = new Usuario();
        admin.setId(1);
        admin.setTipo(TipoUsuario.ADMIN);

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(admin));

        ResultadoOperacao resultado = service.excluir(1, admin);

        assertFalse(resultado.isSucesso());
        verify(usuarioRepository, never()).save(admin);
    }
}
