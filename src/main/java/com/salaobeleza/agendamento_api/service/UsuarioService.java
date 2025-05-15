package com.salaobeleza.agendamento_api.service;

import com.salaobeleza.agendamento_api.dto.UsuarioRequestDto;
import com.salaobeleza.agendamento_api.dto.UsuarioUpdateDto;
import com.salaobeleza.agendamento_api.model.Usuario;
import com.salaobeleza.agendamento_api.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Usuario cadastrarUsuario(UsuarioRequestDto dto) {
        var usuario = new Usuario(dto);
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        var usuarioSalvo = usuarioRepository.save(usuario);
        log.info("Usuário criado com ID: {}", usuarioSalvo.getId());
        return usuarioSalvo;
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findByDisabledAtIsNull();
    }

    public Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o id: " + id));
    }

    public Usuario atualizarUsuario(Long id, UsuarioUpdateDto dto) {
        if (dto == null) {
            log.error("Tentativa de atualizar usuário com dados nulos.");
            throw new IllegalArgumentException("Dados de atualização não podem ser nulos.");
        }

        var usuario = buscarUsuario(id);
        String senhaCriptografada = dto.senha() != null ? passwordEncoder.encode(dto.senha()) : null;

        UsuarioUpdateDto dtoAtualizado = new UsuarioUpdateDto(
                dto.email(),
                dto.telefone(),
                senhaCriptografada,
                dto.perfil()
        );
        usuario.atualizar(dtoAtualizado);
        log.info("Usuário com ID: {} atualizado.", id);
        return usuarioRepository.save(usuario);
    }

    public Usuario deletarUsuario(Long id) {
        var usuario = buscarUsuario(id);
        if (usuario.getDisabledAt() != null) {
            log.warn("Tentativa de desativar usuário já desativado com ID: {}", id);
            throw new IllegalArgumentException("O usuário já está desativado.");
        }
        usuario.setDisabledAt(LocalDateTime.now());
        log.info("Usuário com ID: {} desativado.", id);
        return usuarioRepository.save(usuario);
    }

    public Usuario getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("Tentativa de obter o usuário logado sem autenticação.");
            throw new IllegalArgumentException("Nenhum usuário autenticado encontrado.");
        }
        String username = authentication.getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuário autenticado não encontrado."));
    }

    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o id: " + id));
    }

    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}