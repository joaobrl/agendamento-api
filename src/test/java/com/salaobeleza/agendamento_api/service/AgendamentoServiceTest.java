package com.salaobeleza.agendamento_api.service;

import com.salaobeleza.agendamento_api.dto.AgendamentoRequestDto;
import com.salaobeleza.agendamento_api.model.Agendamento;
import com.salaobeleza.agendamento_api.model.Agenda;
import com.salaobeleza.agendamento_api.model.Trancamento;
import com.salaobeleza.agendamento_api.model.Usuario;
import com.salaobeleza.agendamento_api.model.enums.Perfil;
import com.salaobeleza.agendamento_api.model.enums.Servico;
import com.salaobeleza.agendamento_api.model.enums.Status;
import com.salaobeleza.agendamento_api.repository.AgendamentoRepository;
import com.salaobeleza.agendamento_api.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private AgendaService agendaService;

    @InjectMocks
    private AgendamentoService agendamentoService;

    private Usuario cliente;
    private Usuario funcionario;

    @BeforeEach
    void setUp() {
        cliente = criarUsuario(1L, Perfil.CLIENTE);
        funcionario = criarUsuario(2L, Perfil.FUNCIONARIO);
    }

    @Test
    void deveAgendarQuandoClienteValidoEFuncionarioDisponivel() {
        LocalDate data = LocalDate.now().plusDays(1);
        LocalTime hora = LocalTime.of(10, 0);
        AgendamentoRequestDto dto = new AgendamentoRequestDto(cliente.getId(), "11999999999", data, hora, Servico.CABELEREIRO, null);

        when(usuarioService.getUsuarioLogado()).thenReturn(cliente);
        when(agendamentoRepository.findByClienteIdAndData(cliente.getId(), data)).thenReturn(List.of());
        when(usuarioRepository.findByPerfilAndDisabledAtIsNull(Perfil.FUNCIONARIO)).thenReturn(List.of(funcionario));
        when(agendamentoRepository.existsByClienteAndDataAndHoraAndStatus(any(), eq(data), eq(hora), eq(Status.PENDENTE))).thenReturn(false);
        when(agendamentoRepository.existsByFuncionarioAndDataAndHoraAndStatus(funcionario, data, hora, Status.PENDENTE)).thenReturn(false);
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Agendamento resultado = agendamentoService.agendar(dto);

        assertNotNull(resultado);
        assertEquals(cliente, resultado.getCliente());
        assertEquals(funcionario, resultado.getFuncionario());
        assertEquals(data, resultado.getData());
        assertEquals(hora, resultado.getHora());
        assertEquals(Status.PENDENTE, resultado.getStatus());
    }

    @Test
    void deveLancarExcecaoQuandoHorarioNaoMultiploDe30() {
        LocalDate data = LocalDate.now().plusDays(1);
        LocalTime hora = LocalTime.of(10, 15);
        AgendamentoRequestDto dto = new AgendamentoRequestDto(cliente.getId(), "11999999999", data, hora, Servico.CABELEREIRO, null);

        when(usuarioService.getUsuarioLogado()).thenReturn(cliente);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> agendamentoService.agendar(dto));
        assertEquals("O horário deve ser múltiplo de 30 minutos, como 10:00, 10:30, etc.", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoHorarioForaFaixaPermitida() {
        LocalDate data = LocalDate.now().plusDays(1);
        LocalTime hora = LocalTime.NOON;
        AgendamentoRequestDto dto = new AgendamentoRequestDto(cliente.getId(), "11999999999", data, hora, Servico.MANICURE, null);

        when(usuarioService.getUsuarioLogado()).thenReturn(cliente);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> agendamentoService.agendar(dto));
        assertEquals("Apenas horários entre 08:00 e 11:30 ou 13:00 e 17:30 são permitidos.", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoClienteUltrapassaLimiteDiario() {
        LocalDate data = LocalDate.now().plusDays(1);
        LocalTime hora = LocalTime.of(10, 0);
        AgendamentoRequestDto dto = new AgendamentoRequestDto(cliente.getId(), "11999999999", data, hora, Servico.MANICURE, null);

        when(usuarioService.getUsuarioLogado()).thenReturn(cliente);
        when(agendamentoRepository.findByClienteIdAndData(cliente.getId(), data)).thenReturn(List.of(new Agendamento(), new Agendamento(), new Agendamento()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> agendamentoService.agendar(dto));
        assertEquals("Limite de 3 agendamentos por dia atingido.", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoClienteTentaAgendarParaOutro() {
        LocalDate data = LocalDate.now().plusDays(1);
        LocalTime hora = LocalTime.of(10, 0);
        AgendamentoRequestDto dto = new AgendamentoRequestDto(999L, "11999999999", data, hora, Servico.CABELEREIRO, null);

        when(usuarioService.getUsuarioLogado()).thenReturn(cliente);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> agendamentoService.agendar(dto));
        assertEquals("Clientes só podem criar agendamentos para si mesmos.", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoFuncionarioNaoDisponivel() {
        LocalDate data = LocalDate.now().plusDays(1);
        LocalTime hora = LocalTime.of(10, 0);
        AgendamentoRequestDto dto = new AgendamentoRequestDto(cliente.getId(), "11999999999", data, hora, Servico.CABELEREIRO, null);

        when(usuarioService.getUsuarioLogado()).thenReturn(cliente);
        when(agendamentoRepository.findByClienteIdAndData(cliente.getId(), data)).thenReturn(List.of());
        when(usuarioRepository.findByPerfilAndDisabledAtIsNull(Perfil.FUNCIONARIO)).thenReturn(List.of());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> agendamentoService.agendar(dto));
        assertEquals("Nenhum funcionário disponível para o horário informado.", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoHorarioConflitante() {
        LocalDate data = LocalDate.now().plusDays(1);
        LocalTime hora = LocalTime.of(10, 0);
        AgendamentoRequestDto dto = new AgendamentoRequestDto(cliente.getId(), "11999999999", data, hora, Servico.MANICURE, funcionario.getId());

        when(usuarioService.getUsuarioLogado()).thenReturn(cliente);
        when(agendamentoRepository.findByClienteIdAndData(cliente.getId(), data)).thenReturn(List.of());
        when(usuarioRepository.findById(funcionario.getId())).thenReturn(Optional.of(funcionario));
        when(agendamentoRepository.existsByClienteAndDataAndHoraAndStatus(any(), eq(data), eq(hora), eq(Status.PENDENTE))).thenReturn(false);
        when(agendamentoRepository.existsByFuncionarioAndDataAndHoraAndStatus(funcionario, data, hora, Status.PENDENTE)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> agendamentoService.agendar(dto));
        assertEquals("Já existe um agendamento para este horário.", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoDataDoAgendamentoPassada() {
        LocalDate data = LocalDate.now().minusDays(1);
        LocalTime hora = LocalTime.of(10, 0);
        AgendamentoRequestDto dto = new AgendamentoRequestDto(cliente.getId(), "11999999999", data, hora, Servico.CABELEREIRO, null);

        when(usuarioService.getUsuarioLogado()).thenReturn(cliente);
        when(agendamentoRepository.findByClienteIdAndData(cliente.getId(), data)).thenReturn(List.of());
        when(usuarioRepository.findByPerfilAndDisabledAtIsNull(Perfil.FUNCIONARIO)).thenReturn(List.of(funcionario));
        when(agendamentoRepository.existsByClienteAndDataAndHoraAndStatus(any(), eq(data), eq(hora), eq(Status.PENDENTE))).thenReturn(false);
        when(agendamentoRepository.existsByFuncionarioAndDataAndHoraAndStatus(funcionario, data, hora, Status.PENDENTE)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> agendamentoService.agendar(dto));
        assertEquals("A data do agendamento deve ser futura.", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoClienteInexistenteParaPerfilNaoCliente() {
        Usuario recepcionista = criarUsuario(10L, Perfil.RECEPCIONISTA);
        LocalDate data = LocalDate.now().plusDays(1);
        LocalTime hora = LocalTime.of(13, 0);
        AgendamentoRequestDto dto = new AgendamentoRequestDto(999L, "11999999999", data, hora, Servico.CABELEREIRO, funcionario.getId());

        when(usuarioService.getUsuarioLogado()).thenReturn(recepcionista);
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> agendamentoService.agendar(dto));
        assertEquals("Cliente não encontrado com o ID: 999", ex.getMessage());
    }

    @Test
    void deveCancelarAgendamentoQuandoClienteAutorizado() {
        Agendamento agendamento = criarAgendamento(1L, cliente, funcionario, LocalDate.now().plusDays(1), LocalTime.of(10, 0), Status.PENDENTE);

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        when(usuarioService.getUsuarioLogado()).thenReturn(cliente);
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Agendamento resultado = agendamentoService.cancelar(1L);

        assertEquals(Status.CANCELADO, resultado.getStatus());
        verify(agendamentoRepository).save(agendamento);
    }

    @Test
    void deveLancarExcecaoAoCancelarAgendamentoJaCancelado() {
        Agendamento agendamento = criarAgendamento(1L, cliente, funcionario, LocalDate.now().plusDays(1), LocalTime.of(10, 0), Status.CANCELADO);

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> agendamentoService.cancelar(1L));
        assertEquals("Agendamento já cancelado.", ex.getMessage());
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoCancelarAgendamentoPassado() {
        Agendamento agendamento = criarAgendamento(1L, cliente, funcionario, LocalDate.now().minusDays(1), LocalTime.of(10, 0), Status.PENDENTE);

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> agendamentoService.cancelar(1L));
        assertEquals("Agendamentos só podem ser cancelados até 24 horas antes do horário agendado.", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoAoCancelarSemPermissao() {
        Usuario outroCliente = criarUsuario(5L, Perfil.CLIENTE);
        Agendamento agendamento = criarAgendamento(1L, cliente, funcionario, LocalDate.now().plusDays(1), LocalTime.of(10, 0), Status.PENDENTE);

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        when(usuarioService.getUsuarioLogado()).thenReturn(outroCliente);

        SecurityException ex = assertThrows(SecurityException.class, () -> agendamentoService.cancelar(1L));
        assertEquals("Você não tem permissão para cancelar este agendamento.", ex.getMessage());
    }

    @Test
    void deveAtualizarAgendamentoQuandoHorarioConcluido() {
        LocalDate data = LocalDate.now().minusDays(1);
        LocalTime hora = LocalTime.of(9, 0);
        Agendamento agendamento = criarAgendamento(1L, cliente, funcionario, data, hora, Status.PENDENTE);

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Agendamento resultado = agendamentoService.atualizar(1L);

        assertEquals(Status.CONCLUIDO, resultado.getStatus());
        verify(agendamentoRepository).save(agendamento);
    }

    @Test
    void deveLancarExcecaoAoAtualizarAntesDoHorarioPermitido() {
        LocalDate data = LocalDate.now().plusDays(1);
        LocalTime hora = LocalTime.of(10, 0);
        Agendamento agendamento = criarAgendamento(1L, cliente, funcionario, data, hora, Status.PENDENTE);

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> agendamentoService.atualizar(1L));
        assertEquals("Agendamentos só podem ser concluídos 30 minutos após o horário agendado.", ex.getMessage());
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void deveRetornarVerdadeiroQuandoAgendamentoDoCliente() {
        Agendamento agendamento = criarAgendamento(1L, cliente, funcionario, LocalDate.now().plusDays(1), LocalTime.of(10, 0), Status.PENDENTE);

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        when(usuarioService.getUsuarioLogado()).thenReturn(cliente);

        assertTrue(agendamentoService.isAgendamentoDoCliente(1L));
    }

    @Test
    void deveRetornarFalsoQuandoAgendamentoNaoDoCliente() {
        Usuario outroCliente = criarUsuario(5L, Perfil.CLIENTE);
        Agendamento agendamento = criarAgendamento(1L, cliente, funcionario, LocalDate.now().plusDays(1), LocalTime.of(10, 0), Status.PENDENTE);

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        when(usuarioService.getUsuarioLogado()).thenReturn(outroCliente);

        assertFalse(agendamentoService.isAgendamentoDoCliente(1L));
    }

    @Test
    void deveRetornarVerdadeiroQuandoAgendamentoDoFuncionario() {
        Agendamento agendamento = criarAgendamento(1L, cliente, funcionario, LocalDate.now().plusDays(1), LocalTime.of(10, 0), Status.PENDENTE);

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        when(usuarioService.getUsuarioLogado()).thenReturn(funcionario);

        assertTrue(agendamentoService.isAgendamentoDoFuncionario(1L));
    }

    @Test
    void deveRetornarFalsoQuandoAgendamentoNaoDoFuncionario() {
        Usuario outroFuncionario = criarUsuario(6L, Perfil.FUNCIONARIO);
        Agendamento agendamento = criarAgendamento(1L, cliente, funcionario, LocalDate.now().plusDays(1), LocalTime.of(10, 0), Status.PENDENTE);

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        when(usuarioService.getUsuarioLogado()).thenReturn(outroFuncionario);

        assertFalse(agendamentoService.isAgendamentoDoFuncionario(1L));
    }

    private Usuario criarUsuario(Long id, Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Usuario " + id);
        usuario.setCpf("0000000000" + id);
        usuario.setEmail("usuario" + id + "@teste.com");
        usuario.setUsername("user" + id);
        usuario.setSenha("senha");
        usuario.setPerfil(perfil);
        Agenda agenda = new Agenda();
        agenda.setTrancamento(new Trancamento(true, null));
        usuario.setAgenda(agenda);
        return usuario;
    }

    private Agendamento criarAgendamento(Long id, Usuario cliente, Usuario funcionario, LocalDate data, LocalTime hora, Status status) {
        Agendamento agendamento = new Agendamento();
        agendamento.setId(id);
        agendamento.setCliente(cliente);
        agendamento.setFuncionario(funcionario);
        agendamento.setData(data);
        agendamento.setHora(hora);
        agendamento.setServico(Servico.CABELEREIRO);
        agendamento.setStatus(status);
        return agendamento;
    }
}