package com.example.mediconnect.repository;

import com.example.mediconnect.model.Pharmacy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * REQUIREMENT 2: Location Implementation
 * REQUIREMENT 8: Retrieve pharmacies by province code OR province name
 * REQUIREMENT 3: Pagination and Sorting Support
 * 
 * This repository shows how to query EMBEDDED objects (Location)
 * Location is @Embedded in Pharmacy, so we can query its fields directly
 */
@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {
    
    /**
     * REQUIREMENT 8: Find pharmacies by province name
     * 
     * REQUIREMENT 2: HOW LOCATION IS STORED:
     * Location is embedded (not a separate table), so pharmacy table has columns:
     * - location_province VARCHAR(100)
     * - location_district VARCHAR(100)
     * - location_latitude DOUBLE
     * - location_longitude DOUBLE
     * 
     * This method queries the location_province column directly!
     * Generated SQL: SELECT * FROM pharmacies WHERE location_province = 'Kigali'
     * 
     * REQUIREMENT 2: RELATIONSHIP HANDLING:
     * Since Location is embedded, NO JOIN needed - just a simple WHERE clause
     * Much faster than if Location was a separate table
     * 
     * EXAMPLE USE:
     * List<Pharmacy> kigaliDrugs = pharmacyRepository.findByLocationProvince("Kigali");
     */
    List<Pharmacy> findByLocationProvince(String province);
    
    /**
     * REQUIREMENT 8: Find pharmacies by province code
     * Some applications use codes instead of full names
     * e.g., "01" for Kigali, "02" for Southern Province
     * 
     * This allows flexibility - users can search by:
     * - Province name: "Kigali"
     * - Province code: "01"
     */
    List<Pharmacy> findByLocationProvinceCode(String provinceCode);
    
    /**
     * REQUIREMENT 3: PAGINATION version of province search
     * Instead of returning all results, this returns one page at a time
     * 
     * EXAMPLE API CALL:
     * GET /api/pharmacies/by-province?province=Kigali&page=0&size=20
     * 
     * With 100 pharmacies in Kigali, this returns only 20 at a time
     * Saves memory and network bandwidth!
     */
    Page<Pharmacy> findByLocationProvince(String province, Pageable pageable);
    
    /**
     * REQUIREMENT 3: PAGINATION with province code
     */
    Page<Pharmacy> findByLocationProvinceCode(String provinceCode, Pageable pageable);
    
    /**
     * Find pharmacies by multiple location attributes
     * More specific search - province AND district
     */
    List<Pharmacy> findByLocationProvinceAndLocationDistrict(String province, String district);
    
    /**
     * REQUIREMENT 3: Paginated version - province and district
     */
    Page<Pharmacy> findByLocationProvinceAndLocationDistrict(
        String province, String district, Pageable pageable
    );
    
    /**
     * Find pharmacies by sector (most specific)
     */
    List<Pharmacy> findByLocationSector(String sector);
    
    /**
     * Find approved pharmacies only
     */
    List<Pharmacy> findByIsApprovedAndIsActive(Boolean isApproved, Boolean isActive);
    
    /**
     * REQUIREMENT 3: Find approved pharmacies with pagination
     */
    Page<Pharmacy> findByIsApprovedAndIsActive(
        Boolean isApproved, Boolean isActive, Pageable pageable
    );
    
    /**
     * Find pharmacies by name with pagination
     * Used for search feature
     */
    Page<Pharmacy> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    /**
     * Custom query combining location and approval status
     * REQUIREMENT 8: Find by province with approval filter
     */
    @Query("SELECT p FROM Pharmacy p WHERE p.location.province = :province AND p.isApproved = true")
    List<Pharmacy> findApprovedByProvince(@Param("province") String province);
    
    /**
     * REQUIREMENT 8: Find pharmacies by province name OR province code
     * This demonstrates flexible querying
     */
    @Query("SELECT p FROM Pharmacy p WHERE " +
           "p.location.province = :provinceName OR " +
           "p.location.provinceCode = :provinceCode")
    List<Pharmacy> findByProvinceNameOrCode(@Param("provinceName") String provinceName,
                                            @Param("provinceCode") String provinceCode);
    
    /**
     * REQUIREMENT 8 + REQUIREMENT 3: Combined - province search with pagination
     */
    @Query("SELECT p FROM Pharmacy p WHERE " +
           "p.location.province = :province OR " +
           "p.location.provinceCode = :province")
    Page<Pharmacy> findByProvinceFlexible(@Param("province") String province, Pageable pageable);
    
    /**
     * Find pharmacy by registration number
     */
    Optional<Pharmacy> findByRegistrationNumber(String registrationNumber);
    
    /**
     * REQUIREMENT 7: Check if pharmacy already registered with this license
     */
    boolean existsByLicenseNumber(String licenseNumber);
}

