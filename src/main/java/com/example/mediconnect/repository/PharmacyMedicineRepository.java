package com.example.mediconnect.repository;

import com.example.mediconnect.model.PharmacyMedicine;
import com.example.mediconnect.model.StockStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * REQUIREMENT 4: Many-to-Many Relationship
 * REQUIREMENT 3: Pagination and Sorting
 * 
 * Repository for the many-to-many join table PharmacyMedicine
 * Demonstrates how to query through the join table
 */
@Repository
public interface PharmacyMedicineRepository extends JpaRepository<PharmacyMedicine, Long> {
    
    /**
     * Find all medicines stocked by a specific pharmacy
     * REQUIREMENT 4: Many-to-Many example
     */
    List<PharmacyMedicine> findByPharmacyId(Long pharmacyId);
    
    /**
     * REQUIREMENT 3: Paginated version - medicines in pharmacy
     * Useful for displaying pharmacy inventory page by page
     */
    Page<PharmacyMedicine> findByPharmacyId(Long pharmacyId, Pageable pageable);
    
    /**
     * Find all pharmacies stocking a specific medicine
     * REQUIREMENT 4: Many-to-Many - reverse direction
     */
    List<PharmacyMedicine> findByMedicineId(Long medicineId);
    
    /**
     * REQUIREMENT 3: Paginated - pharmacies that stock a medicine
     */
    Page<PharmacyMedicine> findByMedicineId(Long medicineId, Pageable pageable);
    
    /**
     * Find a specific medicine in a specific pharmacy
     */
    Optional<PharmacyMedicine> findByPharmacyIdAndMedicineId(Long pharmacyId, Long medicineId);
    
    /**
     * Find medicines in stock (not out of stock, not discontinued)
     * REQUIREMENT 3: With pagination
     */
    Page<PharmacyMedicine> findByPharmacyIdAndStatus(Long pharmacyId, StockStatus status, Pageable pageable);
    
    /**
     * Find medicines that need reordering
     */
    @Query("SELECT pm FROM PharmacyMedicine pm WHERE " +
           "pm.pharmacy.id = :pharmacyId AND " +
           "pm.stockQuantity <= pm.reorderLevel")
    List<PharmacyMedicine> findLowStockMedicines(@Param("pharmacyId") Long pharmacyId);
    
    /**
     * Find all active medicines in a pharmacy with pagination
     */
    @Query("SELECT pm FROM PharmacyMedicine pm WHERE " +
           "pm.pharmacy.id = :pharmacyId AND " +
           "pm.status = 'ACTIVE'")
    Page<PharmacyMedicine> findActiveStockByPharmacy(@Param("pharmacyId") Long pharmacyId, Pageable pageable);
    
    /**
     * Search medicine by name in a pharmacy
     */
    @Query("SELECT pm FROM PharmacyMedicine pm WHERE " +
           "pm.pharmacy.id = :pharmacyId AND " +
           "LOWER(pm.medicine.genericName) LIKE LOWER(CONCAT('%', :medicineName, '%'))")
    Page<PharmacyMedicine> searchMedicinesInPharmacy(
        @Param("pharmacyId") Long pharmacyId,
        @Param("medicineName") String medicineName,
        Pageable pageable
    );
}
