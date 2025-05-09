package com.medical.center.leo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Citas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Integer id_cita; // Clave primaria

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultorio_id", nullable = false)
    private Consultorio consultorio; // Lombok generará getConsultorio() y setConsultorio()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false) // La columna en la BD se llama "medico_id"
    private Doctor doctor; // Lombok generará getDoctor() y setDoctor()

    @Column(name = "horario_consulta", nullable = false)
    private LocalDateTime horario_consulta; // Horario de la cita

    @Column(name = "nombre_paciente", nullable = false, length = 100)
    private String nombre_paciente; // Nombre del paciente

    // public Cita(Integer id_cita, Consultorio consultorio, Doctor doctor,
    // LocalDateTime horario_consulta, String nombre_paciente)
}