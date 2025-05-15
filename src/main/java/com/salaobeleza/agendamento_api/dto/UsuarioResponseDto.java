package com.salaobeleza.agendamento_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.salaobeleza.agendamento_api.model.Usuario;
import com.salaobeleza.agendamento_api.model.enums.Perfil;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UsuarioResponseDto(
        Long id,
        String nome,
        String cpf,
        String email,
        String telefone,
        String username,
        Perfil perfil,
        LocalDateTime disabledAt
) {
    public UsuarioResponseDto(Usuario usuario) {
        this(usuario.getId(),
                usuario.getNome(),
                usuario.getCpf(),
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getUsername(),
                usuario.getPerfil(),
                usuario.getDisabledAt()
        );
    }

}
