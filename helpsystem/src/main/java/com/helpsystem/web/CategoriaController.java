package com.helpsystem.web;

import com.helpsystem.model.Usuario;
import com.helpsystem.service.CategoriaService;
import com.helpsystem.service.ResultadoOperacao;
import com.helpsystem.web.dto.ApiResponse;
import com.helpsystem.web.dto.CategoriaRequest;
import com.helpsystem.web.dto.CategoriaResponse;

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
public class CategoriaController {

    private final CategoriaService categorias;

    public CategoriaController(CategoriaService categorias) {
        this.categorias = categorias;
    }

    @GetMapping("/categorias")
    public ResponseEntity<ApiResponse> listar() {
        List<CategoriaResponse> resposta = categorias.listar().stream()
                .map(CategoriaResponse::new)
                .toList();
        return ResponseEntity.ok(new ApiResponse(true, "Categorias listadas com sucesso.", resposta));
    }

    @PostMapping("/categorias")
    public ResponseEntity<ApiResponse> criar(@RequestBody CategoriaRequest req, HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getAttribute(AuthInterceptor.USUARIO_LOGADO);
        if (usuarioLogado == null || !usuarioLogado.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Apenas administradores podem criar categorias.", null));
        }

        ResultadoOperacao r = categorias.criar(req != null ? req.nome : null);
        if (!r.isSucesso()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, r.getMensagem(), null));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, r.getMensagem(), new CategoriaResponse(r.getCategoria())));
    }

    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<ApiResponse> excluir(@PathVariable Integer id, HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getAttribute(AuthInterceptor.USUARIO_LOGADO);
        if (usuarioLogado == null || !usuarioLogado.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Apenas administradores podem excluir categorias.", null));
        }

        ResultadoOperacao r = categorias.excluir(id);
        if (!r.isSucesso()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, r.getMensagem(), null));
        }

        return ResponseEntity.ok(new ApiResponse(true, r.getMensagem(), null));
    }
}
