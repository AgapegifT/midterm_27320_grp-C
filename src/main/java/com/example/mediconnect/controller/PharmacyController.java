package com.example.mediconnect.controller;

import com.example.mediconnect.model.*;
import com.example.mediconnect.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REQUIREMENT 2: Pharmacy with Embedded Location implementation
 * REQUIREMENT 3: Pagination and Sorting for large datasets
 * REQUIREMENT 4: Many-to-Many relationship (Pharmacy ↔ Medicine via PharmacyMedicine)
 * REQUIREMENT 8: Find pharmacies by province (code or name)
 * REQUIREMENT 7: existsBy() methods for duplicate prevention
 * 
 * Database Query Strategy:
 * - No loops in database code
 * - All queries are single-line repository calls
 * - Let database handle the work (not Java code)
 * - Spring Data JPA handles pagination at database level
 * 
 * PERFORMANCE BENEFIT OF PAGINATION:
 * Without pagination: SELECT * FROM pharmacies (loads all 1000 records into memory)
 * With pagination:    SELECT * FROM pharmacies LIMIT 10 OFFSET 0 (loads only 10)
 */
@RestController
@RequestMapping("/api/pharmacies")
public class PharmacyController {
    
    // Inject repositories for database operations
    private final PharmacyRepository pharmacyRepository;
    private final MedicineRepository medicineRepository;
    private final PharmacyMedicineRepository pharmacyMedicineRepository;

    // Constructor injection ensures dependencies are provided
    public PharmacyController(PharmacyRepository pharmacyRepository,
                              MedicineRepository medicineRepository,
                              PharmacyMedicineRepository pharmacyMedicineRepository) {
        this.pharmacyRepository = pharmacyRepository;
        this.medicineRepository = medicineRepository;
        this.pharmacyMedicineRepository = pharmacyMedicineRepository;
    }

    /**
     * Register a new pharmacy
     * REQUIREMENT 2: Accepts location data in the request
     * REQUIREMENT 7: Check for duplicate license numbers
     * 
     * API USAGE:
     * POST /api/pharmacies
     * Body: {
     *   "name": "City Pharmacy",
     *   "email": "city@pharmacy.rw",
     *   "phoneNumber": "0788123456",
     *   "licenseNumber": "LIC-001",
     *   "location": {
     *     "province": "Kigali",
     *     "provinceCode": "01",
     *     "district": "Gasabo",
     *     "sector": "Kimironko",
     *     "latitude": 1.9441,
     *     "longitude": 30.0619
     *   }
     * }
     * 
     * Database Operation:
     * INSERT INTO pharmacies (name, email, phone_number, license_number, 
     *                        location_province, location_district, ...)
     * VALUES (...);
     */
    @PostMapping
    public ResponseEntity<?> registerPharmacy(
            // Request body containing pharmacy registration data
            @RequestBody Pharmacy pharmacy) {
        
        // Validate that location information was provided (REQUIREMENT 2)
        if (pharmacy.getLocation() == null) {
            return ResponseEntity.badRequest()
                    .body("Location information is required");
        }
        
        // REQUIREMENT 7: Check if license number already exists
        // Single database query: SELECT COUNT(*) > 0 FROM pharmacies WHERE license_number = ?
        if (pharmacy.getLicenseNumber() != null && 
            pharmacyRepository.existsByLicenseNumber(pharmacy.getLicenseNumber())) {
            return ResponseEntity.badRequest()
                    .body("License number already registered: " + pharmacy.getLicenseNumber());
        }
        
        // Execute ONE database query: Save the pharmacy
        Pharmacy saved = pharmacyRepository.save(pharmacy);
        
        // Return the registered pharmacy with HTTP 201 Created status
        return ResponseEntity.status(201).body(saved);
    }

    /**
     * REQUIREMENT 3: Get all pharmacies with PAGINATION and SORTING
     * 
     * API ENDPOINT USAGE EXAMPLES:
     * 
     * 1. Basic pagination (first 10 pharmacies):
     *    GET /api/pharmacies?page=0&size=10
     * 
     * 2. Sort by name in ascending order:
     *    GET /api/pharmacies?page=0&size=20&sort=name,asc
     * 
     * 3. Sort by creation date descending (newest first):
     *    GET /api/pharmacies?page=0&size=10&sort=createdAt,desc
     * 
     * RESPONSE EXAMPLE (when there are 53 pharmacies total):
     * {
     *   "content": [
     *     { "id": 1, "name": "City Pharmacy", "location": {...} },
     *     { "id": 2, "name": "Downtown Pharmacy", "location": {...} },
     *     ...  (8 more up to 10 total)
     *   ],
     *   "pageable": { "pageNumber": 0, "pageSize": 10 },
     *   "totalElements": 53,
     *   "totalPages": 6,
     *   "first": true,
     *   "last": false
     * }
     * 
     * PERFORMANCE EXPLANATION (REQUIREMENT 3):
     * Database Query Generated:
     * SELECT * FROM pharmacies 
     * ORDER BY created_at DESC
     * LIMIT 10 OFFSET 0
     * 
     * Benefits of Pagination:
     * ✓ Memory usage: Only 10 pharmacies in memory instead of 53
     * ✓ Network: Fewer bytes transferred to client
     * ✓ Database: Faster queries (database doesn't fetch unnecessary rows)
     * ✓ User experience: First page loads instantly instead of waiting
     * 
     * NO LOOP - Database handles sorting and pagination
     */
    @GetMapping
    public ResponseEntity<?> getAllPharmacies(
            // REQUIREMENT 3: Page number (0-indexed, so page 0 = first page)
            @RequestParam(defaultValue = "0") int page,
            // REQUIREMENT 3: Records per page (how many results per request)
            @RequestParam(defaultValue = "10") int size,
            // Sorting field and direction (e.g., "createdAt,desc")
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        // Parse sort parameter to extract field name and direction
        String sortBy = "createdAt";          // Default sort field
        Sort.Direction direction = Sort.Direction.DESC;  // Default direction (descending)
        
        // If sort parameter contains comma, parse it
        if (sort.contains(",")) {
            String[] parts = sort.split(",");
            sortBy = parts[0];
            direction = Sort.Direction.fromString(parts[1].toUpperCase());
        }
        
        // Create pagination object that tells database to:
        // - Skip (page * size) records (OFFSET)
        // - Return only 'size' records (LIMIT)
        // - Sort by the specified field
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // Execute ONE database query: Get all pharmacies with pagination
        // Database returns only 10 records, not millions
        Page<Pharmacy> pharmacies = pharmacyRepository.findAll(pageable);
        
        // Return the paginated results
        return ResponseEntity.ok(pharmacies);
    }

    /**
     * REQUIREMENT 8: Find pharmacies by PROVINCE NAME or CODE
     * REQUIREMENT 2: Query embedded Location object
     * REQUIREMENT 3: With pagination
     * 
     * API USAGE:
     * Search by province NAME:
     *   GET /api/pharmacies/by-province?provinceName=Kigali&page=0&size=20
     * 
     * Search by province CODE:
     *   GET /api/pharmacies/by-province?provinceCode=01&page=0&size=20
     * 
     * HOW LOCATION QUERYING WORKS (REQUIREMENT 2):
     * Pharmacy has @Embedded Location with province field
     * Location data is stored directly in pharmacy table with "location_" prefix:
     * - location_province (stored as column in pharmacies table)
     * - location_district
     * - location_sector
     * 
     * Database Query Generated for provinceName="Kigali":
     * SELECT * FROM pharmacies 
     * WHERE location_province = 'Kigali' 
     * LIMIT 20 OFFSET 0
     * 
     * PERFORMANCE NOTE:
     * NO JOIN NEEDED! The location is embedded in same table!
     * Much faster than if Location was a separate table requiring JOIN!
     * 
     * This demonstrates why Embedded objects are better for simple entities
     */
    @GetMapping("/by-province")
    public ResponseEntity<?> getPharmaciesByProvince(
            // REQUIREMENT 8: Search by province name (e.g., "Kigali")
            @RequestParam(required = false) String provinceName,
            // REQUIREMENT 8: Search by province code (e.g., "01")
            @RequestParam(required = false) String provinceCode,
            // REQUIREMENT 3: Pagination parameters
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Create pagination object
        Pageable pageable = PageRequest.of(page, size);
        
        // Variable to hold the query result
        Page<Pharmacy> result;
        
        // REQUIREMENT 8: Allow search by province name OR code (flexible search)
        if (provinceName != null && !provinceName.isEmpty()) {
            // Execute ONE database query: Find pharmacies by province NAME
            // WHERE location_province = 'Kigali'
            result = pharmacyRepository.findByLocationProvince(provinceName, pageable);
        } else if (provinceCode != null && !provinceCode.isEmpty()) {
            // Execute ONE database query: Find pharmacies by province CODE
            // WHERE location_province_code = '01'
            result = pharmacyRepository.findByLocationProvinceCode(provinceCode, pageable);
        } else {
            // Neither parameter provided
            return ResponseEntity.badRequest()
                    .body("Please provide either 'provinceName' or 'provinceCode' parameter");
        }
        
        // Return the paginated results
        return ResponseEntity.ok(result);
    }

    /**
     * REQUIREMENT 8: Find pharmacies by PROVINCE AND DISTRICT
     * More specific location-based search
     * 
     * API USAGE:
     * GET /api/pharmacies/by-location?province=Kigali&district=Gasabo&page=0&size=10
     * 
     * Database Query Generated:
     * SELECT * FROM pharmacies 
     * WHERE location_province = 'Kigali' 
     * AND location_district = 'Gasabo'
     * LIMIT 10 OFFSET 0
     */
    @GetMapping("/by-location")
    public ResponseEntity<?> getPharmaciesByLocation(
            // REQUIREMENT 2: Province name (embedded property)
            @RequestParam String province,
            // REQUIREMENT 2: District name (embedded property)
            @RequestParam String district,
            // REQUIREMENT 3: Pagination parameters
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Create pagination object
        Pageable pageable = PageRequest.of(page, size);
        
        // Execute ONE database query: Find pharmacies by province AND district
        // NO LOOP - Database handles filtering and pagination
        Page<Pharmacy> pharmacies = pharmacyRepository
                .findByLocationProvinceAndLocationDistrict(province, district, pageable);
        
        // Return the paginated results
        return ResponseEntity.ok(pharmacies);
    }

    /**
     * REQUIREMENT 4: Get medicines stocked by a pharmacy
     * This demonstrates the MANY-TO-MANY relationship
     * REQUIREMENT 3: With pagination
     * 
     * HOW MANY-TO-MANY WORKS:
     * - Pharmacy has many PharmacyMedicine records (join table)
     * - Each PharmacyMedicine points to one Medicine
     * - Therefore: One pharmacy stocks many medicines
     * 
     * Database query generated:
     * SELECT pm.* FROM pharmacy_medicines pm
     * WHERE pm.pharmacy_id = 5
     * ORDER BY pm.medicine_id ASC
     * LIMIT 10 OFFSET 0
     * 
     * Then for each pm, we can access the Medicine via hibernate lazy loading
     * 
     * API USAGE:
     * GET /api/pharmacies/5/medicines?page=0&size=20
     */
    @GetMapping("/{pharmacyId}/medicines")
    public ResponseEntity<?> getPharmacyMedicines(
            // The pharmacy ID to get medicines from
            @PathVariable Long pharmacyId,
            // REQUIREMENT 3: Pagination parameters
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        // Execute ONE database query: Check if pharmacy exists
        Optional<Pharmacy> pharmacy = pharmacyRepository.findById(pharmacyId);
        
        // If pharmacy doesn't exist, return 404 Not Found
        if (pharmacy.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Create pagination object
        Pageable pageable = PageRequest.of(page, size);
        
        // Execute ONE database query: Get all medicines for this pharmacy
        // REQUIREMENT 4: Query many-to-many join table
        // NO LOOP - Database joins pharmacy_medicines with medicines
        Page<PharmacyMedicine> medicines = pharmacyMedicineRepository
                .findByPharmacyId(pharmacyId, pageable);
        
        // Return the paginated medicine list
        return ResponseEntity.ok(medicines);
    }

    /**
     * REQUIREMENT 4: Add or update medicine in pharmacy stock
     * This creates/updates a record in the PharmacyMedicine join table
     * 
     * REQUEST BODY EXAMPLE:
     * {
     *   "medicine": { "id": 5 },
     *   "stockQuantity": 100,
     *   "price": "500.00",
     *   "reorderLevel": 20,
     *   "status": "ACTIVE"
     * }
     * 
     * Database Operations (2 queries total):
     * 1. SELECT * FROM pharmacies WHERE id = ?
     * 2. INSERT/UPDATE pharmacy_medicines ...
     */
    @PostMapping("/{pharmacyId}/medicines")
    public ResponseEntity<?> addOrUpdateMedicine(
            // The pharmacy ID to add medicine to
            @PathVariable Long pharmacyId,
            // Request body with medicine and stock details
            @RequestBody PharmacyMedicine pharmacyMedicine) {
        
        // Execute ONE database query: Verify pharmacy exists
        Optional<Pharmacy> pharmacy = pharmacyRepository.findById(pharmacyId);
        
        // If pharmacy doesn't exist, return 404
        if (pharmacy.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Execute ONE database query: Verify medicine exists
        if (!medicineRepository.existsById(pharmacyMedicine.getMedicine().getId())) {
            return ResponseEntity.badRequest()
                    .body("Medicine not found");
        }
        
        // Set the pharmacy on the join table record
        pharmacyMedicine.setPharmacy(pharmacy.get());
        
        // Execute ONE database query: Save or update the pharmacy-medicine link
        PharmacyMedicine saved = pharmacyMedicineRepository.save(pharmacyMedicine);
        
        // Return the saved record with HTTP 201 Created
        return ResponseEntity.status(201).body(saved);
    }

    /**
     * Search pharmacies by name with pagination
     * 
     * API USAGE:
     * GET /api/pharmacies/search?name=City&page=0&size=10
     * 
     * Database Query Generated:
     * SELECT * FROM pharmacies 
     * WHERE UPPER(name) LIKE UPPER('%City%')
     * LIMIT 10 OFFSET 0
     * 
     * REQUIREMENT 3: Pagination improves performance
     * REQUIREMENT 7: Case-insensitive search (helpful for users)
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchPharmacies(
            // The search term to find pharmacies by name
            @RequestParam String name,
            // REQUIREMENT 3: Pagination parameters
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Create pagination object
        Pageable pageable = PageRequest.of(page, size);
        
        // Execute ONE database query: Find pharmacies by name (case-insensitive)
        // NO LOOP - Database handles searching and pagination
        Page<Pharmacy> pharmacies = pharmacyRepository
                .findByNameContainingIgnoreCase(name, pageable);
        
        // Return the paginated search results
        return ResponseEntity.ok(pharmacies);
    }

    /**
     * Get a single pharmacy by ID
     * 
     * API USAGE:
     * GET /api/pharmacies/1
     * 
     * Database Query Generated:
     * SELECT * FROM pharmacies WHERE id = 1
     * 
     * This is a direct lookup by primary key - O(1) complexity, very fast!
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPharmacy(
            // The pharmacy ID (extracted from URL)
            @PathVariable Long id) {
        
        // Execute ONE database query: Get pharmacy by ID directly
        Optional<Pharmacy> pharmacy = pharmacyRepository.findById(id);
        
        // Return pharmacy if found, or 404 if not found
        return pharmacy.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Update pharmacy details
     * 
     * API USAGE:
     * PUT /api/pharmacies/1
     * Body: {
     *   "name": "New Name",
     *   "email": "new@pharmacy.rw",
     *   "location": { "province": "Kigali", ... }
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePharmacy(
            // The pharmacy ID to update
            @PathVariable Long id,
            // Updated pharmacy data
            @RequestBody Pharmacy updates) {
        
        // Execute ONE database query: Get pharmacy by ID
        Optional<Pharmacy> existing = pharmacyRepository.findById(id);
        
        // If pharmacy doesn't exist, return 404 Not Found
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Get the pharmacy from Optional
        Pharmacy pharmacy = existing.get();
        
        // Update name if provided
        if (updates.getName() != null && !updates.getName().isEmpty()) {
            pharmacy.setName(updates.getName());
        }
        
        // Update email if provided
        if (updates.getEmail() != null && !updates.getEmail().isEmpty()) {
            pharmacy.setEmail(updates.getEmail());
        }
        
        // Update phone number if provided
        if (updates.getPhoneNumber() != null && !updates.getPhoneNumber().isEmpty()) {
            pharmacy.setPhoneNumber(updates.getPhoneNumber());
        }
        
        // Update location if provided
        if (updates.getLocation() != null) {
            pharmacy.setLocation(updates.getLocation());
        }
        
        // Execute ONE database query: Save the updated pharmacy
        Pharmacy updated = pharmacyRepository.save(pharmacy);
        
        // Return the updated pharmacy
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a pharmacy by ID
     * 
     * API USAGE:
     * DELETE /api/pharmacies/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePharmacy(
            // The pharmacy ID to delete
            @PathVariable Long id) {
        
        // Execute ONE database query: Check if pharmacy exists
        if (!pharmacyRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        // Execute ONE database query: Delete the pharmacy
        pharmacyRepository.deleteById(id);
        
        // Return 204 No Content (successful deletion, no response body)
        return ResponseEntity.noContent().build();
    }
}

