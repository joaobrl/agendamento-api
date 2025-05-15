package com.salaobeleza.agendamento_api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AgendaLockRequestDto(
        @NotNull
        Long profissionalId,
        @NotNull
        LocalDate data
) {
}
