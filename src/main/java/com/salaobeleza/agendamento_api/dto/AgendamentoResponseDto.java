package com.salaobeleza.agendamento_api.dto;

import com.salaobeleza.agendamento_api.model.Agendamento;
import com.salaobeleza.agendamento_api.model.enums.Servico;
import com.salaobeleza.agendamento_api.model.enums.Status;

import java.time.LocalDate;
import java.time.LocalTime;

public record AgendamentoResponseDto(
        Long id,
        String clienteNome,
        String clienteTelefone,
        LocalDate data,
        LocalTime hora,
        Servico servico,
        String funcionarioNome,
        Status status
) {

    public AgendamentoResponseDto(Agendamento agendamento) {
        this(
                agendamento.getId(),
                agendamento.getCliente().getNome(),
                agendamento.getCliente().getTelefone(),
                agendamento.getData(),
                agendamento.getHora(),
                agendamento.getServico(),
                agendamento.getFuncionario().getNome(),
                agendamento.getStatus()
        );
    }
}
