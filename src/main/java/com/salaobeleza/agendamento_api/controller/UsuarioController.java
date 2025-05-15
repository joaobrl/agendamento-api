package com.salaobeleza.agendamento_api.controller;

import com.salaobeleza.agendamento_api.dto.UsuarioRequestDto;
import com.salaobeleza.agendamento_api.dto.UsuarioResponseDto;
import com.salaobeleza.agendamento_api.dto.UsuarioUpdateDto;
import com.salaobeleza.agendamento_api.service.UsuarioService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/cadastrar")
    @Transactional
    public ResponseEntity cadastrarUsuario (@RequestBody @Valid UsuarioRequestDto dto, UriComponentsBuilder uriBuilder) {
        var usuario = usuarioService.cadastrarUsuario(dto);
        var uri = uriBuilder.path("/usuarios/{id}").buildAndExpand(usuario.getId()).toUri();
        return ResponseEntity.created(uri).body(new UsuarioResponseDto(usuario));
    }

    @GetMapping("/listar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'FUNCIONARIO')")
    public ResponseEntity<List<UsuarioResponseDto>> listarUsuarios() {
        var usuarios = usuarioService.listarUsuarios()
                .stream()
                .map(UsuarioResponseDto::new)
                .toList();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'FUNCIONARIO')")
    public ResponseEntity buscarUsuario (@PathVariable Long id) {
        var usuario = usuarioService.buscarUsuario(id);
        return ResponseEntity.ok(new UsuarioResponseDto(usuario));
    }

    @PatchMapping("/{id}/atualizar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA')")
    public ResponseEntity atualizarUsuario(@PathVariable Long id, @RequestBody @Valid UsuarioUpdateDto dto) {
        var usuario = usuarioService.atualizarUsuario(id, dto);
        return ResponseEntity.ok(new UsuarioResponseDto(usuario));
    }

    @DeleteMapping("/{id}/deletar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA')")
    public ResponseEntity deletarUsuario(@PathVariable Long id) {
        var usuario = usuarioService.deletarUsuario(id);
        return ResponseEntity.ok(new UsuarioResponseDto(usuario));
    }
}
