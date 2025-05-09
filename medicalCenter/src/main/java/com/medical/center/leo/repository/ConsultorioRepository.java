package com.medical.center.leo.repository;

import com.medical.center.leo.entity.Consultorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsultorioRepository extends JpaRepository<Consultorio, Integer> {

    // Metodo para buscar un consultorio por su numero, que se espera sea unico
    Optional<Consultorio> findByNumero_consultorio(Integer numeroConsultorio);

    // Metodo para verificar si existe un consultorio con un número especifico
    boolean existsByNumero_consultorio(Integer numeroConsultorio);

    // List<Consultorio> findByPiso(Integer piso);
}