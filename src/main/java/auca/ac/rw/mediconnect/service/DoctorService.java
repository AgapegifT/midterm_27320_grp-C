package auca.ac.rw.mediconnect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import auca.ac.rw.mediconnect.model.Doctor;
import auca.ac.rw.mediconnect.model.Location;
import auca.ac.rw.mediconnect.repository.DoctorRepository;

import java.util.List;

@Service
@Transactional
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private LocationService locationService;

    public Doctor createDoctor(Doctor doctor) {
        if (doctorRepository.existsByEmail(doctor.getEmail())) {
            throw new RuntimeException("Email already exists: " + doctor.getEmail());
        }

        if (doctorRepository.existsByLicenseNumber(doctor.getLicenseNumber())) {
            throw new RuntimeException("License number already exists: " + doctor.getLicenseNumber());
        }

        if (doctor.getLocation() != null && doctor.getLocation().getId() != null) {
            Location location = locationService.getLocationById(doctor.getLocation().getId());
            doctor.setLocation(location);
        }

        return doctorRepository.save(doctor);
    }

    public Page<Doctor> getAllDoctors(Pageable pageable) {
        return doctorRepository.findAll(pageable);
    }

    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));
    }

    public Doctor getDoctorByLicenseNumber(String licenseNumber) {
        return doctorRepository.findByLicenseNumber(licenseNumber)
                .orElseThrow(() -> new RuntimeException("Doctor not found with license number: " + licenseNumber));
    }

    public Page<Doctor> getDoctorsBySpecialization(String specialization, Pageable pageable) {
        return doctorRepository.findBySpecializationIgnoreCase(specialization, pageable);
    }

    public Page<Doctor> getDoctorsByLocation(Long locationId, Pageable pageable) {
        return doctorRepository.findByLocationId(locationId, pageable);
    }

    public List<Doctor> getDoctorsByProvinceCode(String provinceCode) {
        return doctorRepository.findAllByProvinceCode(provinceCode);
    }

    public List<Doctor> getDoctorsByProvinceName(String provinceName) {
        return doctorRepository.findAllByProvinceName(provinceName);
    }

    public Doctor updateDoctor(Long id, Doctor doctorDetails) {
        Doctor doctor = getDoctorById(id);

        doctor.setFirstName(doctorDetails.getFirstName());
        doctor.setLastName(doctorDetails.getLastName());
        doctor.setEmail(doctorDetails.getEmail());
        doctor.setPhoneNumber(doctorDetails.getPhoneNumber());
        doctor.setSpecialization(doctorDetails.getSpecialization());

        if (doctorDetails.getLocation() != null && doctorDetails.getLocation().getId() != null) {
            Location location = locationService.getLocationById(doctorDetails.getLocation().getId());
            doctor.setLocation(location);
        }

        return doctorRepository.save(doctor);
    }

    public void deleteDoctor(Long id) {
        Doctor doctor = getDoctorById(id);
        doctorRepository.delete(doctor);
    }

    public boolean existsByLicenseNumber(String licenseNumber) {
        return doctorRepository.existsByLicenseNumber(licenseNumber);
    }
}