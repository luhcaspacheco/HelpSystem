package com.helpsystem.service;

import com.helpsystem.model.Solicitacao;
import com.helpsystem.model.Usuario;
import com.helpsystem.model.enums.TipoUsuario;
import com.helpsystem.repository.CategoriaRepository;
import com.helpsystem.repository.NotificacaoRepository;
import com.helpsystem.repository.RespostaRepository;
import com.helpsystem.repository.SolicitacaoRepository;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SolicitacaoServiceTest {

    private final SolicitacaoRepository solicitacaoRepository = mock(SolicitacaoRepository.class);
    private final CategoriaRepository categoriaRepository = mock(CategoriaRepository.class);
    private final RespostaRepository respostaRepository = mock(RespostaRepository.class);
    private final NotificacaoRepository notificacaoRepository = mock(NotificacaoRepository.class);
    private final SolicitacaoService service = new SolicitacaoService(
            solicitacaoRepository,
            categoriaRepository,
            respostaRepository,
            notificacaoRepository);

    @Test
    void repassaTermoNormalizadoAoListarSolicitacoes() {
        service.listar(null, null, null, "  VPN  ", "data");

        verify(solicitacaoRepository).filtrar(null, null, null, "vpn");
    }

    @Test
    void bloqueiaResolverSolicitacaoDeOutroAutor() {
        Usuario autorOriginal = new Usuario();
        autorOriginal.setId(1);

        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(2);

        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId(10);
        solicitacao.setAutor(autorOriginal);

        when(solicitacaoRepository.findById(10)).thenReturn(Optional.of(solicitacao));

        ResultadoOperacao resultado = service.resolver(10, outroUsuario);

        assertFalse(resultado.isSucesso());
        verify(solicitacaoRepository, never()).save(solicitacao);
    }

    @Test
    void permiteAdminResolverSolicitacaoDeOutroAutor() {
        Usuario autorOriginal = new Usuario();
        autorOriginal.setId(1);

        Usuario admin = new Usuario();
        admin.setId(2);
        admin.setTipo(TipoUsuario.ADMIN);

        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId(10);
        solicitacao.setAutor(autorOriginal);

        when(solicitacaoRepository.findById(10)).thenReturn(Optional.of(solicitacao));

        ResultadoOperacao resultado = service.resolver(10, admin);

        assertTrue(resultado.isSucesso());
        verify(solicitacaoRepository).save(solicitacao);
    }

    @Test
    void permiteAutorExcluirPropriaSolicitacao() {
        Usuario autor = new Usuario();
        autor.setId(1);

        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId(10);
        solicitacao.setAutor(autor);

        when(solicitacaoRepository.findById(10)).thenReturn(Optional.of(solicitacao));

        ResultadoOperacao resultado = service.excluir(10, autor);

        assertTrue(resultado.isSucesso());
        verify(notificacaoRepository).deleteBySolicitacaoId(10);
        verify(respostaRepository).deleteBySolicitacaoId(10);
        verify(solicitacaoRepository).delete(solicitacao);
    }

    @Test
    void bloqueiaExcluirSolicitacaoDeOutroAutor() {
        Usuario autor = new Usuario();
        autor.setId(1);

        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(2);

        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId(10);
        solicitacao.setAutor(autor);

        when(solicitacaoRepository.findById(10)).thenReturn(Optional.of(solicitacao));

        ResultadoOperacao resultado = service.excluir(10, outroUsuario);

        assertFalse(resultado.isSucesso());
        verify(solicitacaoRepository, never()).delete(solicitacao);
    }
}
