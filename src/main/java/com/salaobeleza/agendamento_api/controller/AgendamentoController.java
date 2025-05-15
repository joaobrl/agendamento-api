package com.salaobeleza.agendamento_api.controller;

import com.salaobeleza.agendamento_api.dto.AgendamentoRequestDto;
import com.salaobeleza.agendamento_api.dto.AgendamentoResponseDto;
import com.salaobeleza.agendamento_api.service.AgendamentoService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/agendamento")
@RequiredArgsConstructor
@Slf4j
public class AgendamentoController {

    private final AgendamentoService agendamentoService;


    @PostMapping("/agendar")
    @Transactional
    @PreAuthorize("hasAnyRole('CLIENTE', 'FUNCIONARIO', 'RECEPCIONISTA', 'ADMINISTRADOR')")
    public ResponseEntity<AgendamentoResponseDto> agendar(@RequestBody @Valid AgendamentoRequestDto dto, UriComponentsBuilder uriBuilder) {
        log.info("Recebendo agendamento: {}", dto);
        var response = agendamentoService.agendar(dto);
        log.info("Agendamento realizado com sucesso: {}", response);
        var uri = uriBuilder.path("/agendamento/{id}").buildAndExpand(response.getId()).toUri();
        return ResponseEntity.created(uri).body(new AgendamentoResponseDto(response));
    }

    @PatchMapping("/{agendamentoId}/atualizar")
    @Transactional
    @PreAuthorize("hasAnyRole('FUNCIONARIO', 'RECEPCIONISTA', 'ADMINISTRADOR')")
    public ResponseEntity atualizar(@PathVariable Long agendamentoId) {
        log.info("Atualizando agendamento ID: {}", agendamentoId);
        var response = agendamentoService.atualizar(agendamentoId);
        log.info("Agendamento concluído com sucesso: {}", response);
        return ResponseEntity.ok(new AgendamentoResponseDto(response));
    }

    @DeleteMapping("/{agendamentoId}/cancelar")
    @Transactional
    @PreAuthorize(
            "hasAnyRole('RECEPCIONISTA', 'ADMINISTRADOR') or " +
                    "(hasRole('CLIENTE') and @agendamentoService.isAgendamentoDoCliente(#agendamentoId)) or " +
                    "(hasRole('FUNCIONARIO') and @agendamentoService.isAgendamentoDoFuncionario(#agendamentoId))"
    )
    public ResponseEntity cancelar(@PathVariable Long agendamentoId) {
        var cancelamento = agendamentoService.cancelar(agendamentoId);
        return ResponseEntity.ok().body(new AgendamentoResponseDto(cancelamento));
    }

}
