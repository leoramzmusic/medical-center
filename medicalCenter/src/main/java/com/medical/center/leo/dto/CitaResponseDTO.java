package com.medical.center.leo.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

public record CitaResponseDTO(
                Integer idCita,
                Integer consultorioNumero,
                Integer consultorioPiso,
                String medicoNombreCompleto,
                String medicoEspecialidad,
                @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime horarioConsulta,
                String nombrePaciente) {
}