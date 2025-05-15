package com.salaobeleza.agendamento_api.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Embeddable
public class Agenda {

    @ElementCollection
    private List<Agendamento> agendamentos = new ArrayList<>();

    private Trancamento trancamento;

}
