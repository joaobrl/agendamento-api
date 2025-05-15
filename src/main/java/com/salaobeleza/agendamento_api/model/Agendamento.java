package com.salaobeleza.agendamento_api.model;


import com.salaobeleza.agendamento_api.dto.AgendamentoRequestDto;
import com.salaobeleza.agendamento_api.model.enums.Servico;
import com.salaobeleza.agendamento_api.model.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "agendamentos")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne
    @JoinColumn(name = "funcionario_id")
    private Usuario funcionario;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private LocalTime hora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Servico servico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public Agendamento(AgendamentoRequestDto dto, Usuario cliente, Usuario funcionario) {
        this.cliente = cliente;
        this.funcionario = funcionario;
        this.data = dto.data();
        this.hora = dto.hora();
        this.servico = dto.servico();
        this.status = Status.PENDENTE;
    }

    public void cancelar() {
        this.status = Status.CANCELADO;
    }

    public void atualizar() {
        this.status = Status.CONCLUIDO;
    }
}

