package com.helpsystem.web;

import com.helpsystem.model.Departamento;
import com.helpsystem.model.enums.TipoUsuario;
import com.helpsystem.service.ResultadoOperacao;
import com.helpsystem.service.UsuarioService;
import com.helpsystem.web.dto.ApiResponse;
import com.helpsystem.web.dto.CadastroRequest;
import com.helpsystem.web.dto.UsuarioResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                    .body(new ApiResponse(false, "Informe os dados do usuario.", null));
        }

        TipoUsuario tipo = TipoUsuario.COMUM;
        if (req.tipo != null && !req.tipo.isBlank()) {
            String tipoInformado = req.tipo.trim().toUpperCase();
            if (!TipoUsuario.COMUM.name().equals(tipoInformado)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "Cadastro publico permite apenas usuario comum.", null));
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
}
