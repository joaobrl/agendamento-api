package com.salaobeleza.agendamento_api.dto;

public record AuthResponse(
        String token
) {
    public AuthResponse(String token) {
        this.token = token;
    }
}
