package com.medical.center.leo.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record CitaRequestDTO(
                @NotNull(message = "ID de consultorio no puede ser nulo") Integer consultorioId,

                @NotNull(message = "ID de médico no puede ser nulo") Integer medicoId,

                @NotNull(message = "Horario de consulta no puede ser nulo") @Future(message = "La fecha de la cita debe ser en el futuro") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime horarioConsulta,

                @NotBlank(message = "Nombre del paciente no puede ser nulo") @Size(max = 100, message = "Nombre del paciente no debe exceder los 100 caracteres") String nombrePaciente) {
}