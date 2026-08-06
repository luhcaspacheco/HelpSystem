package com.helpsystem.web;

import com.helpsystem.model.Departamento;
import com.helpsystem.model.Usuario;
import com.helpsystem.model.enums.TipoUsuario;
import com.helpsystem.service.ResultadoOperacao;
import com.helpsystem.service.UsuarioService;
import com.helpsystem.web.dto.ApiResponse;
import com.helpsystem.web.dto.CadastroRequest;
import com.helpsystem.web.dto.UsuarioResponse;
import com.helpsystem.web.dto.UsuarioTipoRequest;

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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarios;

    public UsuarioController(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    @PostMapping("/usuarios")
    public ResponseEntity<ApiResponse> cadastrar(@RequestBody CadastroRequest req) {
        if (req == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Informe os dados do usuário.", null));
        }

        TipoUsuario tipo = TipoUsuario.COMUM;
        if (req.tipo != null && !req.tipo.isBlank()) {
            String tipoInformado = req.tipo.trim().toUpperCase();
            if (!TipoUsuario.COMUM.name().equals(tipoInformado)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "O cadastro público permite apenas usuários comuns.", null));
            }
        }

        Departamento departamento = null;
        if (req.departamentoId != null && req.departamentoId > 0) {
            departamento = new Departamento(req.departamentoId, null);
        }

        ResultadoOperacao r = usuarios.cadastrar(req.nome, req.email, req.senha, tipo, departamento);
        if (!r.isSucesso()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, r.getMensagem(), null));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, r.getMensagem(), new UsuarioResponse(r.getUsuario())));
    }

    @GetMapping("/usuarios")
    public ResponseEntity<ApiResponse> listar(HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getAttribute(AuthInterceptor.USUARIO_LOGADO);
        if (usuarioLogado == null || !usuarioLogado.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Apenas administradores podem listar usuários.", null));
        }

        List<UsuarioResponse> resposta = usuarios.listar(usuarioLogado).stream()
                .map(UsuarioResponse::new)
                .toList();
        return ResponseEntity.ok(new ApiResponse(true, "Usuários listados com sucesso.", resposta));
    }

    @PatchMapping("/usuarios/{id}/tipo")
    public ResponseEntity<ApiResponse> alterarTipo(@PathVariable Integer id,
                                                   @RequestBody UsuarioTipoRequest req,
                                                   HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getAttribute(AuthInterceptor.USUARIO_LOGADO);
        ResultadoOperacao r = usuarios.alterarTipo(id, req != null ? req.tipo : null, usuarioLogado);
        if (!r.isSucesso()) {
            HttpStatus status = statusErroUsuario(r.getMensagem());
            return ResponseEntity.status(status).body(new ApiResponse(false, r.getMensagem(), null));
        }

        return ResponseEntity.ok(new ApiResponse(true, r.getMensagem(), new UsuarioResponse(r.getUsuario())));
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<ApiResponse> excluir(@PathVariable Integer id, HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getAttribute(AuthInterceptor.USUARIO_LOGADO);
        ResultadoOperacao r = usuarios.excluir(id, usuarioLogado);
        if (!r.isSucesso()) {
            HttpStatus status = statusErroUsuario(r.getMensagem());
            return ResponseEntity.status(status).body(new ApiResponse(false, r.getMensagem(), null));
        }

        return ResponseEntity.ok(new ApiResponse(true, r.getMensagem(), new UsuarioResponse(r.getUsuario())));
    }

    @PatchMapping("/usuarios/{id}/reativar")
    public ResponseEntity<ApiResponse> reativar(@PathVariable Integer id, HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getAttribute(AuthInterceptor.USUARIO_LOGADO);
        ResultadoOperacao r = usuarios.reativar(id, usuarioLogado);
        if (!r.isSucesso()) {
            HttpStatus status = statusErroUsuario(r.getMensagem());
            return ResponseEntity.status(status).body(new ApiResponse(false, r.getMensagem(), null));
        }

        return ResponseEntity.ok(new ApiResponse(true, r.getMensagem(), new UsuarioResponse(r.getUsuario())));
    }

    private HttpStatus statusErroUsuario(String mensagem) {
        if (mensagem.contains("não encontrado")) {
            return HttpStatus.NOT_FOUND;
        }
        if (mensagem.contains("inválido") || mensagem.contains("já está")) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.FORBIDDEN;
    }
}
