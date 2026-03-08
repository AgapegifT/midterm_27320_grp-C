package com.example.mediconnect.repository;

import com.example.mediconnect.model.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * REQUIREMENT 3: Pagination and Sorting
 * REQUIREMENT 7: existsBy() method for existence checking
 * 
 * This repository demonstrates:
 * - How pagination works (splits large result sets into pages)
 * - How sorting works (order results by field)
 * - How Spring Data JPA reduces code by generating queries from method names
 */
@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    
    /**
     * Find medicine by generic name (exact match)
     */
    Optional<Medicine> findByGenericName(String genericName);
    
    /**
     * REQUIREMENT 7: Check if medicine with generic name already exists
     * EXISTENCE CHECKING:
     * Use: if (medicineRepository.existsByGenericName("Paracetamol"))
     * 
     * Prevents duplicate medicines in database
     * Method generates: SELECT COUNT(m) > 0 FROM medicines WHERE generic_name = ?
     */
    boolean existsByGenericName(String genericName);
    
    /**
     * REQUIREMENT 3: Search medicines with PAGINATION and SORTING
     * 
     * HOW PAGINATION WORKS:
     * Instead of: SELECT * FROM medicines WHERE generic_name LIKE 'Paracet%'
     * We use Pageable to get: SELECT * FROM medicines WHERE generic_name LIKE 'Paracet%' LIMIT 10 OFFSET 0
     * 
     * This loads only 10 results at a time instead of potentially thousands!
     * 
     * EXAMPLE API CALL:
     * GET /api/medicines/search?name=Paracet&page=0&size=10&sort=genericName,asc
     * 
     * Parameters:
     * - page: Which page (0-indexed)
     * - size: How many per page
     * - sort: Field to sort by, direction (asc/desc)
     * 
     * PERFORMANCE BENEFITS:
     * - Reduces memory usage (load only 10 instead of 1000)
     * - Faster response time (less data to transfer)
     * - Better user experience (instant first results)
     * - Scalable (works with millions of records)
     * 
     * Returns Page<Medicine> which contains:
     * - Content: List of 10 medicines
     * - TotalElements: Total count (1234)
     * - TotalPages: How many pages total (124 pages)
     * - CurrentPageNumber: Which page was returned (0)
     */
    Page<Medicine> findByGenericNameContainingIgnoreCase(String name, Pageable pageable);
    
    /**
     * Find medicines that require prescription
     * Used for REQUIREMENT 3: Pagination
     */
    Page<Medicine> findByRequiresPrescription(Boolean requiresPrescription, Pageable pageable);
    
    /**
     * Find all medicines with pagination support
     * Allows sorting and pagination for REQUIREMENT 3
     */
    @Override
    Page<Medicine> findAll(Pageable pageable);
    
    /**
     * Find medicines by strength with pagination
     */
    Page<Medicine> findByStrengthContainingIgnoreCase(String strength, Pageable pageable);
    
    /**
     * Search across multiple fields with pagination
     */
    @Query("SELECT m FROM Medicine m WHERE " +
           "LOWER(m.genericName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.brandName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Medicine> searchMedicines(@Param("search") String search, Pageable pageable);
}

