package com.helpsystem.service;

import com.helpsystem.model.Categoria;
import com.helpsystem.repository.CategoriaRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> listar() {
        return categoriaRepository.findAllByOrderByNomeAsc();
    }

    public ResultadoOperacao criar(String nome) {
        if (nome == null || nome.isBlank()) {
            return ResultadoOperacao.erro("O nome da categoria é obrigatório.");
        }

        String nomeNormalizado = nome.trim();
        try {
            if (categoriaRepository.existsByNomeIgnoreCase(nomeNormalizado)) {
                return ResultadoOperacao.erro("Já existe uma categoria com este nome.");
            }

            Categoria categoria = categoriaRepository.save(new Categoria(nomeNormalizado));
            return ResultadoOperacao.ok("Categoria criada com sucesso!", categoria);
        } catch (DataIntegrityViolationException e) {
            return ResultadoOperacao.erro("Já existe uma categoria com este nome.");
        }
    }

    public ResultadoOperacao excluir(Integer id) {
        if (id == null) {
            return ResultadoOperacao.erro("Categoria não informada.");
        }

        if (!categoriaRepository.existsById(id)) {
            return ResultadoOperacao.erro("Categoria não encontrada.");
        }

        try {
            categoriaRepository.deleteById(id);
            return ResultadoOperacao.ok("Categoria excluída com sucesso!");
        } catch (DataIntegrityViolationException e) {
            return ResultadoOperacao.erro("Não é possível excluir uma categoria que já está em uso.");
        }
    }
}
