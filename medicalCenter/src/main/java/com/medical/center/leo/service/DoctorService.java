package com.medical.center.leo.service;

import com.medical.center.leo.entity.Doctor;
import com.medical.center.leo.exception.BusinessRuleException;
import com.medical.center.leo.exception.ResourceNotFoundException;
import com.medical.center.leo.repository.CitaRepository;
import com.medical.center.leo.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final CitaRepository citaRepository;

    public DoctorService(DoctorRepository doctorRepository, CitaRepository citaRepository) {
        this.doctorRepository = doctorRepository;
        this.citaRepository = citaRepository;
    }

    @Transactional
    public Doctor registrarDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @Transactional(readOnly = true)
    public List<Doctor> listarDoctores() {
        return doctorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Doctor obtenerDoctorPorId(Integer idMedico) {
        return doctorRepository.findById(idMedico)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor no encontrado con ID: " + idMedico));
    }

    @Transactional
    public Doctor actualizarDoctor(Integer idMedico, Doctor doctorActualizado) {
        Doctor doctorExistente = doctorRepository.findById(idMedico)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor no encontrado con ID: " + idMedico + " para actualizar."));

        // Actualizar los campos del doctor existente
        doctorExistente.setNombre(doctorActualizado.getNombre());
        doctorExistente.setApellido_paterno(doctorActualizado.getApellido_paterno());
        doctorExistente.setApellido_materno(doctorActualizado.getApellido_materno());
        doctorExistente.setEspecialidad(doctorActualizado.getEspecialidad());

        return doctorRepository.save(doctorExistente);
    }

    @Transactional
    public void eliminarDoctor(Integer idMedico) {
        Doctor doctor = doctorRepository.findById(idMedico)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor no encontrado con ID: " + idMedico + " para eliminar."));

        // Regla de negocio: No se puede eliminar un doctor si tiene citas asociadas.
        if (citaRepository.medicoTieneCitas(idMedico)) {
            throw new BusinessRuleException("No se puede eliminar el doctor con ID: " + idMedico +
                    " porque tiene citas médicas asociadas. Cancele o reasigne las citas primero.");
        }

        doctorRepository.delete(doctor);
    }
}