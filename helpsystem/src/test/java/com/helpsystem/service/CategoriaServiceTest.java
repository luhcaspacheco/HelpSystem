package com.helpsystem.service;

import com.helpsystem.model.Categoria;
import com.helpsystem.repository.CategoriaRepository;
import com.helpsystem.repository.SolicitacaoRepository;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategoriaServiceTest {

    private final CategoriaRepository categoriaRepository = mock(CategoriaRepository.class);
    private final SolicitacaoRepository solicitacaoRepository = mock(SolicitacaoRepository.class);
    private final CategoriaService service = new CategoriaService(categoriaRepository, solicitacaoRepository);

    @Test
    void criaCategoriaValida() {
        Categoria categoria = new Categoria("Sistemas");
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        ResultadoOperacao resultado = service.criar(" Sistemas ");

        assertTrue(resultado.isSucesso());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    void bloqueiaCategoriaDuplicada() {
        when(categoriaRepository.existsByNomeIgnoreCase("Sistemas")).thenReturn(true);

        ResultadoOperacao resultado = service.criar("Sistemas");

        assertFalse(resultado.isSucesso());
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void excluiCategoriaReatribuindoParaOutros() {
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(new Categoria("Sistemas")));
        when(categoriaRepository.findFirstByNomeIgnoreCase("Outros")).thenReturn(Optional.of(new Categoria("Outros")));

        ResultadoOperacao resultado = service.excluir(1);

        assertTrue(resultado.isSucesso());
        verify(solicitacaoRepository).reatribuirCategoria(eq(1), any(Categoria.class));
        verify(categoriaRepository).deleteById(1);
    }

    @Test
    void bloqueiaExclusaoDaCategoriaPadrao() {
        when(categoriaRepository.findById(9)).thenReturn(Optional.of(new Categoria("Outros")));

        ResultadoOperacao resultado = service.excluir(9);

        assertFalse(resultado.isSucesso());
        verify(categoriaRepository, never()).deleteById(any());
    }

    @Test
    void bloqueiaExclusaoQuandoBancoRecusa() {
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(new Categoria("Sistemas")));
        when(categoriaRepository.findFirstByNomeIgnoreCase("Outros")).thenReturn(Optional.of(new Categoria("Outros")));
        doThrow(new DataIntegrityViolationException("fk")).when(categoriaRepository).deleteById(1);

        ResultadoOperacao resultado = service.excluir(1);

        assertFalse(resultado.isSucesso());
    }
}
