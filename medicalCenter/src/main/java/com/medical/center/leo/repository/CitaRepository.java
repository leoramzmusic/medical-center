package com.medical.center.leo.repository;

import com.medical.center.leo.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Integer> {

        // --- Métodos para validaciones de reglas de negocio ---

        // Regla 1: Consultorio ocupado a la misma hora
        @Query("SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END FROM Cita c " +
                        "WHERE c.consultorio.id_consultorio = :consultorioId AND c.horario_consulta = :horarioConsulta")
        boolean existsByConsultorioIdAndHorarioConsulta(@Param("consultorioId") Integer consultorioId,
                        @Param("horarioConsulta") LocalDateTime horarioConsulta);

        // Regla 2: Médico ocupado a la misma hora
        @Query("SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END FROM Cita c " +
                        "WHERE c.doctor.id_medico = :medicoId AND c.horario_consulta = :horarioConsulta")
        boolean existsByMedicoIdAndHorarioConsulta(@Param("medicoId") Integer medicoId,
                        @Param("horarioConsulta") LocalDateTime horarioConsulta);

        // Regla 3: Paciente con citas cercanas el mismo día (esta query ya está bien)
        @Query("SELECT c FROM Cita c WHERE c.nombre_paciente = :nombrePaciente " +
                        "AND FUNCTION('CONVERT', DATE, c.horario_consulta) = FUNCTION('CONVERT', DATE, :horarioReferencia) "
                        +
                        "AND c.horario_consulta >= :limiteInferior AND c.horario_consulta < :limiteSuperior")
        List<Cita> findCitasPacienteEnRangoHorario(
                        @Param("nombrePaciente") String nombrePaciente,
                        @Param("horarioReferencia") LocalDateTime horarioReferencia,
                        @Param("limiteInferior") LocalDateTime limiteInferior,
                        @Param("limiteSuperior") LocalDateTime limiteSuperior);

        // Regla 4: Límite de citas para un médico en un día
        @Query("SELECT COUNT(c) FROM Cita c WHERE c.doctor.id_medico = :medicoId " +
                        "AND FUNCTION('CONVERT', DATE, c.horario_consulta) = FUNCTION('CONVERT', DATE, :fecha)")
        long countByMedicoIdAndDia(@Param("medicoId") Integer medicoId, @Param("fecha") LocalDateTime fecha);

        // --- Métodos para consulta de citas ---
        // Para que se llame "findByHorarioConsultaBetween"
        // Spring Data JPA debería entender "HorarioConsulta" si el campo es
        // horario_consulta

        List<Cita> findByHorario_consultaBetween(LocalDateTime inicioDia, LocalDateTime finDia);

        // Para que se llame "findByHorarioConsultaBetweenAndMedicoId"
        @Query("SELECT c FROM Cita c WHERE c.doctor.id_medico = :medicoId " +
                        "AND c.horario_consulta BETWEEN :inicioDia AND :finDia")
        List<Cita> findByHorarioConsultaBetweenAndMedicoId(@Param("inicioDia") LocalDateTime inicioDia,
                        @Param("finDia") LocalDateTime finDia,
                        @Param("medicoId") Integer medicoId);

        // Para que se llame "findByHorarioConsultaBetweenAndConsultorioId"
        @Query("SELECT c FROM Cita c WHERE c.consultorio.id_consultorio = :consultorioId " +
                        "AND c.horario_consulta BETWEEN :inicioDia AND :finDia")
        List<Cita> findByHorarioConsultaBetweenAndConsultorioId(@Param("inicioDia") LocalDateTime inicioDia,
                        @Param("finDia") LocalDateTime finDia,
                        @Param("consultorioId") Integer consultorioId);

        // Para que se llame "findByHorarioConsultaBetweenAndConsultorioIdAndMedicoId"
        @Query("SELECT c FROM Cita c WHERE c.consultorio.id_consultorio = :consultorioId AND c.doctor.id_medico = :medicoId "
                        +
                        "AND c.horario_consulta BETWEEN :inicioDia AND :finDia")
        List<Cita> findByHorarioConsultaBetweenAndConsultorioIdAndMedicoId(@Param("inicioDia") LocalDateTime inicioDia,
                        @Param("finDia") LocalDateTime finDia,
                        @Param("consultorioId") Integer consultorioId,
                        @Param("medicoId") Integer medicoId);

        // Metodos para verificar si existen citas asociadas a un medico o consultorio
        @Query("SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END FROM Cita c WHERE c.doctor.id_medico = :medicoId")
        boolean medicoTieneCitas(@Param("medicoId") Integer medicoId);

        // Verifica si un consultorio tiene citas
        @Query("SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END FROM Cita c WHERE c.consultorio.id_consultorio = :consultorioId")
        boolean consultorioTieneCitas(@Param("consultorioId") Integer consultorioId);
}