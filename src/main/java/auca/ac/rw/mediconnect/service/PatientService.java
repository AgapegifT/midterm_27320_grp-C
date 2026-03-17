package auca.ac.rw.mediconnect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import auca.ac.rw.mediconnect.model.Location;
import auca.ac.rw.mediconnect.model.Patient;
import auca.ac.rw.mediconnect.repository.PatientRepository;

import java.util.List;

@Service
@Transactional
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private LocationService locationService;

    public Patient createPatient(Patient patient) {
        if (patientRepository.existsByEmail(patient.getEmail())) {
            throw new RuntimeException("Email already exists: " + patient.getEmail());
        }

        if (patientRepository.existsByNationalId(patient.getNationalId())) {
            throw new RuntimeException("National ID already exists: " + patient.getNationalId());
        }

        if (patient.getLocation() != null && patient.getLocation().getId() != null) {
            Location location = locationService.getLocationById(patient.getLocation().getId());
            patient.setLocation(location);
        }

        return patientRepository.save(patient);
    }

    public Page<Patient> getAllPatients(Pageable pageable) {
        return patientRepository.findAll(pageable);
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));
    }

    public Patient getPatientByEmail(String email) {
        return patientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found with email: " + email));
    }

    public Patient getPatientByNationalId(String nationalId) {
        return patientRepository.findByNationalId(nationalId)
                .orElseThrow(() -> new RuntimeException("Patient not found with national ID: " + nationalId));
    }

    public Page<Patient> getPatientsByLocation(Long locationId, Pageable pageable) {
        return patientRepository.findByLocationId(locationId, pageable);
    }

    public List<Patient> getPatientsByProvinceCode(String provinceCode) {
        return patientRepository.findAllByProvinceCode(provinceCode);
    }

    public List<Patient> getPatientsByProvinceName(String provinceName) {
        return patientRepository.findAllByProvinceName(provinceName);
    }

    public Patient updatePatient(Long id, Patient patientDetails) {
        Patient patient = getPatientById(id);

        if (patientDetails.getEmail() != null && !patientDetails.getEmail().equals(patient.getEmail()) &&
                patientRepository.existsByEmail(patientDetails.getEmail())) {
            throw new RuntimeException("Email already exists: " + patientDetails.getEmail());
        }

        patient.setFirstName(patientDetails.getFirstName());
        patient.setLastName(patientDetails.getLastName());
        patient.setEmail(patientDetails.getEmail());
        patient.setPhoneNumber(patientDetails.getPhoneNumber());
        patient.setDateOfBirth(patientDetails.getDateOfBirth());

        if (patientDetails.getLocation() != null && patientDetails.getLocation().getId() != null) {
            Location location = locationService.getLocationById(patientDetails.getLocation().getId());
            patient.setLocation(location);
        }

        return patientRepository.save(patient);
    }

    public void deletePatient(Long id) {
        Patient patient = getPatientById(id);
        patientRepository.delete(patient);
    }

    public boolean existsByEmail(String email) {
        return patientRepository.existsByEmail(email);
    }
}