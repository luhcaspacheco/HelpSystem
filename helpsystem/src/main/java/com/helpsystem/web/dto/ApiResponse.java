package com.helpsystem.web.dto;

/**
 * Envelope padrão das respostas da API.
 * Ex.: { "sucesso": true, "mensagem": "...", "dado": { ... } }
 */
public class ApiResponse {
    public boolean sucesso;
    public String mensagem;
    public Object dado;

    public ApiResponse(boolean sucesso, String mensagem, Object dado) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.dado = dado;
    }
}
