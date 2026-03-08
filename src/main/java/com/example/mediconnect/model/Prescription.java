package com.example.mediconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * REQUIREMENT 5: One-to-Many Relationship
 * REQUIREMENT 1: Entity with proper relationships
 * 
 * RELATIONSHIP MAPPING:
 * 
 * 1. Doctor (Profile) → Prescriptions: ONE-TO-MANY
 *    - One doctor creates/writes many prescriptions
 *    - Foreign key: doctor_id in prescriptions table points to profiles.id
 *    - A doctor OWNS the relationship
 * 
 * 2. Patient (Profile) → Prescriptions: ONE-TO-MANY
 *    - One patient receives many prescriptions
 *    - Foreign key: patient_id in prescriptions table points to profiles.id
 *    - A patient OWNS the relationship
 * 
 * 3. Pharmacy → Prescriptions: ONE-TO-MANY
 *    - One pharmacy dispenses many prescriptions
 *    - Foreign key: pharmacy_id in prescriptions table points to pharmacies.id
 *    - A prescription can be filled at ONE pharmacy
 * 
 * FOREIGN KEYS EXPLAINED:
 * prescriptions table columns:
 * - id (Primary Key)
 * - prescription_code (Unique identifier)
 * - doctor_id (FK to profiles - the doctor who prescribed)
 * - patient_id (FK to profiles - the patient receiving medicine)
 * - pharmacy_id (FK to pharmacies - where it was/will be dispensed)
 * - issue_date, status, etc.
 * 
 * EXAMPLE DATA:
 * A prescription record:
 * {
 *   id: 1,
 *   prescription_code: "RX-2024-0001",
 *   doctor_id: 5 (Dr. Jane Smith from profiles table),
 *   patient_id: 12 (John Doe from profiles table),
 *   pharmacy_id: 2 (City Pharmacy),
 *   status: "active"
 * }
 */
@Entity
@Table(name = "prescriptions")
public class Prescription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique prescription code for verification
     * e.g., "RX-2024-0001"
     */
    @Column(unique = true, nullable = false)
    private String prescriptionCode;

    /**
     * ONE-TO-MANY (Inverse): Many prescriptions → One doctor
     * This prescription was written by ONE doctor
     * Foreign key: doctor_id
     * 
     * The doctor is the "one" side - they write many prescriptions
     * This is the "many" side - each prescription belongs to one doctor
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Profile doctor;

    /**
     * ONE-TO-MANY (Inverse): Many prescriptions → One patient
     * This prescription is for ONE patient
     * Foreign key: patient_id
     * 
     * The patient is the "one" side - they receive many prescriptions
     * This is the "many" side - each prescription is for one patient
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Profile patient;

    /**
     * ONE-TO-MANY (Inverse): Many prescriptions → One pharmacy
     * This prescription can be dispensed at ONE pharmacy (though patient might try others)
     * Foreign key: pharmacy_id
     * 
     * Nullable because the prescription might not yet be assigned to a pharmacy
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id")
    private Pharmacy pharmacy;

    // Prescription Details
    private LocalDateTime issueDate = LocalDateTime.now();
    private LocalDateTime expiryDate;
    private LocalDateTime dispensedDate;

    /**
     * Status tracking
     * - "active": Patient hasn't filled it yet
     * - "dispensed": Pharmacy has filled it
     * - "expired": Prescription is too old
     * - "cancelled": Doctor or patient cancelled it
     */
    @Enumerated(EnumType.STRING)
    private PrescriptionStatus status = PrescriptionStatus.ACTIVE;

    /**
     * Clinical notes from the doctor
     */
    @Column(length = 2000)
    private String notes;

    /**
     * Verification code for audit trail
     */
    private String verificationCode;

    // Constructors
    public Prescription() {
        this.issueDate = LocalDateTime.now();
    }

    public Prescription(Profile doctor, Profile patient, String prescriptionCode) {
        this.doctor = doctor;
        this.patient = patient;
        this.prescriptionCode = prescriptionCode;
        this.issueDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPrescriptionCode() {
        return prescriptionCode;
    }

    public void setPrescriptionCode(String prescriptionCode) {
        this.prescriptionCode = prescriptionCode;
    }

    public Profile getDoctor() {
        return doctor;
    }

    public void setDoctor(Profile doctor) {
        this.doctor = doctor;
    }

    public Profile getPatient() {
        return patient;
    }

    public void setPatient(Profile patient) {
        this.patient = patient;
    }

    public Pharmacy getPharmacy() {
        return pharmacy;
    }

    public void setPharmacy(Pharmacy pharmacy) {
        this.pharmacy = pharmacy;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public LocalDateTime getDispensedDate() {
        return dispensedDate;
    }

    public void setDispensedDate(LocalDateTime dispensedDate) {
        this.dispensedDate = dispensedDate;
    }

    public PrescriptionStatus getStatus() {
        return status;
    }

    public void setStatus(PrescriptionStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    @Override
    public String toString() {
        return "Prescription{" +
                "id=" + id +
                ", code='" + prescriptionCode + '\'' +
                ", doctor=" + (doctor != null ? doctor.getFullName() : "null") +
                ", patient=" + (patient != null ? patient.getFullName() : "null") +
                ", status=" + status +
                '}';
    }
}
