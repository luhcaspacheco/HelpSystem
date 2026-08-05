package com.helpsystem.service;

import com.helpsystem.model.Solicitacao;
import com.helpsystem.model.Usuario;
import com.helpsystem.repository.CategoriaRepository;
import com.helpsystem.repository.SolicitacaoRepository;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SolicitacaoServiceTest {

    private final SolicitacaoRepository solicitacaoRepository = mock(SolicitacaoRepository.class);
    private final CategoriaRepository categoriaRepository = mock(CategoriaRepository.class);
    private final SolicitacaoService service = new SolicitacaoService(solicitacaoRepository, categoriaRepository);

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
}
