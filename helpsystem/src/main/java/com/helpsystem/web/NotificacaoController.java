package com.helpsystem.web;

import com.helpsystem.model.Usuario;
import com.helpsystem.service.NotificacaoService;
import com.helpsystem.service.ResultadoOperacao;
import com.helpsystem.web.dto.ApiResponse;
import com.helpsystem.web.dto.NotificacaoResponse;
import com.helpsystem.web.dto.TotalNaoLidasResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class NotificacaoController {

    private final NotificacaoService notificacoes;

    public NotificacaoController(NotificacaoService notificacoes) {
        this.notificacoes = notificacoes;
    }

    @GetMapping("/notificacoes")
    public ResponseEntity<ApiResponse> listar(@RequestParam(value = "lida", required = false) Boolean lida,
                                              HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getAttribute(AuthInterceptor.USUARIO_LOGADO);

        List<NotificacaoResponse> resposta = notificacoes.listar(usuarioLogado, lida).stream()
                .map(NotificacaoResponse::new)
                .toList();

        return ResponseEntity.ok(new ApiResponse(true, "Notificações listadas com sucesso.", resposta));
    }

    @GetMapping("/notificacoes/nao-lidas/total")
    public ResponseEntity<ApiResponse> contarNaoLidas(HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getAttribute(AuthInterceptor.USUARIO_LOGADO);
        long total = notificacoes.contarNaoLidas(usuarioLogado);

        return ResponseEntity.ok(new ApiResponse(true, "Total de notificações não lidas.", new TotalNaoLidasResponse(total)));
    }

    @PatchMapping("/notificacoes/{id}/lida")
    public ResponseEntity<ApiResponse> marcarComoLida(@PathVariable Integer id, HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getAttribute(AuthInterceptor.USUARIO_LOGADO);
        ResultadoOperacao r = notificacoes.marcarComoLida(id, usuarioLogado);
        if (!r.isSucesso()) {
            HttpStatus status = r.getMensagem().contains("não encontrada")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.FORBIDDEN;
            return ResponseEntity.status(status).body(new ApiResponse(false, r.getMensagem(), null));
        }

        return ResponseEntity.ok(new ApiResponse(true, r.getMensagem(), new NotificacaoResponse(r.getNotificacao())));
    }
}
