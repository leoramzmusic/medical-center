package com.medical.center.leo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "Doctores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    // Atributos de la clase Doctor
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_medico;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido_paterno;

    @Column(nullable = false, length = 100)
    private String apellido_materno;

    @Column(nullable = false, length = 100)
    private String especialidad;
}