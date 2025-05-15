package com.salaobeleza.agendamento_api.repository;

import com.salaobeleza.agendamento_api.model.Usuario;
import com.salaobeleza.agendamento_api.model.enums.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    List<Usuario> findByPerfilAndDisabledAtIsNull(Perfil perfil);

    List<Usuario> findByDisabledAtIsNull();
}
