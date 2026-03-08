package com.example.mediconnect.controller;

import com.example.mediconnect.model.Medicine;
import com.example.mediconnect.repository.MedicineRepository;
import com.example.mediconnect.repository.PharmacyMedicineRepository;
import com.example.mediconnect.model.PharmacyMedicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REQUIREMENT 3: Pagination and Sorting
 * REQUIREMENT 7: existsBy() Method for duplicate checking
 * 
 * This controller demonstrates clean database queries:
 * - Single database calls (no loops)
 * - Efficient pagination
 * - Clear comments for lectee understanding
 */
@RestController
@RequestMapping("/api/medicines")
public class MedicineController {
    // Inject the MedicineRepository for database operations
    private final MedicineRepository medicineRepository;
    private final PharmacyMedicineRepository pharmacyMedicineRepository;

    // Constructor to initialize the repositories
    public MedicineController(MedicineRepository medicineRepository,
                              PharmacyMedicineRepository pharmacyMedicineRepository) {
        this.medicineRepository = medicineRepository;
        this.pharmacyMedicineRepository = pharmacyMedicineRepository;
    }

    /**
     * REQUIREMENT 3: Get all medicines with PAGINATION and SORTING
     * 
     * API USAGE:
     * GET /api/medicines?page=0&size=10&sort=name,asc
     * 
     * Database Query Generated:
     * SELECT * FROM medicines LIMIT 10 OFFSET 0 ORDER BY name ASC;
     * 
     * Benefits:
     * - Loads only 10 medicines instead of all (memory efficient)
     * - Sorted alphabetically by name
     * - Database does sorting (faster than Java code)
     */
    @GetMapping
    public Page<Medicine> getAllMedicines(
            // Parameter 1: Which page to return (0 = first page)
            @RequestParam(defaultValue = "0") int page,
            // Parameter 2: How many medicines per page
            @RequestParam(defaultValue = "10") int size,
            // Parameter 3: Field to sort by and direction
            @RequestParam(defaultValue = "name,asc") String[] sort) {
        
        // Parse the sort parameter (format: "fieldName,direction")
        Sort.Direction direction = Sort.Direction.ASC;
        String sortBy = "name";
        
        // Extract sort field and direction
        if (sort.length > 0 && sort[0].contains(",")) {
            // Split "name,asc" into ["name", "asc"]
            String[] parts = sort[0].split(",");
            sortBy = parts[0];
            // Convert string "asc" or "desc" to Sort.Direction enum
            direction = Sort.Direction.fromString(parts[1].toUpperCase());
        }
        
        // Create Pageable object with page, size, and sorting
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // Execute ONE database query (no loop, no multiple calls)
        return medicineRepository.findAll(pageable);
    }

    /**
     * REQUIREMENT 3: Search medicines by name with PAGINATION
     * 
     * API USAGE:
     * GET /api/medicines/search?name=Paracet&page=0&size=10
     * 
     * Database Query Generated:
     * SELECT * FROM medicines WHERE generic_name LIKE 'Paracet%' LIMIT 10 OFFSET 0;
     */
    @GetMapping("/search")
    public Page<Medicine> searchMedicines(
            // Search term provided by user
            @RequestParam String name,
            // Pagination parameters
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Create pagination object
        Pageable pageable = PageRequest.of(page, size);
        
        // Execute ONE database query to search and paginate
        return medicineRepository.findByGenericNameContainingIgnoreCase(name, pageable);
    }

    /**
     * Get a single medicine by ID
     * 
     * API USAGE:
     * GET /api/medicines/5
     * 
     * Database Query Generated:
     * SELECT * FROM medicines WHERE id = 5;
     * 
     * This is a DIRECT lookup - O(1) complexity, very fast!
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMedicineById(
            // The medicine ID (extracted from URL)
            @PathVariable Long id) {
        
        // Execute ONE database query: Get medicine by ID directly
        Optional<Medicine> medicine = medicineRepository.findById(id);
        
        // Return medicine if found, or 404 if not found (no loop, no iteration)
        return medicine.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a new medicine
     * 
     * API USAGE:
     * POST /api/medicines
     * Body: {"genericName": "Paracetamol", "brandName": "Panadol", ...}
     * 
     * Database Operation:
     * INSERT INTO medicines (...) VALUES (...);
     */
    @PostMapping
    public ResponseEntity<?> createMedicine(
            // Request body containing medicine data
            @RequestBody Medicine medicine) {
        
        // REQUIREMENT 7: Check if medicine already exists using existsBy()
        // This uses: SELECT COUNT(*) > 0 FROM medicines WHERE generic_name = ?
        // Much faster than loading the whole object!
        if (medicineRepository.existsByGenericName(medicine.getGenericName())) {
            return ResponseEntity.badRequest()
                    .body("Medicine with name '" + medicine.getGenericName() + "' already exists");
        }
        
        // Execute ONE database query to save the medicine
        Medicine saved = medicineRepository.save(medicine);
        
        // Return the saved medicine with HTTP 201 Created
        return ResponseEntity.status(201).body(saved);
    }

    /**
     * Update an existing medicine
     * 
     * API USAGE:
     * PUT /api/medicines/5
     * Body: {"brandName": "Panadol Extra", ...}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMedicine(
            // The medicine ID to update
            @PathVariable Long id,
            // Updated medicine data
            @RequestBody Medicine updates) {
        
        // Execute ONE database query: Get the existing medicine by ID
        Optional<Medicine> existing = medicineRepository.findById(id);
        
        // If medicine doesn't exist, return 404
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Get the medicine from Optional
        Medicine medicine = existing.get();
        
        // Update fields if provided
        if (updates.getGenericName() != null) {
            medicine.setGenericName(updates.getGenericName());
        }
        if (updates.getBrandName() != null) {
            medicine.setBrandName(updates.getBrandName());
        }
        if (updates.getDescription() != null) {
            medicine.setDescription(updates.getDescription());
        }
        if (updates.getDosageForm() != null) {
            medicine.setDosageForm(updates.getDosageForm());
        }
        
        // Execute ONE database query: Save the updated medicine
        Medicine updated = medicineRepository.save(medicine);
        
        // Return the updated medicine
        return ResponseEntity.ok(updated);
    }

    /**
     * Get list of pharmacies that stock a specific medicine
     *
     * API USAGE:
     * GET /api/medicines/{id}/pharmacies?page=0&size=10
     *
     * This uses the join table via PharmacyMedicineRepository to reverse-lookup.
     */
    @GetMapping("/{id}/pharmacies")
    public ResponseEntity<?> getPharmaciesByMedicine(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PharmacyMedicine> results = pharmacyMedicineRepository.findByMedicineId(id, pageable);
        return ResponseEntity.ok(results);
    }

    /**
     * Delete a medicine by ID
     * 
     * API USAGE:
     * DELETE /api/medicines/5
     * 
     * Database Query Generated:
     * DELETE FROM medicines WHERE id = 5;
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMedicine(
            // The medicine ID to delete
            @PathVariable Long id) {
        
        // Execute ONE database query: Check if medicine exists
        if (!medicineRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        // Execute ONE database query: Delete the medicine
        medicineRepository.deleteById(id);
        
        // Return 204 No Content (successful deletion)
        return ResponseEntity.noContent().build();
    }
}
