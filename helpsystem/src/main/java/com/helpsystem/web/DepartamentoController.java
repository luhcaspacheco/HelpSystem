package com.helpsystem.web;

import com.helpsystem.model.Usuario;
import com.helpsystem.service.ResultadoOperacao;
import com.helpsystem.service.DepartamentoService;
import com.helpsystem.web.dto.ApiResponse;
import com.helpsystem.web.dto.DepartamentoRequest;
import com.helpsystem.web.dto.DepartamentoResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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
public class DepartamentoController {

    private final DepartamentoService departamentos;

    public DepartamentoController(DepartamentoService departamentos) {
        this.departamentos = departamentos;
    }

    @GetMapping("/departamentos")
    public ResponseEntity<ApiResponse> listar() {
        List<DepartamentoResponse> resposta = departamentos.listar().stream()
                .map(DepartamentoResponse::new)
                .toList();

        return ResponseEntity.ok(new ApiResponse(true, "Departamentos listados com sucesso.", resposta));
    }

    @PostMapping("/departamentos")
    public ResponseEntity<ApiResponse> criar(@RequestBody DepartamentoRequest req, HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getAttribute(AuthInterceptor.USUARIO_LOGADO);
        if (usuarioLogado == null || !usuarioLogado.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Apenas administradores podem criar departamentos.", null));
        }

        ResultadoOperacao r = departamentos.criar(req != null ? req.nome : null);
        if (!r.isSucesso()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, r.getMensagem(), null));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, r.getMensagem(), new DepartamentoResponse(r.getDepartamento())));
    }

    @DeleteMapping("/departamentos/{id}")
    public ResponseEntity<ApiResponse> excluir(@PathVariable Integer id, HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getAttribute(AuthInterceptor.USUARIO_LOGADO);
        if (usuarioLogado == null || !usuarioLogado.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Apenas administradores podem excluir departamentos.", null));
        }

        ResultadoOperacao r = departamentos.excluir(id);
        if (!r.isSucesso()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, r.getMensagem(), null));
        }

        return ResponseEntity.ok(new ApiResponse(true, r.getMensagem(), null));
    }
}
