package com.medical.center.leo.controller;

import com.medical.center.leo.dto.CitaRequestDTO;
import com.medical.center.leo.dto.CitaResponseDTO;
import com.medical.center.leo.service.CitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
@Tag(name = "Citas API", description = "Operaciones para la gestión de citas médicas")
public class CitaController {

    private final CitaService citaService;

    // Inyección de dependencias a través del constructor
    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Ejemplo de autorización
    @Operation(summary = "Crear una nueva cita médica", description = "Registra una nueva cita validando las reglas de negocio.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cita creada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CitaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o violación de regla de negocio"),
            @ApiResponse(responseCode = "401", description = "No autorizado para realizar esta acción"),
            @ApiResponse(responseCode = "404", description = "Médico o Consultorio no encontrado")
    })
    public ResponseEntity<CitaResponseDTO> crearCita(
            @Parameter(description = "Datos para la nueva cita", required = true) @Valid @RequestBody CitaRequestDTO citaRequestDTO) {
        CitaResponseDTO nuevaCita = citaService.crearCita(citaRequestDTO);
        return new ResponseEntity<>(nuevaCita, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Consultar citas", description = "Obtiene una lista de citas filtradas por fecha y opcionalmente por ID de consultorio y/o ID de médico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Citas encontradas", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CitaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros de búsqueda inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<List<CitaResponseDTO>> consultarCitas(
            @Parameter(description = "Fecha para la consulta de citas (formato YYYY-MM-DD)", required = true, example = "2024-12-31") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,

            @Parameter(description = "ID del consultorio para filtrar (opcional)", example = "1") @RequestParam(required = false) Integer consultorioId,

            @Parameter(description = "ID del médico para filtrar (opcional)", example = "1") @RequestParam(required = false) Integer medicoId) {
        List<CitaResponseDTO> citas = citaService.consultarCitas(fecha, consultorioId, medicoId);
        return ResponseEntity.ok(citas);
    }

    @PutMapping("/{idCita}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Editar una cita existente", description = "Actualiza los datos de una cita existente, respetando las reglas de negocio.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cita actualizada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CitaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o violación de regla de negocio"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Cita, Médico o Consultorio no encontrado")
    })
    public ResponseEntity<CitaResponseDTO> editarCita(
            @Parameter(description = "ID de la cita a editar", required = true, example = "1") @PathVariable("idCita") Integer idCita,

            @Parameter(description = "Nuevos datos para la cita", required = true) @Valid @RequestBody CitaRequestDTO citaRequestDTO) {
        CitaResponseDTO citaActualizada = citaService.editarCita(idCita, citaRequestDTO);
        return ResponseEntity.ok(citaActualizada);
    }

    @DeleteMapping("/{idCita}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // O podría ser solo ADMIN si se decide
    @Operation(summary = "Cancelar una cita pendiente", description = "Elimina una cita que aún no ha ocurrido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cita cancelada exitosamente (Sin contenido)"),
            @ApiResponse(responseCode = "400", description = "No se puede cancelar una cita que ya pasó"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada")
    })
    public ResponseEntity<Void> cancelarCita(
            @Parameter(description = "ID de la cita a cancelar", required = true, example = "1") @PathVariable("idCita") Integer idCita) {
        citaService.cancelarCita(idCita);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content es apropiado para DELETE exitoso
    }

    @GetMapping("/{idCita}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Obtener una cita por ID", description = "Recupera los detalles de una cita específica por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cita encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CitaResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada")
    })
    public ResponseEntity<CitaResponseDTO> obtenerCitaPorId(
            @Parameter(description = "ID de la cita a obtener", required = true, example = "1") @PathVariable("idCita") Integer idCita) {
        CitaResponseDTO cita = citaService.obtenerCitaPorId(idCita);
        return ResponseEntity.ok(cita);
    }
}