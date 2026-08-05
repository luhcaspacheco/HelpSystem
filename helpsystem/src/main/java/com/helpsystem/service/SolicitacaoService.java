package com.helpsystem.service;

import com.helpsystem.model.Categoria;
import com.helpsystem.model.Solicitacao;
import com.helpsystem.model.Usuario;
import com.helpsystem.model.enums.Prioridade;
import com.helpsystem.model.enums.StatusSolicitacao;
import com.helpsystem.repository.CategoriaRepository;
import com.helpsystem.repository.NotificacaoRepository;
import com.helpsystem.repository.RespostaRepository;
import com.helpsystem.repository.SolicitacaoRepository;
import com.helpsystem.web.dto.SolicitacaoRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final CategoriaRepository categoriaRepository;
    private final RespostaRepository respostaRepository;
    private final NotificacaoRepository notificacaoRepository;

    public SolicitacaoService(SolicitacaoRepository solicitacaoRepository,
                              CategoriaRepository categoriaRepository,
                              RespostaRepository respostaRepository,
                              NotificacaoRepository notificacaoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.categoriaRepository = categoriaRepository;
        this.respostaRepository = respostaRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    public List<Solicitacao> listar(
            StatusSolicitacao status,
            Integer categoriaId,
            Integer autorId,
            String termo,
            String ordenarPor) {
        Integer categoriaFiltro = normalizarId(categoriaId);
        Integer autorFiltro = normalizarId(autorId);
        String termoFiltro = normalizarTermo(termo);

        if ("prioridade".equalsIgnoreCase(ordenarPor)) {
            return solicitacaoRepository.filtrarOrdenandoPorPrioridade(status, categoriaFiltro, autorFiltro, termoFiltro);
        }

        return solicitacaoRepository.filtrar(status, categoriaFiltro, autorFiltro, termoFiltro);
    }

    private Integer normalizarId(Integer id) {
        return id != null && id > 0 ? id : null;
    }

    private String normalizarTermo(String termo) {
        return termo != null && !termo.isBlank() ? termo.trim().toLowerCase() : null;
    }

    public Optional<Solicitacao> buscarPorId(Integer id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return solicitacaoRepository.findById(id);
    }

    public ResultadoOperacao criar(SolicitacaoRequest req, Usuario autor) {
        if (autor == null) {
            return ResultadoOperacao.erro("Usuário logado não encontrado.");
        }
        if (req == null) {
            return ResultadoOperacao.erro("Informe os dados da solicitação.");
        }
        if (req.titulo == null || req.titulo.isBlank()) {
            return ResultadoOperacao.erro("O título é obrigatório.");
        }
        if (req.descricao == null || req.descricao.isBlank()) {
            return ResultadoOperacao.erro("A descrição é obrigatória.");
        }
        if (req.categoriaId == null || req.categoriaId <= 0) {
            return ResultadoOperacao.erro("A categoria é obrigatória.");
        }

        Categoria categoria = categoriaRepository.findById(req.categoriaId).orElse(null);
        if (categoria == null) {
            return ResultadoOperacao.erro("Categoria não encontrada.");
        }

        Prioridade prioridade = Prioridade.MEDIA;
        if (req.prioridade != null && !req.prioridade.isBlank()) {
            try {
                prioridade = Prioridade.valueOf(req.prioridade.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResultadoOperacao.erro("Prioridade inválida (use BAIXA, MEDIA ou ALTA).");
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
        return ResultadoOperacao.ok("Solicitação criada com sucesso!", solicitacao);
    }

    public ResultadoOperacao resolver(Integer id, Usuario usuarioLogado) {
        if (usuarioLogado == null) {
            return ResultadoOperacao.erro("Usuário logado não encontrado.");
        }

        Solicitacao solicitacao = buscarPorId(id).orElse(null);
        if (solicitacao == null) {
            return ResultadoOperacao.erro("Solicitação não encontrada.");
        }
        boolean usuarioEhAutor = solicitacao.getAutor() != null && solicitacao.getAutor().getId() == usuarioLogado.getId();
        if (!usuarioEhAutor && !usuarioLogado.isAdmin()) {
            return ResultadoOperacao.erro("Apenas o autor ou um administrador pode marcar a solicitação como resolvida.");
        }
        if (solicitacao.getStatus() == StatusSolicitacao.RESOLVIDA) {
            return ResultadoOperacao.erro("A solicitação já está resolvida.");
        }

        solicitacao.resolver();
        solicitacaoRepository.save(solicitacao);
        return ResultadoOperacao.ok("Solicitação marcada como resolvida.", solicitacao);
    }

    @Transactional
    public ResultadoOperacao excluir(Integer id, Usuario usuarioLogado) {
        if (usuarioLogado == null) {
            return ResultadoOperacao.erro("Usuário logado não encontrado.");
        }

        Solicitacao solicitacao = buscarPorId(id).orElse(null);
        if (solicitacao == null) {
            return ResultadoOperacao.erro("Solicitação não encontrada.");
        }
        if (solicitacao.getAutor() == null || solicitacao.getAutor().getId() != usuarioLogado.getId()) {
            return ResultadoOperacao.erro("Apenas o autor pode excluir a própria solicitação.");
        }

        notificacaoRepository.deleteBySolicitacaoId(solicitacao.getId());
        respostaRepository.deleteBySolicitacaoId(solicitacao.getId());
        solicitacaoRepository.delete(solicitacao);
        return ResultadoOperacao.ok("Solicitação excluída com sucesso.");
    }
}
