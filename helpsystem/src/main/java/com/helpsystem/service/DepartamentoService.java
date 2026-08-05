package com.helpsystem.service;

import com.helpsystem.model.Departamento;
import com.helpsystem.repository.DepartamentoRepository;

import org.springframework.dao.DataIntegrityViolationException;
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

    public ResultadoOperacao criar(String nome) {
        if (nome == null || nome.isBlank()) {
            return ResultadoOperacao.erro("O nome do departamento é obrigatório.");
        }

        String nomeNormalizado = nome.trim();
        try {
            if (departamentoRepository.existsByNomeIgnoreCase(nomeNormalizado)) {
                return ResultadoOperacao.erro("Já existe um departamento com este nome.");
            }

            Departamento departamento = departamentoRepository.save(new Departamento(nomeNormalizado));
            return ResultadoOperacao.ok("Departamento criado com sucesso!", departamento);
        } catch (DataIntegrityViolationException e) {
            return ResultadoOperacao.erro("Já existe um departamento com este nome.");
        }
    }

    public ResultadoOperacao excluir(Integer id) {
        if (id == null) {
            return ResultadoOperacao.erro("Departamento não informado.");
        }

        if (!departamentoRepository.existsById(id)) {
            return ResultadoOperacao.erro("Departamento não encontrado.");
        }

        try {
            departamentoRepository.deleteById(id);
            return ResultadoOperacao.ok("Departamento excluído com sucesso!");
        } catch (DataIntegrityViolationException e) {
            return ResultadoOperacao.erro("Não é possível excluir um departamento que já está em uso.");
        }
    }
}
