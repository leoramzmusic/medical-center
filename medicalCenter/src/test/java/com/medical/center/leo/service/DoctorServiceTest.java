package com.medical.center.leo.service;

import com.medical.center.leo.entity.Doctor;
import com.medical.center.leo.exception.BusinessRuleException;
import com.medical.center.leo.exception.ResourceNotFoundException;
import com.medical.center.leo.repository.CitaRepository;
import com.medical.center.leo.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor doctor1;
    private Doctor doctor2;
    private Doctor doctorToUpdate;

    @BeforeEach
    void setUp() {
        doctor1 = new Doctor(1, "Carlos", "Sanchez", "Rodriguez", "Cardiología");
        doctor2 = new Doctor(2, "Ana", "Gomez", "Lopez", "Pediatría");

        doctorToUpdate = new Doctor(null, "Carlos Alberto", "Sanchez", "Perez", "Cardiología Avanzada");
    }

    @Test
    void registrarDoctor_deberiaGuardarYRetornarDoctor() {
        Doctor nuevoDoctor = new Doctor(null, "Luis", "Martinez", "Fernandez", "General");
        // Simula que el doctor guardado es el mismo que el nuevo doctor, pero con ID
        // asignado
        Doctor doctorGuardado = new Doctor(3, "Luis", "Martinez", "Fernandez", "General");
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctorGuardado);
        Doctor resultado = doctorService.registrarDoctor(nuevoDoctor);
        assertNotNull(resultado);
        assertEquals(doctorGuardado.getId_medico(), resultado.getId_medico());
        assertEquals(nuevoDoctor.getNombre(), resultado.getNombre());
        verify(doctorRepository, times(1)).save(any(Doctor.class));
    }

    @Test
    void listarDoctores_deberiaRetornarListaDeDoctores() {
        List<Doctor> listaDoctores = Arrays.asList(doctor1, doctor2);
        when(doctorRepository.findAll()).thenReturn(listaDoctores);
        List<Doctor> resultado = doctorService.listarDoctores();
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(doctor1.getNombre(), resultado.get(0).getNombre());
        verify(doctorRepository, times(1)).findAll();
    }

    @Test
    void obtenerDoctorPorId_cuandoDoctorExiste_deberiaRetornarDoctor() {
        when(doctorRepository.findById(1)).thenReturn(Optional.of(doctor1));
        Doctor resultado = doctorService.obtenerDoctorPorId(1);
        assertNotNull(resultado);
        assertEquals(doctor1.getId_medico(), resultado.getId_medico());
        assertEquals(doctor1.getNombre(), resultado.getNombre());
        verify(doctorRepository, times(1)).findById(1);
    }

    @Test
    void obtenerDoctorPorId_cuandoDoctorNoExiste_lanzaResourceNotFoundException() {
        when(doctorRepository.findById(99)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            doctorService.obtenerDoctorPorId(99);
        });
        assertEquals("Doctor no encontrado con ID: 99", exception.getMessage());
        verify(doctorRepository, times(1)).findById(99);
    }

    @Test
    void actualizarDoctor_cuandoDoctorExiste_deberiaActualizarYRetornarDoctor() {
        Integer idExistente = 1;
        Doctor doctorExistente = new Doctor(idExistente, "Nombre Viejo", "Apellido Viejo", "", "Especialidad Vieja");
        Doctor doctorActualizadoConNuevosDatos = new Doctor(idExistente, doctorToUpdate.getNombre(),
                doctorToUpdate.getApellido_paterno(), doctorToUpdate.getApellido_materno(),
                doctorToUpdate.getEspecialidad());

        when(doctorRepository.findById(idExistente)).thenReturn(Optional.of(doctorExistente));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctorActualizadoConNuevosDatos);
        Doctor resultado = doctorService.actualizarDoctor(idExistente, doctorToUpdate);
        assertNotNull(resultado);
        assertEquals(idExistente, resultado.getId_medico());
        assertEquals(doctorToUpdate.getNombre(), resultado.getNombre());
        assertEquals(doctorToUpdate.getEspecialidad(), resultado.getEspecialidad());
        verify(doctorRepository, times(1)).findById(idExistente);
        verify(doctorRepository, times(1)).save(any(Doctor.class));
    }

    @Test
    void actualizarDoctor_cuandoDoctorNoExiste_lanzaResourceNotFoundException() {
        Integer idInexistente = 99;
        when(doctorRepository.findById(idInexistente)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            doctorService.actualizarDoctor(idInexistente, doctorToUpdate);
        });
        assertEquals("Doctor no encontrado con ID: " + idInexistente + " para actualizar.", exception.getMessage());
        verify(doctorRepository, times(1)).findById(idInexistente);
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void eliminarDoctor_cuandoDoctorExisteYSinCitas_deberiaEliminarDoctor() {
        Integer idExistente = 1;
        when(doctorRepository.findById(idExistente)).thenReturn(Optional.of(doctor1));
        when(citaRepository.medicoTieneCitas(idExistente)).thenReturn(false); // Doctor no tiene citas
        doNothing().when(doctorRepository).delete(doctor1);
        doctorService.eliminarDoctor(idExistente);
        verify(doctorRepository, times(1)).findById(idExistente);
        verify(citaRepository, times(1)).medicoTieneCitas(idExistente);
        verify(doctorRepository, times(1)).delete(doctor1);
    }

    @Test
    void eliminarDoctor_cuandoDoctorNoExiste_lanzaResourceNotFoundException() {
        Integer idInexistente = 99;
        when(doctorRepository.findById(idInexistente)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            doctorService.eliminarDoctor(idInexistente);
        });
        assertEquals("Doctor no encontrado con ID: " + idInexistente + " para eliminar.", exception.getMessage());
        verify(doctorRepository, times(1)).findById(idInexistente);
        verify(citaRepository, never()).medicoTieneCitas(anyInt());
        verify(doctorRepository, never()).delete(any(Doctor.class));
    }

    @Test
    void eliminarDoctor_cuandoDoctorTieneCitas_lanzaBusinessRuleException() {
        Integer idExistenteConCitas = 1;
        when(doctorRepository.findById(idExistenteConCitas)).thenReturn(Optional.of(doctor1));
        when(citaRepository.medicoTieneCitas(idExistenteConCitas)).thenReturn(true); // Doctor SÍ tiene citas
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            doctorService.eliminarDoctor(idExistenteConCitas);
        });
        assertTrue(exception.getMessage().contains("No se puede eliminar el doctor con ID: " + idExistenteConCitas));
        verify(doctorRepository, times(1)).findById(idExistenteConCitas);
        verify(citaRepository, times(1)).medicoTieneCitas(idExistenteConCitas);
        verify(doctorRepository, never()).delete(any(Doctor.class));
    }
}