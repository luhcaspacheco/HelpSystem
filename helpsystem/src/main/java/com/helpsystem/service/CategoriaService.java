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
            return ResultadoOperacao.erro("O nome da categoria e obrigatorio.");
        }

        String nomeNormalizado = nome.trim();
        try {
            if (categoriaRepository.existsByNomeIgnoreCase(nomeNormalizado)) {
                return ResultadoOperacao.erro("Ja existe uma categoria com este nome.");
            }

            Categoria categoria = categoriaRepository.save(new Categoria(nomeNormalizado));
            return ResultadoOperacao.ok("Categoria criada com sucesso!", categoria);
        } catch (DataIntegrityViolationException e) {
            return ResultadoOperacao.erro("Ja existe uma categoria com este nome.");
        }
    }
}
