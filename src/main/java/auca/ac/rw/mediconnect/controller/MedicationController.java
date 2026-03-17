package auca.ac.rw.mediconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import auca.ac.rw.mediconnect.model.Medication;
import auca.ac.rw.mediconnect.service.MedicationService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/medications")
@CrossOrigin(origins = "*")
public class MedicationController {

    @Autowired
    private MedicationService medicationService;

    @PostMapping
    public ResponseEntity<Medication> createMedication(@RequestBody Medication medication) {
        Medication created = medicationService.createMedication(medication);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<Medication>> getAllMedications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(medicationService.getAllMedications(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medication> getMedicationById(@PathVariable Long id) {
        return ResponseEntity.ok(medicationService.getMedicationById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Medication> getMedicationByCode(@PathVariable String code) {
        return ResponseEntity.ok(medicationService.getMedicationByCode(code));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Medication>> searchMedications(
            @RequestParam String term,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(medicationService.searchMedications(term, pageable));
    }

    @GetMapping("/exists/code/{code}")
    public ResponseEntity<Map<String, Boolean>> existsByCode(@PathVariable String code) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", medicationService.existsByCode(code));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medication> updateMedication(
            @PathVariable Long id,
            @RequestBody Medication medicationDetails) {
        return ResponseEntity.ok(medicationService.updateMedication(id, medicationDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMedication(@PathVariable Long id) {
        medicationService.deleteMedication(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Medication deleted successfully");
        return ResponseEntity.ok(response);
    }
}