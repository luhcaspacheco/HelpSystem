package com.helpsystem.service;

import com.helpsystem.model.Departamento;
import com.helpsystem.repository.DepartamentoRepository;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

class DepartamentoServiceTest {

    private final DepartamentoRepository departamentoRepository = mock(DepartamentoRepository.class);
    private final DepartamentoService service = new DepartamentoService(departamentoRepository);

    @Test
    void criaDepartamentoValido() {
        Departamento departamento = new Departamento("TI");
        when(departamentoRepository.save(any(Departamento.class))).thenReturn(departamento);

        ResultadoOperacao resultado = service.criar(" TI ");

        assertTrue(resultado.isSucesso());
        verify(departamentoRepository).save(any(Departamento.class));
    }

    @Test
    void bloqueiaDepartamentoDuplicado() {
        when(departamentoRepository.existsByNomeIgnoreCase("TI")).thenReturn(true);

        ResultadoOperacao resultado = service.criar("TI");

        assertFalse(resultado.isSucesso());
        verify(departamentoRepository, never()).save(any(Departamento.class));
    }

    @Test
    void excluiDepartamentoExistente() {
        when(departamentoRepository.existsById(1)).thenReturn(true);

        ResultadoOperacao resultado = service.excluir(1);

        assertTrue(resultado.isSucesso());
        verify(departamentoRepository).deleteById(1);
    }

    @Test
    void bloqueiaExclusaoDeDepartamentoEmUso() {
        when(departamentoRepository.existsById(1)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("fk")).when(departamentoRepository).deleteById(1);

        ResultadoOperacao resultado = service.excluir(1);

        assertFalse(resultado.isSucesso());
    }
}
