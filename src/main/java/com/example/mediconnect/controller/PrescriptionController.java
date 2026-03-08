package com.example.mediconnect.controller;

import com.example.mediconnect.model.Prescription;
import com.example.mediconnect.model.Role;
import com.example.mediconnect.repository.PrescriptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REQUIREMENT 5: One-to-Many Relationship
 * 
 * Database Query Strategy:
 * - No loops in database code
 * - All queries are single-line repository calls
 * - Let database handle the work (not Java code)
 */
@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {
    // Inject the PrescriptionRepository for database operations
    private final PrescriptionRepository prescriptionRepository;

    // Constructor to initialize the repository
    public PrescriptionController(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    /**
     * Create a new prescription (Doctor only)
     * 
     * API USAGE:
     * POST /api/prescriptions
     * Body: {
     *   "prescriptionCode": "RX-2024-0001",
     *   "doctor": {"id": 1},
     *   "patient": {"id": 2},
     *   "status": "ACTIVE"
     * }
     * 
     * Database Operation:
     * INSERT INTO prescriptions (...) VALUES (...);
     */
    @PostMapping
    public ResponseEntity<?> createPrescription(
            // Request body containing prescription data
            @RequestBody Prescription prescription,
            // Role header to verify doctor authorization
            @RequestHeader(value = "X-Role", required = false) String role) {
        
        // Check if user has DOCTOR role
        if (!isValidRole(role, Role.DOCTOR)) {
            return ResponseEntity.status(403)
                    .body("Only doctors can create prescriptions");
        }
        
        // REQUIREMENT 7: Check if prescription code already exists
        // Single database query: SELECT COUNT(*) > 0 FROM prescriptions WHERE prescription_code = ?
        if (prescriptionRepository.existsByPrescriptionCode(prescription.getPrescriptionCode())) {
            return ResponseEntity.badRequest()
                    .body("Prescription code already exists: " + prescription.getPrescriptionCode());
        }
        
        // Execute ONE database query: Save the prescription
        Prescription saved = prescriptionRepository.save(prescription);
        
        // Return the saved prescription with HTTP 201 Created
        return ResponseEntity.status(201).body(saved);
    }

    /**
     * Get a single prescription by ID
     * 
     * API USAGE:
     * GET /api/prescriptions/5
     * 
     * Database Query Generated:
     * SELECT * FROM prescriptions WHERE id = 5;
     * 
     * This is a DIRECT lookup by primary key - O(1) complexity, very fast!
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPrescriptionById(
            // The prescription ID (extracted from URL)
            @PathVariable Long id) {
        
        // Execute ONE database query: Get prescription by ID directly (no loop)
        Optional<Prescription> prescription = prescriptionRepository.findById(id);
        
        // Return prescription if found, or 404 if not found
        return prescription.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * REQUIREMENT 5: Get all prescriptions for a specific PATIENT
     * 
     * API USAGE:
     * GET /api/prescriptions/patient/2
     * 
     * Database Query Generated:
     * SELECT * FROM prescriptions WHERE patient_id = 2;
     * 
     * This demonstrates One-to-Many: One patient has many prescriptions
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPatientPrescriptions(
            // The patient ID (extracted from URL)
            @PathVariable Long patientId,
            // Pagination parameters for REQUIREMENT 3
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Create pagination object
        Pageable pageable = PageRequest.of(page, size);
        
        // Execute ONE database query: Get all prescriptions for patient (auto-paginated)
        // NO LOOP - Database returns only 10 records
        Page<Prescription> prescriptions = prescriptionRepository.findByPatientId(patientId, pageable);
        
        // Return the paginated results
        return ResponseEntity.ok(prescriptions);
    }

    /**
     * Get all prescriptions written by a specific DOCTOR
     * 
     * API USAGE:
     * GET /api/prescriptions/doctor/1
     * 
     * Database Query Generated:
     * SELECT * FROM prescriptions WHERE doctor_id = 1;
     * 
     * This demonstrates One-to-Many: One doctor writes many prescriptions
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getDoctorPrescriptions(
            // The doctor ID (extracted from URL)
            @PathVariable Long doctorId,
            // Pagination parameters for REQUIREMENT 3
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Create pagination object
        Pageable pageable = PageRequest.of(page, size);
        
        // Execute ONE database query: Get all prescriptions written by doctor
        // NO LOOP - Database returns only 10 records
        Page<Prescription> prescriptions = prescriptionRepository.findByDoctorId(doctorId, pageable);
        
        // Return the paginated results
        return ResponseEntity.ok(prescriptions);
    }

    /**
     * Get all prescriptions dispensed at a specific PHARMACY
     * 
     * API USAGE:
     * GET /api/prescriptions/pharmacy/3
     * 
     * Database Query Generated:
     * SELECT * FROM prescriptions WHERE pharmacy_id = 3;
     */
    @GetMapping("/pharmacy/{pharmacyId}")
    public ResponseEntity<?> getPharmacyPrescriptions(
            // The pharmacy ID (extracted from URL)
            @PathVariable Long pharmacyId,
            // Pagination parameters for REQUIREMENT 3
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Create pagination object
        Pageable pageable = PageRequest.of(page, size);
        
        // Execute ONE database query: Get all prescriptions dispensed at pharmacy
        // NO LOOP - Database returns only 10 records
        Page<Prescription> prescriptions = prescriptionRepository.findByPharmacyId(pharmacyId, pageable);
        
        // Return the paginated results
        return ResponseEntity.ok(prescriptions);
    }

    /**
     * Update prescription status
     * 
     * API USAGE:
     * PUT /api/prescriptions/5/status
     * Body: {"status": "DISPENSED"}
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updatePrescriptionStatus(
            // The prescription ID to update
            @PathVariable Long id,
            // Request body with new status
            @RequestBody Prescription updates) {
        
        // Execute ONE database query: Get prescription by ID
        Optional<Prescription> existing = prescriptionRepository.findById(id);
        
        // If prescription doesn't exist, return 404
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Get the prescription from Optional
        Prescription prescription = existing.get();
        
        // Update the status
        if (updates.getStatus() != null) {
            prescription.setStatus(updates.getStatus());
        }
        
        // Execute ONE database query: Save the updated prescription
        Prescription updated = prescriptionRepository.save(prescription);
        
        // Return the updated prescription
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a prescription by ID
     * 
     * API USAGE:
     * DELETE /api/prescriptions/5
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePrescription(
            // The prescription ID to delete
            @PathVariable Long id) {
        
        // Execute ONE database query: Check if prescription exists using ID
        if (!prescriptionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        // Execute ONE database query: Delete the prescription
        prescriptionRepository.deleteById(id);
        
        // Return 204 No Content (successful deletion, no body)
        return ResponseEntity.noContent().build();
    }

    /**
     * Helper method to validate user role
     * 
     * @param roleHeader The role from request header
     * @param requiredRole The required role to access endpoint
     * @return true if user has the required role
     */
    private boolean isValidRole(String roleHeader, Role requiredRole) {
        // Check if role header exists and matches the required role (case-insensitive)
        return roleHeader != null && roleHeader.equalsIgnoreCase(requiredRole.name());
    }
}
