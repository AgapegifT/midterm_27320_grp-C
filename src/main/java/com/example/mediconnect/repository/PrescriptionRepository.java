package com.example.mediconnect.repository;

import com.example.mediconnect.model.Prescription;
import com.example.mediconnect.model.PrescriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REQUIREMENT 5: One-to-Many Relationship
 * REQUIREMENT 3: Pagination and Sorting
 * REQUIREMENT 7: existsBy() methods
 * 
 * Repository for Prescription entity demonstrating:
 * - One-to-Many relationships (doctor has many prescriptions, patient receives many)
 * - Pagination for large result sets
 * - Existence checking with existsByCode()
 */
@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    
    /**
     * Find prescription by unique prescription code
     */
    Optional<Prescription> findByPrescriptionCode(String prescriptionCode);
    
    /**
     * REQUIREMENT 7: Existence checking
     * Check if a prescription code already exists
     * Prevents duplicate prescription codes
     */
    boolean existsByPrescriptionCode(String prescriptionCode);
    
    /**
     * REQUIREMENT 5: Find all prescriptions for a specific patient
     * ONE-TO-MANY: One patient receives many prescriptions
     * 
     * HOW RELATIONSHIP WORKS:
     * - patient_id is a foreign key in prescriptions table
     * - Points to the patient's profile in profiles table
     * - One patient (profile) can have many prescriptions
     */
    List<Prescription> findByPatientId(Long patientId);
    
    /**
     * REQUIREMENT 3: Paginated version of patient prescriptions
     * Displays patient's prescription history page by page
     */
    Page<Prescription> findByPatientId(Long patientId, Pageable pageable);
    
    /**
     * REQUIREMENT 5: Find all prescriptions written by a doctor
     * ONE-TO-MANY: One doctor writes many prescriptions
     * 
     * HOW RELATIONSHIP WORKS:
     * - doctor_id is a foreign key in prescriptions table
     * - Points to the doctor's profile in profiles table
     * - One doctor (profile) can write many prescriptions
     */
    List<Prescription> findByDoctorId(Long doctorId);
    
    /**
     * REQUIREMENT 3: Paginated doctor prescriptions
     */
    Page<Prescription> findByDoctorId(Long doctorId, Pageable pageable);
    
    /**
     * Find prescriptions dispensed at a specific pharmacy
     */
    List<Prescription> findByPharmacyId(Long pharmacyId);
    
    /**
     * REQUIREMENT 3: Paginated pharmacy prescriptions
     */
    Page<Prescription> findByPharmacyId(Long pharmacyId, Pageable pageable);
    
    /**
     * Find prescriptions by status
     * Used for filtering: active, dispensed, expired, cancelled
     */
    List<Prescription> findByStatus(PrescriptionStatus status);
    
    /**
     * REQUIREMENT 3: Paginated by status
     */
    Page<Prescription> findByStatus(PrescriptionStatus status, Pageable pageable);
    
    /**
     * Find active prescriptions for a patient
     * Useful for pharmacies to verify before dispensing
     */
    List<Prescription> findByPatientIdAndStatus(Long patientId, PrescriptionStatus status);
    
    /**
     * Find all prescriptions created after a certain date
     * Useful for reporting
     */
    List<Prescription> findByIssueDateAfter(LocalDateTime date);
    
    /**
     * Complex query: Patient's active prescriptions that haven't been dispensed
     * REQUIREMENT 3: With pagination
     */
    @Query("SELECT p FROM Prescription p WHERE " +
           "p.patient.id = :patientId AND " +
           "p.status = 'ACTIVE' AND " +
           "p.expiryDate > CURRENT_TIMESTAMP")
    Page<Prescription> findActiveValidPrescriptionsForPatient(
        @Param("patientId") Long patientId,
        Pageable pageable
    );
    
    /**
     * Verification query for pharmacy: Find prescription by code and patient
     * Ensures prescription belongs to the patient trying to fill it
     */
    @Query("SELECT p FROM Prescription p WHERE " +
           "p.prescriptionCode = :code AND " +
           "p.patient.id = :patientId AND " +
           "p.status = 'ACTIVE'")
    Optional<Prescription> verifyPrescription(@Param("code") String code, @Param("patientId") Long patientId);
}

