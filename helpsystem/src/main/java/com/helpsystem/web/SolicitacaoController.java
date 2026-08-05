package com.helpsystem.web;

import com.helpsystem.model.Solicitacao;
import com.helpsystem.model.Usuario;
import com.helpsystem.model.enums.StatusSolicitacao;
import com.helpsystem.service.ResultadoOperacao;
import com.helpsystem.service.SolicitacaoService;
import com.helpsystem.web.dto.ApiResponse;
import com.helpsystem.web.dto.SolicitacaoRequest;
import com.helpsystem.web.dto.SolicitacaoResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SolicitacaoController {

    private final SolicitacaoService solicitacoes;

    public SolicitacaoController(SolicitacaoService solicitacoes) {
        this.solicitacoes = solicitacoes;
    }

    @GetMapping("/solicitacoes")
    public ResponseEntity<ApiResponse> listar(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "categoriaId", required = false) Integer categoriaId,
            @RequestParam(value = "autorId", required = false) Integer autorId,
            @RequestParam(value = "termo", required = false) String termo,
            @RequestParam(value = "ordenarPor", required = false, defaultValue = "data") String ordenarPor) {

        StatusSolicitacao statusFiltro = null;
        if (status != null && !status.isBlank()) {
            try {
                statusFiltro = StatusSolicitacao.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Status inválido (use ABERTA, RESPONDIDA ou RESOLVIDA).", null));
            }
        }
        if (!"data".equalsIgnoreCase(ordenarPor) && !"prioridade".equalsIgnoreCase(ordenarPor)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Ordenação inválida (use data ou prioridade).", null));
        }

        List<SolicitacaoResponse> resposta = solicitacoes.listar(statusFiltro, categoriaId, autorId, termo, ordenarPor).stream()
                .map(SolicitacaoResponse::new)
                .toList();

        return ResponseEntity.ok(new ApiResponse(true, "Solicitações listadas com sucesso.", resposta));
    }

    @GetMapping("/solicitacoes/{id}")
    public ResponseEntity<ApiResponse> buscarPorId(@PathVariable Integer id) {
        Solicitacao solicitacao = solicitacoes.buscarPorId(id).orElse(null);
        if (solicitacao == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Solicitação não encontrada.", null));
        }

        return ResponseEntity.ok(new ApiResponse(true, "Solicitação encontrada.", new SolicitacaoResponse(solicitacao)));
    }

    @PostMapping("/solicitacoes")
    public ResponseEntity<ApiResponse> criar(@RequestBody SolicitacaoRequest req, HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getAttribute(AuthInterceptor.USUARIO_LOGADO);
        ResultadoOperacao r = solicitacoes.criar(req, usuarioLogado);
        if (!r.isSucesso()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, r.getMensagem(), null));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, r.getMensagem(), new SolicitacaoResponse(r.getSolicitacao())));
    }

    @PatchMapping("/solicitacoes/{id}/resolver")
    public ResponseEntity<ApiResponse> resolver(@PathVariable Integer id, HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getAttribute(AuthInterceptor.USUARIO_LOGADO);
        ResultadoOperacao r = solicitacoes.resolver(id, usuarioLogado);
        if (!r.isSucesso()) {
            HttpStatus status = r.getMensagem().contains("não encontrada")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.FORBIDDEN;
            return ResponseEntity.status(status).body(new ApiResponse(false, r.getMensagem(), null));
        }

        return ResponseEntity.ok(new ApiResponse(true, r.getMensagem(), new SolicitacaoResponse(r.getSolicitacao())));
    }

    @DeleteMapping("/solicitacoes/{id}")
    public ResponseEntity<ApiResponse> excluir(@PathVariable Integer id, HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getAttribute(AuthInterceptor.USUARIO_LOGADO);
        ResultadoOperacao r = solicitacoes.excluir(id, usuarioLogado);
        if (!r.isSucesso()) {
            HttpStatus status = r.getMensagem().contains("não encontrada")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.FORBIDDEN;
            return ResponseEntity.status(status).body(new ApiResponse(false, r.getMensagem(), null));
        }

        return ResponseEntity.ok(new ApiResponse(true, r.getMensagem(), null));
    }
}
