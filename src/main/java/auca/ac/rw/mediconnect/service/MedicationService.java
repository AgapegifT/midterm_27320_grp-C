package auca.ac.rw.mediconnect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import auca.ac.rw.mediconnect.model.Medication;
import auca.ac.rw.mediconnect.repository.MedicationRepository;

@Service
@Transactional
public class MedicationService {

    @Autowired
    private MedicationRepository medicationRepository;

    public Medication createMedication(Medication medication) {
        if (medicationRepository.existsByCode(medication.getCode())) {
            throw new RuntimeException("Medication code already exists: " + medication.getCode());
        }

        return medicationRepository.save(medication);
    }

    public Page<Medication> getAllMedications(Pageable pageable) {
        return medicationRepository.findAll(pageable);
    }

    public Medication getMedicationById(Long id) {
        return medicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medication not found with id: " + id));
    }

    public Medication getMedicationByCode(String code) {
        return medicationRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Medication not found with code: " + code));
    }

    public Page<Medication> searchMedications(String searchTerm, Pageable pageable) {
        return medicationRepository.findByNameContainingIgnoreCase(searchTerm, pageable);
    }

    public Page<Medication> getMedicationsByDosageForm(String dosageForm, Pageable pageable) {
        return medicationRepository.findByDosageFormIgnoreCase(dosageForm, pageable);
    }

    public Medication updateMedication(Long id, Medication medicationDetails) {
        Medication medication = getMedicationById(id);

        medication.setName(medicationDetails.getName());
        medication.setDescription(medicationDetails.getDescription());
        medication.setDosageForm(medicationDetails.getDosageForm());
        medication.setStrength(medicationDetails.getStrength());
        medication.setManufacturer(medicationDetails.getManufacturer());

        return medicationRepository.save(medication);
    }

    public void deleteMedication(Long id) {
        Medication medication = getMedicationById(id);
        medicationRepository.delete(medication);
    }

    public boolean existsByCode(String code) {
        return medicationRepository.existsByCode(code);
    }
}