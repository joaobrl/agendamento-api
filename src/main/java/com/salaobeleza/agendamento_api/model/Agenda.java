package com.salaobeleza.agendamento_api.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Embeddable
public class Agenda {

    @OneToMany(mappedBy = "funcionario")
    private List<Agendamento> agendamentos = new ArrayList<>();

    private Trancamento trancamento;
}
