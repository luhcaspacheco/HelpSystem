package com.helpsystem.service;

import com.helpsystem.model.Departamento;
import com.helpsystem.repository.DepartamentoRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;

    public DepartamentoService(DepartamentoRepository departamentoRepository) {
        this.departamentoRepository = departamentoRepository;
    }

    public List<Departamento> listar() {
        return departamentoRepository.findAllByOrderByNomeAsc();
    }
}
