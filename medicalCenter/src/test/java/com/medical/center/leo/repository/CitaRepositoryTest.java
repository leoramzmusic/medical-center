package com.medical.center.leo.repository;

import com.medical.center.leo.entity.Cita;
import com.medical.center.leo.entity.Consultorio;
import com.medical.center.leo.entity.Doctor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CitaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired(required = false)
    private ConsultorioRepository consultorioRepository;

    private Doctor doctor1;
    private Consultorio consultorio1;
    private Cita cita1, cita2;

    @BeforeEach
    void setUp() {
        doctor1 = new Doctor(null, "TestRepo", "DoctorRepo", "UnoRepo", "PediatriaRepo");
        entityManager.persist(doctor1);

        final int numeroConsultorioPrueba = 401;
        if (consultorioRepository != null) {
            Optional<Consultorio> optConsultorio = consultorioRepository
                    .findByNumero_consultorio(numeroConsultorioPrueba);
            if (optConsultorio.isPresent()) {
                consultorio1 = optConsultorio.get();
            } else {
                consultorio1 = new Consultorio(null, numeroConsultorioPrueba, 4);
                entityManager.persist(consultorio1);
            }
        } else {
            consultorio1 = new Consultorio(null, numeroConsultorioPrueba, 4);
            entityManager.persist(consultorio1);
        }

        entityManager.flush();

        LocalDateTime horario1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime horario2 = LocalDateTime.of(2026, 1, 15, 11, 0);

        cita1 = new Cita(null, consultorio1, doctor1, horario1, "PacienteRepo A");
        cita2 = new Cita(null, consultorio1, doctor1, horario2, "PacienteRepo B");
    }

    @Test
    void whenSaved_thenFindById() {
        Cita citaGuardada = citaRepository.save(cita1);
        entityManager.flush();

        Optional<Cita> encontrada = citaRepository.findById(citaGuardada.getId_cita());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getNombre_paciente()).isEqualTo(cita1.getNombre_paciente());
        assertThat(encontrada.get().getDoctor().getId_medico()).isEqualTo(doctor1.getId_medico());
    }

    @Test
    void existsByConsultorioIdAndHorarioConsulta_cuandoExiste_deberiaRetornarTrue() {
        citaRepository.save(cita1);
        entityManager.flush();
        boolean existe = citaRepository.existsByConsultorioIdAndHorarioConsulta(
                consultorio1.getId_consultorio(),
                cita1.getHorario_consulta());
        assertTrue(existe);
    }

    @Test
    void existsByConsultorioIdAndHorarioConsulta_cuandoNoExiste_deberiaRetornarFalse() {
        boolean existe = citaRepository.existsByConsultorioIdAndHorarioConsulta(
                999, // ID de consultorio no existente
                LocalDateTime.of(2026, 12, 1, 9, 0));
        assertFalse(existe);
    }

    @Test
    void existsByMedicoIdAndHorarioConsulta_cuandoExiste_deberiaRetornarTrue() {
        citaRepository.save(cita1);
        entityManager.flush();

        // LLAMADA CORREGIDA
        boolean existe = citaRepository.existsByMedicoIdAndHorarioConsulta(
                doctor1.getId_medico(),
                cita1.getHorario_consulta());
        assertTrue(existe);
    }

    @Test
    void countByMedicoIdAndDia_deberiaRetornarConteoCorrecto() {
        citaRepository.save(cita1);
        citaRepository.save(cita2);
        entityManager.flush();

        // LLAMADA CORREGIDA
        long conteo = citaRepository.countByMedicoIdAndDia(
                doctor1.getId_medico(),
                cita1.getHorario_consulta());
        assertEquals(2, conteo);
    }

    @Test
    void findCitasPacienteEnRangoHorario_deberiaRetornarCitasCorrectas() {
        citaRepository.save(cita1);
        entityManager.flush();

        LocalDateTime horarioReferencia = LocalDateTime.of(2026, 1, 15, 10, 30);
        LocalDateTime limiteInferior = horarioReferencia.minusHours(2);
        LocalDateTime limiteSuperior = horarioReferencia.plusHours(2);

        List<Cita> encontradas = citaRepository.findCitasPacienteEnRangoHorario(
                "PacienteRepo A",
                horarioReferencia,
                limiteInferior,
                limiteSuperior);
        assertThat(encontradas).hasSize(1);
        assertThat(encontradas.get(0).getNombre_paciente()).isEqualTo("PacienteRepo A");
    }

    @Test
    void findByHorario_consultaBetween_deberiaRetornarCitasEnRango() {
        citaRepository.save(cita1);
        citaRepository.save(cita2);

        Doctor doctor2 = new Doctor(null, "OtroRepo", "DocRepo", "", "GeneralRepo");
        entityManager.persist(doctor2);

        final int numeroConsultorioOtro = 402;
        Consultorio consultorio2;
        if (consultorioRepository != null) {
            Optional<Consultorio> optConsultorioOtro = consultorioRepository
                    .findByNumero_consultorio(numeroConsultorioOtro);
            if (optConsultorioOtro.isPresent()) {
                consultorio2 = optConsultorioOtro.get();
            } else {
                consultorio2 = new Consultorio(null, numeroConsultorioOtro, 4);
                entityManager.persist(consultorio2);
            }
        } else {
            consultorio2 = new Consultorio(null, numeroConsultorioOtro, 4);
            entityManager.persist(consultorio2);
        }

        Cita citaOtroDia = new Cita(null, consultorio2, doctor2, LocalDateTime.of(2026, 1, 16, 9, 0), "PacienteRepo C");
        citaRepository.save(citaOtroDia);
        entityManager.flush();

        LocalDate fechaConsulta = LocalDate.of(2026, 1, 15);
        LocalDateTime inicioDia = fechaConsulta.atStartOfDay();
        LocalDateTime finDia = fechaConsulta.atTime(LocalTime.MAX);
        // findByHorario_consultaBetween)
        List<Cita> encontradas = citaRepository.findByHorario_consultaBetween(inicioDia, finDia);

        // List<Cita> encontradas =
        // citaRepository.findByHorarioConsultaBetween(inicioDia, finDia);

        assertThat(encontradas).hasSize(2);
        assertThat(encontradas).extracting(Cita::getNombre_paciente).containsExactlyInAnyOrder("PacienteRepo A",
                "PacienteRepo B");
    }

    @Test
    void findByHorarioConsultaBetweenAndMedicoId_deberiaRetornarCitasCorrectas() {
        citaRepository.save(cita1);
        citaRepository.save(cita2);

        Doctor doctor2 = new Doctor(null, "DoctorDosRepo", "Repo", "Apellido", "CardioRepo");
        entityManager.persist(doctor2);
        Cita citaOtroDoctor = new Cita(null, consultorio1, doctor2, LocalDateTime.of(2026, 1, 15, 14, 0),
                "PacienteConOtroDoc");
        citaRepository.save(citaOtroDoctor);
        entityManager.flush();

        LocalDate fechaConsulta = LocalDate.of(2026, 1, 15);
        LocalDateTime inicioDia = fechaConsulta.atStartOfDay();
        LocalDateTime finDia = fechaConsulta.atTime(LocalTime.MAX);

        List<Cita> encontradas = citaRepository.findByHorarioConsultaBetweenAndMedicoId(
                inicioDia,
                finDia,
                doctor1.getId_medico());

        assertThat(encontradas).hasSize(2);
        assertThat(encontradas).allMatch(c -> c.getDoctor().getId_medico().equals(doctor1.getId_medico()));
        assertThat(encontradas).extracting(Cita::getNombre_paciente).containsExactlyInAnyOrder("PacienteRepo A",
                "PacienteRepo B");
    }
}