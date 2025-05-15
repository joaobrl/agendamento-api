package com.salaobeleza.agendamento_api.dto;

import com.salaobeleza.agendamento_api.model.enums.Perfil;
import jakarta.validation.constraints.NotNull;

public record UsuarioUpdateDto(
        String email,
        String telefone,
        String senha,
        Perfil perfil
) {
}
