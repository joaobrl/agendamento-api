package com.salaobeleza.agendamento_api.dto;

import com.salaobeleza.agendamento_api.model.enums.Servico;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record AgendamentoRequestDto(
        Long clienteId,
        @NotNull
        String telefoneCliente,
        @NotNull
        LocalDate data,
        @NotNull
        LocalTime hora,
        @NotNull
        Servico servico,
        Long funcionarioId
) {
}
