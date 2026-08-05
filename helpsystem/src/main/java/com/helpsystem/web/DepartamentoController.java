package com.helpsystem.web;

import com.helpsystem.service.DepartamentoService;
import com.helpsystem.web.dto.ApiResponse;
import com.helpsystem.web.dto.DepartamentoResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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
}
