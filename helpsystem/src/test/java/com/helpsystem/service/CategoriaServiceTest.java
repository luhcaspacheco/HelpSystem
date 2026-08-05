package com.helpsystem.service;

import com.helpsystem.model.Categoria;
import com.helpsystem.repository.CategoriaRepository;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategoriaServiceTest {

    private final CategoriaRepository categoriaRepository = mock(CategoriaRepository.class);
    private final CategoriaService service = new CategoriaService(categoriaRepository);

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
    void excluiCategoriaExistente() {
        when(categoriaRepository.existsById(1)).thenReturn(true);

        ResultadoOperacao resultado = service.excluir(1);

        assertTrue(resultado.isSucesso());
        verify(categoriaRepository).deleteById(1);
    }

    @Test
    void bloqueiaExclusaoDeCategoriaEmUso() {
        when(categoriaRepository.existsById(1)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("fk")).when(categoriaRepository).deleteById(1);

        ResultadoOperacao resultado = service.excluir(1);

        assertFalse(resultado.isSucesso());
    }
}
