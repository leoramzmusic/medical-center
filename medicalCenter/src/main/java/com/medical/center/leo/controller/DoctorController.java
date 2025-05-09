package com.medical.center.leo.controller;

import com.medical.center.leo.entity.Doctor;
import com.medical.center.leo.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctores")
@Tag(name = "Doctores", description = "API para la gestión de doctores")
public class DoctorController {

    private final DoctorService doctorService; // Inyectar

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo doctor")
    public ResponseEntity<Doctor> registrarDoctor(@RequestBody Doctor doctor) { // Usar DTO si es necesario
        return new ResponseEntity<>(doctorService.registrarDoctor(doctor), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar todos los doctores")
    public ResponseEntity<List<Doctor>> listarDoctores() {
        return ResponseEntity.ok(doctorService.listarDoctores());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un doctor existente")
    public ResponseEntity<Doctor> actualizarDoctor(@PathVariable Integer id, @RequestBody Doctor doctor) {
        return ResponseEntity.ok(doctorService.actualizarDoctor(id, doctor));
    }

    // Eliminar Doctores (considerando el impacto en Citas existentes)
    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> eliminarDoctor(@PathVariable Integer id) {
    // doctorService.eliminarDoctor(id);
    // return ResponseEntity.noContent().build();
    // }
}