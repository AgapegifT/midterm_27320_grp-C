package auca.ac.rw.mediconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import auca.ac.rw.mediconnect.model.Patient;
import auca.ac.rw.mediconnect.service.PatientService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/patients")
@CrossOrigin(origins = "*")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @PostMapping
    public ResponseEntity<Patient> createPatient(@RequestBody Patient patient) {
        Patient created = patientService.createPatient(patient);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<Patient>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Patient> patients = patientService.getAllPatients(pageable);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        Patient patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patient);
    }

    @GetMapping("/province/code/{provinceCode}")
    public ResponseEntity<List<Patient>> getPatientsByProvinceCode(@PathVariable String provinceCode) {
        List<Patient> patients = patientService.getPatientsByProvinceCode(provinceCode);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/province/name/{provinceName}")
    public ResponseEntity<List<Patient>> getPatientsByProvinceName(@PathVariable String provinceName) {
        List<Patient> patients = patientService.getPatientsByProvinceName(provinceName);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Map<String, Boolean>> checkEmailExists(@PathVariable String email) {
        boolean exists = patientService.existsByEmail(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(
            @PathVariable Long id,
            @RequestBody Patient patientDetails) {
        Patient updated = patientService.updatePatient(id, patientDetails);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Patient deleted successfully");
        return ResponseEntity.ok(response);
    }
}