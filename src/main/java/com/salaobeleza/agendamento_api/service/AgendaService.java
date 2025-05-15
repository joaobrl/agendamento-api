package com.salaobeleza.agendamento_api.service;

import com.salaobeleza.agendamento_api.dto.AgendaClienteDto;
import com.salaobeleza.agendamento_api.dto.AgendaFuncionarioDto;
import com.salaobeleza.agendamento_api.dto.AgendaLockRequestDto;
import com.salaobeleza.agendamento_api.dto.AgendamentoResponseDto;
import com.salaobeleza.agendamento_api.model.Trancamento;
import com.salaobeleza.agendamento_api.model.Usuario;
import com.salaobeleza.agendamento_api.model.enums.Perfil;
import com.salaobeleza.agendamento_api.model.enums.Status;
import com.salaobeleza.agendamento_api.repository.AgendamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgendaService {

    private final AgendamentoRepository agendamentoRepository;
    private final UsuarioService usuarioService;

    public List<AgendamentoResponseDto> consultarAgendaUsuarioLogado() {
        var usuarioLogado = usuarioService.getUsuarioLogado();
        var agendamentos = agendamentoRepository.findByClienteIdOrFuncionarioId(usuarioLogado.getId(), usuarioLogado.getId());

        log.info("Agenda consultada para o usuário logado com ID: {}", usuarioLogado.getId());
        return agendamentos.stream()
                .filter(agendamento -> agendamento.getStatus() == Status.PENDENTE)
                .map(AgendamentoResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<AgendaFuncionarioDto> consultarAgendaDeProfissional(Long funcionarioId) {
        var funcionario = validarProfissional(funcionarioId);
        var agendamentos = agendamentoRepository.findByFuncionarioId(funcionario.getId());

        log.info("Agenda consultada para o profissional com ID: {}", funcionarioId);
        return agendamentos.stream()
                .map(agendamento -> new AgendaFuncionarioDto(
                        agendamento.getCliente().getNome(),
                        agendamento.getCliente().getTelefone(),
                        agendamento.getData(),
                        agendamento.getHora(),
                        agendamento.getServico()))
                .collect(Collectors.toList());
    }

    public List<AgendaClienteDto> consultarAgendaDeCliente(Long clienteId) {
        var cliente = validarCliente(clienteId);
        var agendamentos = agendamentoRepository.findByClienteId(cliente.getId());

        log.info("Agenda consultada para o cliente com ID: {}", clienteId);
        return agendamentos.stream()
                .map(agendamento -> new AgendaClienteDto(
                        agendamento.getData(),
                        agendamento.getHora(),
                        agendamento.getServico(),
                        agendamento.getFuncionario().getNome(),
                        agendamento.getFuncionario().getTelefone()))
                .collect(Collectors.toList());
    }

    public List<LocalTime> consultarDisponibilidade(Long profissionalId, LocalDate data) {

        // Buscar o profissional
        var profissional = usuarioService.findById(profissionalId);
        if (!profissional.getPerfil().equals(Perfil.FUNCIONARIO)) {
            log.error("Usuário com ID: {} não é um funcionário.", profissionalId);
            throw new IllegalArgumentException("O ID informado não pertence a um funcionário.");
        }

        // Verificar se a agenda do profissional está aberta
        var agenda = profissional.getAgenda();
        if (!agenda.getTrancamento().isAberta()) {
            log.error("Agenda do profissional com ID: {} está fechada ou não há vagas disponiveis.", profissionalId);
            throw new IllegalArgumentException("A agenda do profissional está fechada ou não configurada.");
        }

        // Buscar agendamentos pendentes para a data e o profissional
        var agendamentosPendentes = agendamentoRepository.findByFuncionarioIdAndData(profissionalId, data).stream()
                .filter(agendamento -> agendamento.getStatus().equals(Status.PENDENTE))
                .toList();

        // Definir o horário de expediente
        LocalTime inicioExpediente = LocalTime.of(8, 0);
        LocalTime fimExpediente = LocalTime.of(18, 0);
        int duracaoSlotMinutos = 30;

        // Gerar lista de horários disponíveis
        List<LocalTime> horariosDisponiveis = new ArrayList<>();
        for (LocalTime hora = inicioExpediente; hora.isBefore(fimExpediente); hora = hora.plusMinutes(duracaoSlotMinutos)) {
            final LocalTime horaAtual = hora;

            // Verificar se o horário está ocupado por um agendamento pendente
            boolean ocupado = agendamentosPendentes.stream()
                    .anyMatch(agendamento -> agendamento.getHora().equals(horaAtual));

            if (!ocupado) {
                horariosDisponiveis.add(horaAtual);
            }
        }

        log.info("Disponibilidade consultada para profissional com ID: {} na data: {}", profissionalId, data);
        return horariosDisponiveis;
    }

    public Trancamento trancarAgenda(AgendaLockRequestDto dto) {
        var profissional = validarProfissional(dto.profissionalId());

        if (profissional.getAgenda() == null) {
            log.error("Tentativa de trancar agenda para profissional sem agenda configurada. ID: {}", dto.profissionalId());
            throw new IllegalArgumentException("O profissional não possui agenda configurada.");
        }

        var trancamento = new Trancamento(false, dto.data()); // agora só com a data

        profissional.getAgenda().setTrancamento(trancamento);
        usuarioService.save(profissional);
        log.info("Agenda do profissional com ID: {} trancada para a data: {}", dto.profissionalId(), dto.data());

        return trancamento;
    }

    public Trancamento abrirAgenda(AgendaLockRequestDto dto) {
        var profissional = validarProfissional(dto.profissionalId());

        var agenda = profissional.getAgenda();
        if (agenda == null) {
            log.error("Tentativa de abrir agenda para profissional sem agenda configurada. ID: {}", dto.profissionalId());
            throw new IllegalArgumentException("O profissional não possui agenda configurada.");
        }

        var trancamento = agenda.getTrancamento();
        if (trancamento == null || !trancamento.getData().equals(dto.data())) {
            log.error("Tentativa de abrir agenda sem trancamento correspondente. ID: {}", dto.profissionalId());
            throw new IllegalArgumentException("Não há trancamento correspondente para esta data.");
        }

        trancamento.setAberta(true);
        usuarioService.save(profissional);

        log.info("Agenda do profissional com ID: {} aberta para a data: {}", dto.profissionalId(), dto.data());

        return trancamento;
    }

    private Usuario validarProfissional(Long profissionalId) {
        return usuarioService.findById(profissionalId);
    }

    private Usuario validarCliente(Long clienteId) {
        return usuarioService.findById(clienteId);
    }

}