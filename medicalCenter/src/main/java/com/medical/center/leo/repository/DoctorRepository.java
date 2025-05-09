package com.medical.center.leo.repository;

import com.medical.center.leo.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {

    // Metodo para buscar doctores por especialidad
    List<Doctor> findByEspecialidad(String especialidad);

    // Metodo para buscar doctores por apellido paterno
    List<Doctor> findByApellido_paternoIgnoreCase(String apellidoPaterno);

    // Metodo para buscar un doctor por nombre completo
    Optional<Doctor> findByNombreAndApellido_paternoAndApellido_materno(
            String nombre, String apellidoPaterno, String apellidoMaterno);

    // Verificar si existe un doctor con una combinacion especifica
    boolean existsByNombreAndApellido_paternoAndApellido_maternoAndEspecialidad(
            String nombre, String apellidoPaterno, String apellidoMaterno, String especialidad);
}