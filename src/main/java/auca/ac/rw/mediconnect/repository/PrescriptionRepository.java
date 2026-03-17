package auca.ac.rw.mediconnect.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import auca.ac.rw.mediconnect.model.Prescription;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    Optional<Prescription> findByPrescriptionNumber(String prescriptionNumber);

    boolean existsByPrescriptionNumber(String prescriptionNumber);

    Page<Prescription> findByPatientId(Long patientId, Pageable pageable);

    Page<Prescription> findByDoctorId(Long doctorId, Pageable pageable);

    @Query("SELECT p FROM Prescription p JOIN p.medications m WHERE m.id = :medicationId")
    Page<Prescription> findByMedicationId(@Param("medicationId") Long medicationId, Pageable pageable);

    @Query("SELECT p FROM Prescription p WHERE p.expiryDate <= :expiryDate AND p.expiryDate >= CURRENT_DATE")
    List<Prescription> findExpiringSoonPrescriptions(@Param("expiryDate") LocalDate expiryDate);
}