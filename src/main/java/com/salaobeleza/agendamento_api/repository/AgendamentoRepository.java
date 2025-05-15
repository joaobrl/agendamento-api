package com.salaobeleza.agendamento_api.repository;

import com.salaobeleza.agendamento_api.model.Agendamento;
import com.salaobeleza.agendamento_api.model.Usuario;
import com.salaobeleza.agendamento_api.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    List<Agendamento> findByClienteId(Long clienteId);

    List<Agendamento> findByFuncionarioId(Long funcionarioId);

    List<Agendamento> findByClienteIdOrFuncionarioId(Long clienteId, Long funcionarioId);

    List<Agendamento> findByFuncionarioIdAndData(Long funcionarioId, LocalDate data);

    boolean existsByClienteAndDataAndHoraAndStatus(Usuario cliente, LocalDate data, LocalTime hora, Status status);

    boolean existsByFuncionarioAndDataAndHoraAndStatus(Usuario funcionario, LocalDate data, LocalTime hora, Status status);

    List<Agendamento> findByClienteIdAndData(Long clienteId, LocalDate data);

}
