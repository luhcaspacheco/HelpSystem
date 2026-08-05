package com.helpsystem.service;

import com.helpsystem.model.Categoria;
import com.helpsystem.model.Solicitacao;
import com.helpsystem.model.Usuario;
import com.helpsystem.model.enums.Prioridade;
import com.helpsystem.model.enums.StatusSolicitacao;
import com.helpsystem.repository.CategoriaRepository;
import com.helpsystem.repository.SolicitacaoRepository;
import com.helpsystem.web.dto.SolicitacaoRequest;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final CategoriaRepository categoriaRepository;

    public SolicitacaoService(SolicitacaoRepository solicitacaoRepository, CategoriaRepository categoriaRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<Solicitacao> listar(StatusSolicitacao status, Integer categoriaId, Integer autorId, String ordenarPor) {
        Integer categoriaFiltro = normalizarId(categoriaId);
        Integer autorFiltro = normalizarId(autorId);

        if ("prioridade".equalsIgnoreCase(ordenarPor)) {
            return solicitacaoRepository.filtrarOrdenandoPorPrioridade(status, categoriaFiltro, autorFiltro);
        }

        return solicitacaoRepository.filtrar(status, categoriaFiltro, autorFiltro);
    }

    private Integer normalizarId(Integer id) {
        return id != null && id > 0 ? id : null;
    }

    public Optional<Solicitacao> buscarPorId(Integer id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return solicitacaoRepository.findById(id);
    }

    public ResultadoOperacao criar(SolicitacaoRequest req, Usuario autor) {
        if (autor == null) {
            return ResultadoOperacao.erro("Usuario logado nao encontrado.");
        }
        if (req == null) {
            return ResultadoOperacao.erro("Informe os dados da solicitacao.");
        }
        if (req.titulo == null || req.titulo.isBlank()) {
            return ResultadoOperacao.erro("O titulo e obrigatorio.");
        }
        if (req.descricao == null || req.descricao.isBlank()) {
            return ResultadoOperacao.erro("A descricao e obrigatoria.");
        }
        if (req.categoriaId == null || req.categoriaId <= 0) {
            return ResultadoOperacao.erro("A categoria e obrigatoria.");
        }

        Categoria categoria = categoriaRepository.findById(req.categoriaId).orElse(null);
        if (categoria == null) {
            return ResultadoOperacao.erro("Categoria nao encontrada.");
        }

        Prioridade prioridade = Prioridade.MEDIA;
        if (req.prioridade != null && !req.prioridade.isBlank()) {
            try {
                prioridade = Prioridade.valueOf(req.prioridade.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResultadoOperacao.erro("Prioridade invalida (use BAIXA, MEDIA ou ALTA).");
            }
        }

        Solicitacao solicitacao = new Solicitacao(
                req.titulo.trim(),
                req.descricao.trim(),
                autor,
                categoria,
                prioridade
        );

        solicitacaoRepository.save(solicitacao);
        return ResultadoOperacao.ok("Solicitacao criada com sucesso!", solicitacao);
    }

    public ResultadoOperacao resolver(Integer id, Usuario usuarioLogado) {
        if (usuarioLogado == null) {
            return ResultadoOperacao.erro("Usuario logado nao encontrado.");
        }

        Solicitacao solicitacao = buscarPorId(id).orElse(null);
        if (solicitacao == null) {
            return ResultadoOperacao.erro("Solicitacao nao encontrada.");
        }
        boolean usuarioEhAutor = solicitacao.getAutor() != null && solicitacao.getAutor().getId() == usuarioLogado.getId();
        if (!usuarioEhAutor && !usuarioLogado.isAdmin()) {
            return ResultadoOperacao.erro("Apenas o autor ou um admin pode marcar a solicitacao como resolvida.");
        }
        if (solicitacao.getStatus() == StatusSolicitacao.RESOLVIDA) {
            return ResultadoOperacao.erro("A solicitacao ja esta resolvida.");
        }

        solicitacao.resolver();
        solicitacaoRepository.save(solicitacao);
        return ResultadoOperacao.ok("Solicitacao marcada como resolvida.", solicitacao);
    }
}
