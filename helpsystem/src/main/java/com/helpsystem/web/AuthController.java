package com.helpsystem.web;

import com.helpsystem.service.AutenticacaoService;
import com.helpsystem.service.ResultadoOperacao;
import com.helpsystem.service.SessaoService;
import com.helpsystem.web.dto.ApiResponse;
import com.helpsystem.web.dto.LoginRequest;
import com.helpsystem.web.dto.LoginResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AutenticacaoService auth;
    private final SessaoService sessoes;

    public AuthController(AutenticacaoService auth, SessaoService sessoes) {
        this.auth = auth;
        this.sessoes = sessoes;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest req) {
        if (req == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Informe e-mail e senha.", null));
        }

        ResultadoOperacao r = auth.login(req.email, req.senha);
        if (!r.isSucesso()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, r.getMensagem(), null));
        }

        String token = sessoes.criarToken(r.getUsuario());
        return ResponseEntity.ok(new ApiResponse(true, r.getMensagem(), new LoginResponse(token, r.getUsuario())));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = extrairToken(authorization);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Token não informado.", null));
        }

        sessoes.invalidar(token);
        return ResponseEntity.ok(new ApiResponse(true, "Logout efetuado com sucesso.", null));
    }

    private String extrairToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7).trim();
    }
}
