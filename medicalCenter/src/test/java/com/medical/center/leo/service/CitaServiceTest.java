package com.medical.center.leo.service;

import com.medical.center.leo.dto.CitaRequestDTO;
import com.medical.center.leo.dto.CitaResponseDTO;
import com.medical.center.leo.entity.Cita;
import com.medical.center.leo.entity.Consultorio;
import com.medical.center.leo.entity.Doctor;
import com.medical.center.leo.exception.BusinessRuleException;
import com.medical.center.leo.exception.ResourceNotFoundException;
import com.medical.center.leo.repository.CitaRepository;
import com.medical.center.leo.repository.ConsultorioRepository;
import com.medical.center.leo.repository.DoctorRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private ConsultorioRepository consultorioRepository;

    @InjectMocks
    private CitaService citaService;

    private Doctor doctorMock;
    private Consultorio consultorioMock;
    private CitaRequestDTO citaRequestDTOMock;
    private Cita citaMock;

    @BeforeEach
    void setUp() {
        doctorMock = new Doctor(1, "Juan", "Perez", "Gomez", "Cardiología");
        consultorioMock = new Consultorio(1, 101, 1);
        LocalDateTime horarioCita = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0)
                .withNano(0);
        citaRequestDTOMock = new CitaRequestDTO(
                consultorioMock.getId_consultorio(),
                doctorMock.getId_medico(),
                horarioCita,
                "Paciente Test");

        citaMock = new Cita();
        citaMock.setId_cita(1);
        citaMock.setDoctor(doctorMock); // Asignar el médico al objeto Cita
        citaMock.setConsultorio(consultorioMock);
        citaMock.setHorario_consulta(horarioCita);
        citaMock.setNombre_paciente("Paciente Test");
    }

    private void mockValidacionesBasicasExitosas(CitaRequestDTO request) {
        // existsByConsultorioIdAndHorarioConsulta
        // existsByMedicoIdAndHorarioConsulta
        // countByMedicoIdAndDia
        when(citaRepository.existsByConsultorioIdAndHorarioConsulta(request.consultorioId(), request.horarioConsulta()))
                .thenReturn(false);
        when(citaRepository.existsByMedicoIdAndHorarioConsulta(request.medicoId(), request.horarioConsulta()))
                .thenReturn(false);
        when(citaRepository.findCitasPacienteEnRangoHorario(
                eq(request.nombrePaciente()),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(citaRepository.countByMedicoIdAndDia(request.medicoId(), request.horarioConsulta())).thenReturn(0L);
        when(doctorRepository.findById(request.medicoId())).thenReturn(Optional.of(doctorMock));
        when(consultorioRepository.findById(request.consultorioId())).thenReturn(Optional.of(consultorioMock));
    }

    private void mockValidacionesBasicasExitosasEdicion(CitaRequestDTO request, Integer citaIdExcluir) {
        when(citaRepository.findById(citaIdExcluir)).thenReturn(Optional.of(citaMock));
        when(citaRepository.existsByConsultorioIdAndHorarioConsulta(request.consultorioId(), request.horarioConsulta()))
                .thenReturn(false);
        when(citaRepository.existsByMedicoIdAndHorarioConsulta(request.medicoId(), request.horarioConsulta()))
                .thenReturn(false);
        when(citaRepository.findCitasPacienteEnRangoHorario(
                eq(request.nombrePaciente()),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(citaRepository.countByMedicoIdAndDia(request.medicoId(), request.horarioConsulta())).thenReturn(0L);
        when(doctorRepository.findById(request.medicoId())).thenReturn(Optional.of(doctorMock));
        when(consultorioRepository.findById(request.consultorioId())).thenReturn(Optional.of(consultorioMock));
    }

    @Test
    void crearCita_cuandoTodoEsValido_deberiaCrearCita() {
        mockValidacionesBasicasExitosas(citaRequestDTOMock);
        when(citaRepository.save(any(Cita.class))).thenReturn(citaMock);

        CitaResponseDTO response = citaService.crearCita(citaRequestDTOMock);

        assertNotNull(response);
        assertEquals(citaRequestDTOMock.nombrePaciente(), response.nombrePaciente());
        assertEquals(doctorMock.getNombre() + " " + doctorMock.getApellido_paterno(), response.medicoNombreCompleto());
        verify(citaRepository, times(1)).save(any(Cita.class));
    }

    @Test
    void crearCita_cuandoConsultorioNoEncontrado_lanzaResourceNotFoundException() {
        when(doctorRepository.findById(citaRequestDTOMock.medicoId())).thenReturn(Optional.of(doctorMock));
        when(consultorioRepository.findById(citaRequestDTOMock.consultorioId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> citaService.crearCita(citaRequestDTOMock));
    }

    @Test
    void crearCita_cuandoDoctorNoEncontrado_lanzaResourceNotFoundException() {
        when(doctorRepository.findById(citaRequestDTOMock.medicoId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> citaService.crearCita(citaRequestDTOMock));
    }

    @Test
    void crearCita_cuandoConsultorioOcupado_lanzaBusinessRuleException() {
        when(doctorRepository.findById(citaRequestDTOMock.medicoId())).thenReturn(Optional.of(doctorMock));
        when(consultorioRepository.findById(citaRequestDTOMock.consultorioId()))
                .thenReturn(Optional.of(consultorioMock));
        when(citaRepository.existsByConsultorioIdAndHorarioConsulta(citaRequestDTOMock.consultorioId(),
                citaRequestDTOMock.horarioConsulta())).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> citaService.crearCita(citaRequestDTOMock));
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    void crearCita_cuandoMedicoOcupado_lanzaBusinessRuleException() {
        when(doctorRepository.findById(citaRequestDTOMock.medicoId())).thenReturn(Optional.of(doctorMock));
        when(consultorioRepository.findById(citaRequestDTOMock.consultorioId()))
                .thenReturn(Optional.of(consultorioMock));
        when(citaRepository.existsByConsultorioIdAndHorarioConsulta(citaRequestDTOMock.consultorioId(),
                citaRequestDTOMock.horarioConsulta())).thenReturn(false);
        when(citaRepository.existsByMedicoIdAndHorarioConsulta(citaRequestDTOMock.medicoId(),
                citaRequestDTOMock.horarioConsulta())).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> citaService.crearCita(citaRequestDTOMock));
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    void crearCita_cuandoPacienteTieneCitaSolapadaMenos2Horas_lanzaBusinessRuleException() {
        LocalDateTime horarioNuevaCita = LocalDateTime.now().plusDays(1).withHour(15).withMinute(0);
        LocalDateTime horarioCitaExistente = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);

        CitaRequestDTO requestConflictiva = new CitaRequestDTO(
                consultorioMock.getId_consultorio(),
                doctorMock.getId_medico(),
                horarioNuevaCita,
                "Paciente Conflictivo");

        Cita citaExistentePaciente = new Cita();
        citaExistentePaciente.setId_cita(99);
        citaExistentePaciente.setNombre_paciente("Paciente Conflictivo");
        citaExistentePaciente.setHorario_consulta(horarioCitaExistente);
        citaExistentePaciente.setDoctor(doctorMock);
        citaExistentePaciente.setConsultorio(consultorioMock);

        // Mockear entidades base
        when(doctorRepository.findById(requestConflictiva.medicoId())).thenReturn(Optional.of(doctorMock));
        when(consultorioRepository.findById(requestConflictiva.consultorioId()))
                .thenReturn(Optional.of(consultorioMock));
        // Mockear validaciones que deben pasar
        when(citaRepository.existsByConsultorioIdAndHorarioConsulta(requestConflictiva.consultorioId(),
                requestConflictiva.horarioConsulta())).thenReturn(false);
        when(citaRepository.existsByMedicoIdAndHorarioConsulta(requestConflictiva.medicoId(),
                requestConflictiva.horarioConsulta())).thenReturn(false);
        // Mockear la condición de fallo
        when(citaRepository.findCitasPacienteEnRangoHorario(
                eq(requestConflictiva.nombrePaciente()),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(Collections.singletonList(citaExistentePaciente));

        assertThrows(BusinessRuleException.class, () -> citaService.crearCita(requestConflictiva));
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    void crearCita_cuandoPacienteTieneCitaMismaHora_lanzaBusinessRuleException() {
        LocalDateTime horarioCita = LocalDateTime.now().plusDays(1).withHour(11).withMinute(0);

        CitaRequestDTO requestConflictiva = new CitaRequestDTO(
                consultorioMock.getId_consultorio(),
                doctorMock.getId_medico(),
                horarioCita,
                "Paciente Mismo Horario");

        Cita citaExistentePaciente = new Cita();
        citaExistentePaciente.setId_cita(98);
        citaExistentePaciente.setNombre_paciente("Paciente Mismo Horario");
        citaExistentePaciente.setHorario_consulta(horarioCita);
        citaExistentePaciente.setDoctor(doctorMock);
        citaExistentePaciente.setConsultorio(consultorioMock);

        when(doctorRepository.findById(requestConflictiva.medicoId())).thenReturn(Optional.of(doctorMock));
        when(consultorioRepository.findById(requestConflictiva.consultorioId()))
                .thenReturn(Optional.of(consultorioMock));
        when(citaRepository.existsByConsultorioIdAndHorarioConsulta(anyInt(), any(LocalDateTime.class)))
                .thenReturn(false);
        when(citaRepository.existsByMedicoIdAndHorarioConsulta(anyInt(), any(LocalDateTime.class))).thenReturn(false);
        when(citaRepository.findCitasPacienteEnRangoHorario(
                eq(requestConflictiva.nombrePaciente()),
                any(LocalDateTime.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(citaExistentePaciente));

        assertThrows(BusinessRuleException.class, () -> citaService.crearCita(requestConflictiva));
    }

    @Test
    void crearCita_cuandoMedicoTiene8CitasEnDia_lanzaBusinessRuleException() {
        mockValidacionesBasicasExitosas(citaRequestDTOMock);
        when(citaRepository.countByMedicoIdAndDia(citaRequestDTOMock.medicoId(), citaRequestDTOMock.horarioConsulta()))
                .thenReturn(8L); // Simulando que ya tiene 8 citas

        assertThrows(BusinessRuleException.class, () -> citaService.crearCita(citaRequestDTOMock));
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    void editarCita_cuandoTodoEsValido_deberiaActualizarCita() {
        Integer citaIdParaEditar = citaMock.getId_cita();
        CitaRequestDTO requestEdicion = new CitaRequestDTO(
                consultorioMock.getId_consultorio(),
                doctorMock.getId_medico(),
                citaRequestDTOMock.horarioConsulta().plusHours(1),
                "Paciente Editado");

        mockValidacionesBasicasExitosasEdicion(requestEdicion, citaIdParaEditar);

        // when(citaRepository.findById(citaIdParaEditar)).thenReturn(Optional.of(citaMock));
        // // Ya está en mockValidacionesBasicasExitosasEdicion
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CitaResponseDTO response = citaService.editarCita(citaIdParaEditar, requestEdicion);

        assertNotNull(response);
        assertEquals(requestEdicion.nombrePaciente(), response.nombrePaciente());
        assertEquals(requestEdicion.horarioConsulta(), response.horarioConsulta());
        verify(citaRepository, times(1)).save(any(Cita.class));
    }

    @Test
    void editarCita_cuandoCitaNoExiste_lanzaResourceNotFoundException() {
        Integer citaIdInexistente = 999;
        when(citaRepository.findById(citaIdInexistente)).thenReturn(Optional.empty());
        when(doctorRepository.findById(anyInt())).thenReturn(Optional.of(doctorMock));
        when(consultorioRepository.findById(anyInt())).thenReturn(Optional.of(consultorioMock));

        assertThrows(ResourceNotFoundException.class,
                () -> citaService.editarCita(citaIdInexistente, citaRequestDTOMock));
    }

    @Test
    void editarCita_cuandoConsultorioOcupadoPorOtraCita_lanzaBusinessRuleException() {
        Integer citaIdParaEditar = citaMock.getId_cita();
        CitaRequestDTO requestEdicionConflictiva = new CitaRequestDTO(
                consultorioMock.getId_consultorio(),
                doctorMock.getId_medico(),
                citaRequestDTOMock.horarioConsulta(), // Mismo horario que podría causar conflicto
                "Paciente Editado");

        // Mockear entidades base
        when(doctorRepository.findById(anyInt())).thenReturn(Optional.of(doctorMock));
        when(consultorioRepository.findById(anyInt())).thenReturn(Optional.of(consultorioMock));
        // Mockear la cita a editar
        when(citaRepository.findById(citaIdParaEditar)).thenReturn(Optional.of(citaMock));
        // Mockear la condición de fallo (consultorio ocupado por OTRA cita)
        when(citaRepository.existsByConsultorioIdAndHorarioConsulta(requestEdicionConflictiva.consultorioId(),
                requestEdicionConflictiva.horarioConsulta())).thenReturn(true);

        assertThrows(BusinessRuleException.class,
                () -> citaService.editarCita(citaIdParaEditar, requestEdicionConflictiva));
    }

    @Test
    void cancelarCita_cuandoCitaExisteYEsFutura_deberiaEliminarCita() {
        when(citaRepository.findById(citaMock.getId_cita())).thenReturn(Optional.of(citaMock));
        citaMock.setHorario_consulta(LocalDateTime.now().plusDays(1));

        citaService.cancelarCita(citaMock.getId_cita());

        verify(citaRepository, times(1)).delete(citaMock);
    }

    @Test
    void cancelarCita_cuandoCitaNoExiste_lanzaResourceNotFoundException() {
        Integer citaIdInexistente = 999;
        when(citaRepository.findById(citaIdInexistente)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> citaService.cancelarCita(citaIdInexistente));
    }

    @Test
    void cancelarCita_cuandoCitaYaPaso_lanzaBusinessRuleException() {
        when(citaRepository.findById(citaMock.getId_cita())).thenReturn(Optional.of(citaMock));
        citaMock.setHorario_consulta(LocalDateTime.now().minusDays(1));

        assertThrows(BusinessRuleException.class, () -> citaService.cancelarCita(citaMock.getId_cita()));
        verify(citaRepository, never()).delete(any(Cita.class));
    }

    @Test
    void consultarCitas_porFecha_deberiaDevolverCitasDeEsaFecha() {
        LocalDate fechaConsulta = LocalDate.now().plusDays(1);
        LocalDateTime inicioDia = fechaConsulta.atStartOfDay();
        LocalDateTime finDia = fechaConsulta.atTime(LocalTime.MAX);

        Cita cita1 = new Cita(1, consultorioMock, doctorMock, fechaConsulta.atTime(10, 0), "Paciente 1");
        Cita cita2 = new Cita(2, consultorioMock, doctorMock, fechaConsulta.atTime(11, 0), "Paciente 2");
        List<Cita> citasDelDia = Arrays.asList(cita1, cita2);

        when(citaRepository.findByHorario_consultaBetween(inicioDia, finDia)).thenReturn(citasDelDia);

        List<CitaResponseDTO> resultado = citaService.consultarCitas(fechaConsulta, null, null);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Paciente 1", resultado.get(0).nombrePaciente());
        verify(citaRepository, times(1)).findByHorario_consultaBetween(inicioDia, finDia);
    }

    @Test
    void consultarCitas_porFechaYDoctor_deberiaDevolverCitasCorrectas() {
        LocalDate fechaConsulta = LocalDate.now().plusDays(1);
        LocalDateTime inicioDia = fechaConsulta.atStartOfDay();
        LocalDateTime finDia = fechaConsulta.atTime(LocalTime.MAX);
        Integer medicoId = doctorMock.getId_medico();

        Cita cita1 = new Cita(1, consultorioMock, doctorMock, fechaConsulta.atTime(10, 0), "Paciente 1");
        List<Cita> citasFiltradas = Collections.singletonList(cita1);

        when(citaRepository.findByHorarioConsultaBetweenAndMedicoId(inicioDia, finDia, medicoId))
                .thenReturn(citasFiltradas);

        List<CitaResponseDTO> resultado = citaService.consultarCitas(fechaConsulta, null, medicoId);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Paciente 1", resultado.get(0).nombrePaciente());
        verify(citaRepository, times(1)).findByHorarioConsultaBetweenAndMedicoId(inicioDia, finDia, medicoId);
    }

    @Test
    void consultarCitas_porFechaYConsultorio_deberiaDevolverCitasCorrectas() {
        LocalDate fechaConsulta = LocalDate.now().plusDays(1);
        LocalDateTime inicioDia = fechaConsulta.atStartOfDay();
        LocalDateTime finDia = fechaConsulta.atTime(LocalTime.MAX);
        Integer consultorioId = consultorioMock.getId_consultorio();

        Cita cita1 = new Cita(1, consultorioMock, doctorMock, fechaConsulta.atTime(10, 0), "Paciente 1");
        List<Cita> citasFiltradas = Collections.singletonList(cita1);

        when(citaRepository.findByHorarioConsultaBetweenAndConsultorioId(inicioDia, finDia, consultorioId))
                .thenReturn(citasFiltradas);

        List<CitaResponseDTO> resultado = citaService.consultarCitas(fechaConsulta, consultorioId, null);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Paciente 1", resultado.get(0).nombrePaciente());
        verify(citaRepository, times(1)).findByHorarioConsultaBetweenAndConsultorioId(inicioDia, finDia, consultorioId);
    }

    @Test
    void consultarCitas_porFechaYConsultorioYMedico_deberiaDevolverCitasCorrectas() {
        LocalDate fechaConsulta = LocalDate.now().plusDays(1);
        LocalDateTime inicioDia = fechaConsulta.atStartOfDay();
        LocalDateTime finDia = fechaConsulta.atTime(LocalTime.MAX);
        Integer consultorioId = consultorioMock.getId_consultorio();
        Integer medicoId = doctorMock.getId_medico();

        Cita cita1 = new Cita(1, consultorioMock, doctorMock, fechaConsulta.atTime(10, 0), "Paciente 1");
        List<Cita> citasFiltradas = Collections.singletonList(cita1);

        when(citaRepository.findByHorarioConsultaBetweenAndConsultorioIdAndMedicoId(inicioDia, finDia, consultorioId,
                medicoId))
                .thenReturn(citasFiltradas);

        List<CitaResponseDTO> resultado = citaService.consultarCitas(fechaConsulta, consultorioId, medicoId);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Paciente 1", resultado.get(0).nombrePaciente());
        verify(citaRepository, times(1)).findByHorarioConsultaBetweenAndConsultorioIdAndMedicoId(inicioDia, finDia,
                consultorioId, medicoId);
    }
}