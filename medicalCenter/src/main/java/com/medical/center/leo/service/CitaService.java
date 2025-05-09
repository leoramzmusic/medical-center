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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CitaService {

        private final CitaRepository citaRepository;
        private final DoctorRepository doctorRepository;
        private final ConsultorioRepository consultorioRepository;

        public CitaService(CitaRepository citaRepository, DoctorRepository doctorRepository,
                        ConsultorioRepository consultorioRepository) {
                this.citaRepository = citaRepository;
                this.doctorRepository = doctorRepository;
                this.consultorioRepository = consultorioRepository;
        }

        @Transactional
        public CitaResponseDTO crearCita(CitaRequestDTO request) {
                Doctor doctor = doctorRepository.findById(request.medicoId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Doctor no encontrado con ID: " + request.medicoId()));
                Consultorio consultorio = consultorioRepository.findById(request.consultorioId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Consultorio no encontrado con ID: " + request.consultorioId()));

                validarReglasNegocio(request.medicoId(), request.consultorioId(), request.horarioConsulta(),
                                request.nombrePaciente(), null);

                Cita nuevaCita = new Cita();
                nuevaCita.setDoctor(doctor);
                nuevaCita.setConsultorio(consultorio);
                nuevaCita.setHorario_consulta(request.horarioConsulta());
                nuevaCita.setNombre_paciente(request.nombrePaciente());

                Cita citaGuardada = citaRepository.save(nuevaCita);
                return mapToCitaResponseDTO(citaGuardada);
        }

        @Transactional
        public CitaResponseDTO editarCita(Integer citaId, CitaRequestDTO request) {
                Cita citaExistente = citaRepository.findById(citaId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Cita no encontrada con ID: " + citaId));

                Doctor doctor = doctorRepository.findById(request.medicoId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Doctor no encontrado con ID: " + request.medicoId()));
                Consultorio consultorio = consultorioRepository.findById(request.consultorioId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Consultorio no encontrado con ID: " + request.consultorioId()));

                validarReglasNegocio(request.medicoId(), request.consultorioId(), request.horarioConsulta(),
                                request.nombrePaciente(), citaId);

                citaExistente.setDoctor(doctor);
                citaExistente.setConsultorio(consultorio);
                citaExistente.setHorario_consulta(request.horarioConsulta());
                citaExistente.setNombre_paciente(request.nombrePaciente());

                Cita citaActualizada = citaRepository.save(citaExistente);
                return mapToCitaResponseDTO(citaActualizada);
        }

        private void validarReglasNegocio(Integer medicoId, Integer consultorioId, LocalDateTime horarioConsulta,
                        String nombrePaciente, Integer citaIdExcluir) {

                // Regla 1: No se puede agendar cita en un mismo consultorio a la misma hora.
                if (citaRepository.existsByConsultorioIdAndHorarioConsulta(consultorioId, horarioConsulta)) {

                        // Necesitamos verificar si la cita existente es la misma que estamos editando
                        if (citaIdExcluir == null || !esMismaCitaConflictiva(consultorioId, null, horarioConsulta,
                                        citaIdExcluir)) {
                                throw new BusinessRuleException("Ya existe otra cita en el consultorio " + consultorioId
                                                + " a las " + horarioConsulta);
                        }
                }

                // Regla 2: No se puede agendar cita para un mismo doctor a la misma hora.
                if (citaRepository.existsByMedicoIdAndHorarioConsulta(medicoId, horarioConsulta)) {
                        if (citaIdExcluir == null
                                        || !esMismaCitaConflictiva(null, medicoId, horarioConsulta, citaIdExcluir)) {
                                throw new BusinessRuleException("El doctor " + medicoId + " ya tiene otra cita a las "
                                                + horarioConsulta);
                        }
                }

                // Regla 3: Paciente con citas cercanas el mismo dia
                LocalDateTime rangoInicioPaciente = horarioConsulta.minusHours(2).plusMinutes(1);
                LocalDateTime rangoFinPaciente = horarioConsulta.plusHours(2).minusMinutes(1);

                List<Cita> citasPacienteConflictivas = citaRepository.findCitasPacienteEnRangoHorario(
                                nombrePaciente,
                                horarioConsulta,
                                rangoInicioPaciente,
                                rangoFinPaciente);

                for (Cita citaExistentePaciente : citasPacienteConflictivas) {
                        if (citaIdExcluir == null || !citaExistentePaciente.getId_cita().equals(citaIdExcluir)) {
                                if (Math.abs(java.time.Duration
                                                .between(citaExistentePaciente.getHorario_consulta(), horarioConsulta)
                                                .toMinutes()) < 120) {
                                        throw new BusinessRuleException("El paciente " + nombrePaciente
                                                        + " ya tiene otra cita programada dentro de las 2 horas en el mismo día.");
                                }
                        }
                }

                // Regla 4: Un mismo doctor no puede tener más de 8 citas en un dia.
                long citasDelDoctorEnDia = citaRepository.countByMedicoIdAndDia(medicoId, horarioConsulta);

                boolean estaEditandoMismaCitaMismoDiaDoctor = false;
                if (citaIdExcluir != null) {
                        Cita citaOriginal = citaRepository.findById(citaIdExcluir).orElse(null);
                        if (citaOriginal != null &&
                                        citaOriginal.getDoctor().getId_medico().equals(medicoId) &&
                                        citaOriginal.getHorario_consulta().toLocalDate()
                                                        .equals(horarioConsulta.toLocalDate())) {
                                estaEditandoMismaCitaMismoDiaDoctor = true;
                        }
                }

                if (estaEditandoMismaCitaMismoDiaDoctor) {
                        if (citasDelDoctorEnDia > 8) {
                                throw new BusinessRuleException(
                                                "El doctor " + medicoId + " excedería el límite de 8 citas para el "
                                                                + horarioConsulta.toLocalDate() + " con esta edición.");
                        }
                } else {
                        if (citasDelDoctorEnDia >= 8) {
                                throw new BusinessRuleException(
                                                "El doctor " + medicoId + " ya tiene 8 citas agendadas para el "
                                                                + horarioConsulta.toLocalDate());
                        }
                }
        }

        /**
         * Verifica si la cita editada es la misma que la cita conflictiva.
         * 
         * @param consultorioId  ID del consultorio
         * @param medicoId       ID del médico
         * @param horario        Horario de la cita
         * @param citaIdEditando ID de la cita que se está editando
         * @return true si es la misma cita, false en caso contrario
         */
        private boolean esMismaCitaConflictiva(Integer consultorioId, Integer medicoId, LocalDateTime horario,
                        Integer citaIdEditando) {
                if (citaIdEditando == null)
                        return false;

                Cita citaEditada = citaRepository.findById(citaIdEditando).orElse(null);
                if (citaEditada == null)
                        return false; // Cita no encontrada
                boolean mismoHorario = citaEditada.getHorario_consulta().equals(horario);
                boolean mismoConsultorio = consultorioId != null
                                && citaEditada.getConsultorio().getId_consultorio().equals(consultorioId);
                boolean mismoMedico = medicoId != null && citaEditada.getDoctor().getId_medico().equals(medicoId);

                if (consultorioId != null) { // Chequeando conflicto de consultorio
                        return mismoHorario && mismoConsultorio;
                }
                if (medicoId != null) { // Chequeando conflicto de medico
                        return mismoHorario && mismoMedico;
                }
                return false;
        }

        @Transactional(readOnly = true)
        public List<CitaResponseDTO> consultarCitas(LocalDate fecha, Integer consultorioId, Integer medicoId) {
                LocalDateTime inicioDia = fecha.atStartOfDay();
                LocalDateTime finDia = fecha.atTime(LocalTime.MAX);
                List<Cita> citas;

                // Verificar si se pasan ambos IDs
                if (consultorioId != null && medicoId != null) {
                        citas = citaRepository.findByHorarioConsultaBetweenAndConsultorioIdAndMedicoId(
                                        inicioDia, finDia, consultorioId, medicoId);
                } else if (consultorioId != null) {
                        citas = citaRepository.findByHorarioConsultaBetweenAndConsultorioId(inicioDia,
                                        finDia, consultorioId);
                } else if (medicoId != null) {
                        citas = citaRepository.findByHorarioConsultaBetweenAndMedicoId(inicioDia, finDia,
                                        medicoId);
                } else {
                        citas = citaRepository.findByHorario_consultaBetween(inicioDia, finDia);
                }
                return citas.stream().map(this::mapToCitaResponseDTO).collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public CitaResponseDTO obtenerCitaPorId(Integer idCita) {
                Cita cita = citaRepository.findById(idCita)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Cita no encontrada con ID: " + idCita));
                return mapToCitaResponseDTO(cita);
        }

        /**
         * Cancela una cita existente.
         * 
         * @param citaId ID de la cita a cancelar
         */
        @Transactional
        public void cancelarCita(Integer citaId) {
                Cita cita = citaRepository.findById(citaId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Cita no encontrada con ID: " + citaId));

                if (cita.getHorario_consulta().isBefore(LocalDateTime.now())) {
                        throw new BusinessRuleException("No se puede cancelar una cita que ya ha pasado.");
                }
                citaRepository.delete(cita);
        }

        /**
         * Mapea una entidad Cita a un DTO de respuesta.
         * 
         * @param cita La entidad Cita a mapear
         * @return Un DTO de respuesta con los datos de la cita
         */
        private CitaResponseDTO mapToCitaResponseDTO(Cita cita) {
                String nombreCompletoMedico = (cita.getDoctor() != null)
                                ? cita.getDoctor().getNombre() + " " + cita.getDoctor().getApellido_paterno()
                                : "N/A";
                String especialidadMedico = (cita.getDoctor() != null) ? cita.getDoctor().getEspecialidad() : "N/A";
                Integer numeroConsultorio = (cita.getConsultorio() != null)
                                ? cita.getConsultorio().getNumero_consultorio()
                                : null;
                Integer pisoConsultorio = (cita.getConsultorio() != null) ? cita.getConsultorio().getPiso() : null;

                return new CitaResponseDTO(
                                cita.getId_cita(),
                                numeroConsultorio,
                                pisoConsultorio,
                                nombreCompletoMedico,
                                especialidadMedico,
                                cita.getHorario_consulta(),
                                cita.getNombre_paciente());
        }
}