package auca.ac.rw.mediconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import auca.ac.rw.mediconnect.model.Prescription;
import auca.ac.rw.mediconnect.service.PrescriptionService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/prescriptions")
@CrossOrigin(origins = "*")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @PostMapping
    public ResponseEntity<Prescription> createPrescription(@RequestBody Prescription prescription) {
        Prescription created = prescriptionService.createPrescription(prescription);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/{prescriptionId}/medications/{medicationId}")
    public ResponseEntity<Prescription> addMedicationToPrescription(
            @PathVariable Long prescriptionId,
            @PathVariable Long medicationId) {
        Prescription updated = prescriptionService.addMedicationToPrescription(prescriptionId, medicationId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{prescriptionId}/medications/{medicationId}")
    public ResponseEntity<Prescription> removeMedicationFromPrescription(
            @PathVariable Long prescriptionId,
            @PathVariable Long medicationId) {
        Prescription updated = prescriptionService.removeMedicationFromPrescription(prescriptionId, medicationId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<Page<Prescription>> getAllPrescriptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "issueDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Prescription> prescriptions = prescriptionService.getAllPrescriptions(pageable);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prescription> getPrescriptionById(@PathVariable Long id) {
        Prescription prescription = prescriptionService.getPrescriptionById(id);
        return ResponseEntity.ok(prescription);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<Page<Prescription>> getPrescriptionsByPatient(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("issueDate").descending());
        Page<Prescription> prescriptions = prescriptionService.getPrescriptionsByPatient(patientId, pageable);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/medication/{medicationId}")
    public ResponseEntity<Page<Prescription>> getPrescriptionsByMedication(
            @PathVariable Long medicationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("issueDate").descending());
        Page<Prescription> prescriptions = prescriptionService.getPrescriptionsByMedication(medicationId, pageable);
        return ResponseEntity.ok(prescriptions);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Prescription> updatePrescription(
            @PathVariable Long id,
            @RequestBody Prescription prescriptionDetails) {
        Prescription updated = prescriptionService.updatePrescription(id, prescriptionDetails);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePrescription(@PathVariable Long id) {
        prescriptionService.deletePrescription(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Prescription deleted successfully");
        return ResponseEntity.ok(response);
    }
}