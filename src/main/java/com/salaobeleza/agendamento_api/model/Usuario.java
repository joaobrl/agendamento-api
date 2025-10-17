package com.salaobeleza.agendamento_api.model;

import com.salaobeleza.agendamento_api.dto.UsuarioRequestDto;
import com.salaobeleza.agendamento_api.dto.UsuarioUpdateDto;
import com.salaobeleza.agendamento_api.model.enums.Perfil;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "O CPF é obrigatório")
    @Column(nullable = false, unique = true)
    @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 dígitos numéricos")
    private String cpf;

    @Email(message = "O e-mail deve ser válido")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "O nome de usuário é obrigatório")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "A senha é obrigatória")
    @Column(nullable = false)
    private String senha;

    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Perfil perfil;

    @Embedded
    private Agenda agenda;

    private LocalDateTime disabledAt;

    public Usuario(UsuarioRequestDto dto) {
        this.nome = dto.nome();
        this.cpf = dto.cpf();
        this.email = dto.email();
        this.telefone = dto.telefone();
        this.username = dto.username();
        this.senha = dto.senha();
        this.perfil = Perfil.CLIENTE;
        this.agenda = new Agenda();
        this.agenda.setTrancamento(new Trancamento(true, null));
    }

    public void atualizar(UsuarioUpdateDto dto) {
        if (dto.email() != null) {
            this.email = dto.email();
        }
        if (dto.telefone() != null) {
            this.telefone = dto.telefone();
        }
        if (dto.perfil() != null) {
            this.perfil = dto.perfil();
        }
        if (dto.senha() != null) {
            this.senha = dto.senha();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.perfil.name()));
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.disabledAt == null;
    }
}