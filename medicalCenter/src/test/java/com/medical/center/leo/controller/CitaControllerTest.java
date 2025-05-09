package com.medical.center.leo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medical.center.leo.dto.CitaRequestDTO;
import com.medical.center.leo.dto.CitaResponseDTO;
import com.medical.center.leo.service.CitaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

/**
 * Clase de prueba para el controlador CitaController.
 * Utiliza MockMvc para simular peticiones HTTP y verificar respuestas.
 */
@WebMvcTest(CitaController.class)
class CitaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CitaService citaService;

    private ObjectMapper objectMapper;

    private CitaRequestDTO citaRequestDTO;
    private CitaResponseDTO citaResponseDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Para manejar LocalDate y LocalDateTime

        LocalDateTime horario = LocalDateTime.of(2025, 10, 20, 10, 0, 0);
        citaRequestDTO = new CitaRequestDTO(1, 1, horario, "Paciente Test Controller");

        citaResponseDTO = new CitaResponseDTO(
                1, 101, 1, "Dr. Juan Perez", "Cardiología",
                horario, "Paciente Test Controller");
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" }) // Simula un usuario autenticado
    void crearCita_cuandoDatosValidos_deberiaRetornarCreatedYResponseDTO() throws Exception {
        when(citaService.crearCita(any(CitaRequestDTO.class))).thenReturn(citaResponseDTO);

        mockMvc.perform(post("/api/citas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(citaRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idCita", is(citaResponseDTO.idCita())))
                .andExpect(jsonPath("$.nombrePaciente", is(citaResponseDTO.nombrePaciente())));
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    void crearCita_cuandoDatosInvalidos_deberiaRetornarBadRequest() throws Exception {
        CitaRequestDTO invalidRequest = new CitaRequestDTO(1, 1, LocalDateTime.now().plusDays(1), "");

        mockMvc.perform(post("/api/citas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    void consultarCitas_deberiaRetornarOkYCitas() throws Exception {
        LocalDate fecha = LocalDate.of(2025, 10, 20);
        when(citaService.consultarCitas(eq(fecha), any(), any()))
                .thenReturn(Collections.singletonList(citaResponseDTO));

        mockMvc.perform(get("/api/citas")
                .param("fecha", fecha.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombrePaciente", is(citaResponseDTO.nombrePaciente())));
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    void editarCita_cuandoDatosValidos_deberiaRetornarOkYResponseDTO() throws Exception {
        Integer citaId = 1;
        when(citaService.editarCita(eq(citaId), any(CitaRequestDTO.class))).thenReturn(citaResponseDTO);

        mockMvc.perform(put("/api/citas/{id}", citaId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(citaRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCita", is(citaResponseDTO.idCita())))
                .andExpect(jsonPath("$.nombrePaciente", is(citaResponseDTO.nombrePaciente())));
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    void cancelarCita_cuandoCitaExiste_deberiaRetornarNoContent() throws Exception {
        Integer citaId = 1;
        doNothing().when(citaService).cancelarCita(eq(citaId));

        mockMvc.perform(delete("/api/citas/{id}", citaId)
                .with(csrf()))
                .andExpect(status().isNoContent());
    }
}