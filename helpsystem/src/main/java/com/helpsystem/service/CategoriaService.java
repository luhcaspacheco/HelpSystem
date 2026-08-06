package com.helpsystem.service;

import com.helpsystem.model.Categoria;
import com.helpsystem.repository.CategoriaRepository;
import com.helpsystem.repository.SolicitacaoRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    /** Categoria padrão que recebe as solicitações órfãs e não pode ser excluída. */
    private static final String CATEGORIA_PADRAO = "Outros";

    private final CategoriaRepository categoriaRepository;
    private final SolicitacaoRepository solicitacaoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository,
                            SolicitacaoRepository solicitacaoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.solicitacaoRepository = solicitacaoRepository;
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

    @Transactional
    public ResultadoOperacao excluir(Integer id) {
        if (id == null) {
            return ResultadoOperacao.erro("Categoria não informada.");
        }

        Categoria categoria = categoriaRepository.findById(id).orElse(null);
        if (categoria == null) {
            return ResultadoOperacao.erro("Categoria não encontrada.");
        }

        // A categoria padrão não pode ser excluída (é o destino das órfãs).
        if (categoria.getNome() != null && categoria.getNome().trim().equalsIgnoreCase(CATEGORIA_PADRAO)) {
            return ResultadoOperacao.erro("A categoria padrão \"" + CATEGORIA_PADRAO + "\" não pode ser excluída.");
        }

        // Antes de excluir, move as solicitações desta categoria para a padrão,
        // evitando que fiquem sem categoria (órfãs).
        Categoria destino = categoriaRepository.findFirstByNomeIgnoreCase(CATEGORIA_PADRAO).orElse(null);
        if (destino == null) {
            return ResultadoOperacao.erro(
                "Mantenha uma categoria \"" + CATEGORIA_PADRAO + "\" para receber as solicitações antes de excluir.");
        }

        int movidas = solicitacaoRepository.reatribuirCategoria(id, destino);

        try {
            categoriaRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            return ResultadoOperacao.erro("Não é possível excluir esta categoria no momento.");
        }

        String msg = movidas > 0
            ? "Categoria excluída. " + movidas + " solicitação(ões) movida(s) para \"" + CATEGORIA_PADRAO + "\"."
            : "Categoria excluída com sucesso!";
        return ResultadoOperacao.ok(msg);
    }
}
