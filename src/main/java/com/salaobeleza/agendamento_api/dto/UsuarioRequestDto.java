package com.salaobeleza.agendamento_api.dto;

import jakarta.validation.constraints.NotNull;

public record UsuarioRequestDto(
        @NotNull
        String nome,
        @NotNull
        String cpf,
        String email,
        @NotNull
        String telefone,
        @NotNull
        String username,
        @NotNull
        String senha
) {
}
