package com.helpsystem.web;

import com.helpsystem.web.dto.ApiResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> jsonInvalido() {
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "JSON invalido ou corpo da requisicao ausente.", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> erroInesperado(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Erro interno da API.", null));
    }
}
