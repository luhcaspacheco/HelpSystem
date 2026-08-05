package com.helpsystem.web;

import com.helpsystem.model.Usuario;
import com.helpsystem.service.RespostaService;
import com.helpsystem.service.ResultadoOperacao;
import com.helpsystem.service.SolicitacaoService;
import com.helpsystem.web.dto.ApiResponse;
import com.helpsystem.web.dto.RespostaRequest;
import com.helpsystem.web.dto.RespostaResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RespostaController {

    private final RespostaService respostas;
    private final SolicitacaoService solicitacoes;

    public RespostaController(RespostaService respostas, SolicitacaoService solicitacoes) {
        this.respostas = respostas;
        this.solicitacoes = solicitacoes;
    }

    @GetMapping("/solicitacoes/{solicitacaoId}/respostas")
    public ResponseEntity<ApiResponse> listar(@PathVariable Integer solicitacaoId) {
        if (solicitacoes.buscarPorId(solicitacaoId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Solicitacao nao encontrada.", null));
        }

        List<RespostaResponse> resposta = respostas.listarPorSolicitacao(solicitacaoId).stream()
                .map(RespostaResponse::new)
                .toList();

        return ResponseEntity.ok(new ApiResponse(true, "Respostas listadas com sucesso.", resposta));
    }

    @PostMapping("/solicitacoes/{solicitacaoId}/respostas")
    public ResponseEntity<ApiResponse> responder(@PathVariable Integer solicitacaoId,
                                                 @RequestBody RespostaRequest req,
                                                 HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getAttribute(AuthInterceptor.USUARIO_LOGADO);
        ResultadoOperacao r = respostas.responder(solicitacaoId, req, usuarioLogado);
        if (!r.isSucesso()) {
            HttpStatus status = r.getMensagem().contains("nao encontrada")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(new ApiResponse(false, r.getMensagem(), null));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, r.getMensagem(), new RespostaResponse(r.getResposta())));
    }
}
