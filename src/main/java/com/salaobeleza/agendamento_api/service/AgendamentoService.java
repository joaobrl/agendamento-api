package com.salaobeleza.agendamento_api.service;

import com.salaobeleza.agendamento_api.dto.AgendamentoRequestDto;
import com.salaobeleza.agendamento_api.model.Agendamento;
import com.salaobeleza.agendamento_api.model.Usuario;
import com.salaobeleza.agendamento_api.model.enums.Perfil;
import com.salaobeleza.agendamento_api.model.enums.Status;
import com.salaobeleza.agendamento_api.repository.AgendamentoRepository;
import com.salaobeleza.agendamento_api.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final AgendaService agendaService;

    public Agendamento agendar(AgendamentoRequestDto dto) {
        var usuarioLogado = usuarioService.getUsuarioLogado();
        var cliente = validarClienteParaAgendamento(dto, usuarioLogado);

        var data = dto.data();
        var hora = dto.hora();
        validarHorario(hora);

        var funcionario = Optional.ofNullable(dto.funcionarioId())
                .flatMap(usuarioRepository::findById)
                .orElseGet(() -> buscarFuncionarioDisponivel(data, hora));

        if (funcionario == null) {
            throw new IllegalArgumentException("Nenhum funcionário disponível para o horário informado.");
        }

        if (isHorarioConflitante(cliente, funcionario, data, hora)) {
            throw new IllegalArgumentException("Já existe um agendamento para este horário.");
        }

        if (data.isEqual(LocalDate.now()) && hora.isBefore(LocalTime.now()) || data.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A data do agendamento deve ser futura.");
        }

        var agendamento = new Agendamento(dto, cliente, funcionario);

        log.info("Agendamento criado para cliente ID: {}, data: {}, hora: {}", cliente.getId(), data, hora);
        return agendamentoRepository.save(agendamento);
    }

    private void validarHorario(LocalTime hora) {
        if (hora.getMinute() % 30 != 0) {
            throw new IllegalArgumentException("O horário deve ser múltiplo de 30 minutos, como 10:00, 10:30, etc.");
        }

        if (!((hora.isAfter(LocalTime.of(7, 59)) && hora.isBefore(LocalTime.of(11, 31))) ||
                (hora.isAfter(LocalTime.of(12, 59)) && hora.isBefore(LocalTime.of(17, 31))))) {
            throw new IllegalArgumentException("Apenas horários entre 08:00 e 11:30 ou 13:00 e 17:30 são permitidos.");
        }
    }

    private Usuario validarClienteParaAgendamento(AgendamentoRequestDto dto, Usuario usuarioLogado) {
        if (usuarioLogado.getPerfil().equals(Perfil.CLIENTE)) {
            if (dto.clienteId() != null && !dto.clienteId().equals(usuarioLogado.getId())) {
                throw new IllegalArgumentException("Clientes só podem criar agendamentos para si mesmos.");
            }
            var agendamentos = agendamentoRepository.findByClienteIdAndData(usuarioLogado.getId(), dto.data());

            if (agendamentos.size() >= 3) {
                throw new IllegalArgumentException("Limite de 3 agendamentos por dia atingido.");
            }
            return usuarioLogado;
        }

        return usuarioRepository.findById(dto.clienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com o ID: " + dto.clienteId()));
    }

    private Usuario buscarFuncionarioDisponivel(LocalDate data, LocalTime hora) {
        var funcionarios = usuarioRepository.findByPerfilAndDisabledAtIsNull(Perfil.FUNCIONARIO);

        return funcionarios.stream()
                .filter(funcionario -> !isHorarioConflitante(null, funcionario, data, hora))
                .findFirst()
                .orElse(null);
    }

    private boolean isHorarioConflitante(Usuario cliente, Usuario funcionario, LocalDate data, LocalTime hora) {
        return agendamentoRepository.existsByClienteAndDataAndHoraAndStatus(cliente, data, hora, Status.PENDENTE) ||
                agendamentoRepository.existsByFuncionarioAndDataAndHoraAndStatus(funcionario, data, hora, Status.PENDENTE);
    }

    public Agendamento cancelar(Long agendamentoId) {
        var agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado com o ID: " + agendamentoId));

        if (agendamento.getStatus().equals(Status.CANCELADO)) {
            throw new IllegalArgumentException("Agendamento já cancelado.");
        }

        if (LocalDate.now().isAfter(agendamento.getData())){
            throw new IllegalArgumentException("Agendamentos só podem ser cancelados até 24 horas antes do horário agendado.");
        }

        var usuarioAutenticado = usuarioService.getUsuarioLogado();

        boolean isCliente = agendamento.getCliente().getId().equals(usuarioAutenticado.getId());
        boolean isFuncionario = agendamento.getFuncionario() != null &&
                agendamento.getFuncionario().getId().equals(usuarioAutenticado.getId());
        boolean isAdminOuRecepcionista = usuarioAutenticado.getPerfil() == Perfil.ADMINISTRADOR ||
                usuarioAutenticado.getPerfil() == Perfil.RECEPCIONISTA;

        if (!(isCliente || isFuncionario || isAdminOuRecepcionista)) {
            throw new SecurityException("Você não tem permissão para cancelar este agendamento.");
        }

        agendamento.cancelar();
        log.info("Agendamento com ID: {} foi cancelado pelo usuário ID: {}", agendamentoId, usuarioAutenticado.getId());

        return agendamentoRepository.save(agendamento);
    }

    public boolean isAgendamentoDoCliente(Long agendamentoId) {
        var agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado"));
        var usuario = usuarioService.getUsuarioLogado();
        return agendamento.getCliente().getId().equals(usuario.getId());
    }

    public boolean isAgendamentoDoFuncionario(Long agendamentoId) {
        var agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado"));
        var usuario = usuarioService.getUsuarioLogado();
        return agendamento.getFuncionario() != null &&
                agendamento.getFuncionario().getId().equals(usuario.getId());
    }

    public Agendamento atualizar(Long agendamentoId) {
        var agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado com o ID: " + agendamentoId));

        var agendamentoConcluido = LocalDateTime.of(agendamento.getData(), agendamento.getHora()).plusMinutes(30);
        if (LocalDateTime.now().isBefore(agendamentoConcluido)) {
            throw new IllegalArgumentException("Agendamentos só podem ser concluídos 30 minutos após o horário agendado.");
        }

        agendamento.atualizar();
        log.info("Agendamento com ID: {} foi concluído", agendamentoId);
        return agendamentoRepository.save(agendamento);
    }
}