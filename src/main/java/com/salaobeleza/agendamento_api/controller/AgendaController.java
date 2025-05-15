package com.salaobeleza.agendamento_api.controller;

import com.salaobeleza.agendamento_api.dto.AgendaClienteDto;
import com.salaobeleza.agendamento_api.dto.AgendaFuncionarioDto;
import com.salaobeleza.agendamento_api.dto.AgendaLockRequestDto;
import com.salaobeleza.agendamento_api.dto.AgendamentoResponseDto;
import com.salaobeleza.agendamento_api.model.Trancamento;
import com.salaobeleza.agendamento_api.service.AgendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/agenda")
@RequiredArgsConstructor
public class AgendaController {

    private final AgendaService agendaService;


    @GetMapping("/minha-agenda")
    public ResponseEntity<List<AgendamentoResponseDto>> consultarMinhaAgenda() {
        var agendamentos = agendaService.consultarAgendaUsuarioLogado();
        return ResponseEntity.ok(agendamentos);
    }

    @GetMapping("/profissional/{funcionarioId}")
    @PreAuthorize("hasRole('RECEPCIONISTA', 'ADMINISTRADOR')")
    public ResponseEntity<List<AgendaFuncionarioDto>> consultarAgendaProfissional(@PathVariable Long funcionarioId) {
        var agendamentos = agendaService.consultarAgendaDeProfissional(funcionarioId);
        return ResponseEntity.ok(agendamentos);
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('RECEPCIONISTA', 'FUNCIONARIO', 'ADMINISTRADOR')")
    public ResponseEntity<List<AgendaClienteDto>> consultarAgendaCliente(@PathVariable Long clienteId) {
        var agendamentos = agendaService.consultarAgendaDeCliente(clienteId);
        return ResponseEntity.ok(agendamentos);
    }

    @GetMapping("/profissional/{profissionalId}/disponibilidade")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'FUNCIONARIO', 'CLIENTE')")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidade(@PathVariable Long profissionalId, @RequestParam LocalDate data) {
        var horariosDisponiveis = agendaService.consultarDisponibilidade(profissionalId, data);
        return ResponseEntity.ok(horariosDisponiveis);
    }

    @PostMapping("/abrir")
    @PreAuthorize("hasAnyRole('RECEPCIONISTA', 'FUNCIONARIO', 'ADMINISTRADOR')")
    public ResponseEntity<Trancamento> abrirAgenda(@RequestBody @Valid AgendaLockRequestDto dto) {
        Trancamento trancamento = agendaService.abrirAgenda(dto);
        return ResponseEntity.ok(trancamento);
    }

    @PostMapping("/trancar")
    @PreAuthorize("hasAnyRole('RECEPCIONISTA', 'FUNCIONARIO', 'ADMINISTRADOR')")
    public ResponseEntity<Trancamento> trancarAgenda(@RequestBody @Valid AgendaLockRequestDto dto) {
        Trancamento trancamento = agendaService.trancarAgenda(dto);
        return ResponseEntity.ok(trancamento);
    }
}