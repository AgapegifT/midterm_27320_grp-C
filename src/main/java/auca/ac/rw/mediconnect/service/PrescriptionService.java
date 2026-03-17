package auca.ac.rw.mediconnect.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import auca.ac.rw.mediconnect.model.Doctor;
import auca.ac.rw.mediconnect.model.Medication;
import auca.ac.rw.mediconnect.model.Patient;
import auca.ac.rw.mediconnect.model.Prescription;
import auca.ac.rw.mediconnect.repository.PrescriptionRepository;

@Service
@Transactional
public class PrescriptionService {
    
    @Autowired
    private PrescriptionRepository prescriptionRepository;
    
    @Autowired
    private PatientService patientService;
    
    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private MedicationService medicationService;
    
    public Prescription createPrescription(Prescription prescription) {
        if (prescription.getPrescriptionNumber() != null && 
            prescriptionRepository.existsByPrescriptionNumber(prescription.getPrescriptionNumber())) {
            throw new RuntimeException("Prescription number already exists: " + prescription.getPrescriptionNumber());
        }
        
        if (prescription.getPatient() != null && prescription.getPatient().getId() != null) {
            Patient patient = patientService.getPatientById(prescription.getPatient().getId());
            prescription.setPatient(patient);
        }
        
        if (prescription.getDoctor() != null && prescription.getDoctor().getId() != null) {
            Doctor doctor = doctorService.getDoctorById(prescription.getDoctor().getId());
            prescription.setDoctor(doctor);
        }
        
        return prescriptionRepository.save(prescription);
    }
    
    public Prescription addMedicationToPrescription(Long prescriptionId, Long medicationId) {
        Prescription prescription = getPrescriptionById(prescriptionId);
        Medication medication = medicationService.getMedicationById(medicationId);
        
        if (prescription.getMedications().contains(medication)) {
            throw new RuntimeException("Medication already in prescription");
        }
        
        prescription.addMedication(medication);
        return prescriptionRepository.save(prescription);
    }
    
    public Prescription removeMedicationFromPrescription(Long prescriptionId, Long medicationId) {
        Prescription prescription = getPrescriptionById(prescriptionId);
        Medication medication = medicationService.getMedicationById(medicationId);
        
        prescription.removeMedication(medication);
        return prescriptionRepository.save(prescription);
    }
    
    public Page<Prescription> getAllPrescriptions(Pageable pageable) {
        return prescriptionRepository.findAll(pageable);
    }
    
    public Prescription getPrescriptionById(Long id) {
        return prescriptionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Prescription not found with id: " + id));
    }
    
    public Prescription getPrescriptionByNumber(String prescriptionNumber) {
        return prescriptionRepository.findByPrescriptionNumber(prescriptionNumber)
            .orElseThrow(() -> new RuntimeException("Prescription not found with number: " + prescriptionNumber));
    }
    
    public Page<Prescription> getPrescriptionsByPatient(Long patientId, Pageable pageable) {
        return prescriptionRepository.findByPatientId(patientId, pageable);
    }
    
    public Page<Prescription> getPrescriptionsByDoctor(Long doctorId, Pageable pageable) {
        return prescriptionRepository.findByDoctorId(doctorId, pageable);
    }
    
    public Page<Prescription> getPrescriptionsByMedication(Long medicationId, Pageable pageable) {
        return prescriptionRepository.findByMedicationId(medicationId, pageable);
    }
    
    public Prescription updatePrescription(Long id, Prescription prescriptionDetails) {
        Prescription prescription = getPrescriptionById(id);
        
        prescription.setIssueDate(prescriptionDetails.getIssueDate());
        prescription.setExpiryDate(prescriptionDetails.getExpiryDate());
        prescription.setDiagnosis(prescriptionDetails.getDiagnosis());
        prescription.setNotes(prescriptionDetails.getNotes());
        
        return prescriptionRepository.save(prescription);
    }
    
    public void deletePrescription(Long id) {
        Prescription prescription = getPrescriptionById(id);
        prescriptionRepository.delete(prescription);
    }

    public Prescription verifyPrescription(Long id, String verifiedBy) {
        Prescription prescription = getPrescriptionById(id);
        prescription.setNotes((prescription.getNotes() != null ? prescription.getNotes() + "\n" : "") + "Verified by: " + verifiedBy + " at " + java.time.LocalDateTime.now());
        return prescriptionRepository.save(prescription);
    }

    public List<Prescription> getExpiringSoonPrescriptions() {
        java.time.LocalDate expiryThreshold = java.time.LocalDate.now().plusDays(7);
        return prescriptionRepository.findExpiringSoonPrescriptions(expiryThreshold);
    }
}