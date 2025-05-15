package com.salaobeleza.agendamento_api.dto;

import com.salaobeleza.agendamento_api.model.Agendamento;
import com.salaobeleza.agendamento_api.model.enums.Servico;

import java.time.LocalDate;
import java.time.LocalTime;

public record AgendaClienteDto(

        LocalDate data,
        LocalTime hora,
        Servico servico,
        String funcionarioNome,
        String funcionarioTelefone
) {

}
