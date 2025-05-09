package com.medical.center.leo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "Consultorios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Consultorio {

    // Atributos de la clase Consultorio
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_consultorio;

    @Column(nullable = false, unique = true)
    private Integer numero_consultorio;

    @Column(nullable = false)
    private Integer piso;
}