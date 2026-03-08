package com.example.mediconnect.repository;

import com.example.mediconnect.model.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * REQUIREMENT 7: existsBy() methods for verification
 * REQUIREMENT 3: Pagination support for searching profiles
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    
    /**
     * REQUIREMENT 7: Check if email already registered
     */
    boolean existsByEmail(String email);
    
    /**
     * REQUIREMENT 7: Check if license number already used
     * Important for doctor and pharmacy verification
     */
    boolean existsByLicenseNumber(String licenseNumber);
    
    /**
     * Find profile by email
     */
    Optional<Profile> findByEmail(String email);
    
    /**
     * Find all doctors (profiles with doctor role)
     * Uses pagination for REQUIREMENT 3
     */
    @Query("SELECT p FROM Profile p WHERE p.user.role = 'DOCTOR' AND p.isApproved = true")
    Page<Profile> findAllDoctors(Pageable pageable);
    
    /**
     * Find all pharmacies (profiles with pharmacy role)
     * Uses pagination for REQUIREMENT 3
     */
    @Query("SELECT p FROM Profile p WHERE p.user.role = 'PHARMACY' AND p.isApproved = true")
    Page<Profile> findAllPharmacies(Pageable pageable);
    
    /**
     * Find pending approvals for admin
     * Profiles that need to be approved before access
     */
    @Query("SELECT p FROM Profile p WHERE p.isApproved = false AND p.isActive = true")
    List<Profile> findPendingApprovals();
    
    /**
     * Find by first name and last name - case insensitive search
     */
    List<Profile> findByFirstNameIgnoreCaseContainingOrLastNameIgnoreCaseContaining(
        String firstName, String lastName
    );
    
    /**
     * Search profiles with pagination - for REQUIREMENT 3
     */
    Page<Profile> findByFirstNameIgnoreCaseContainingOrLastNameIgnoreCaseContaining(
        String firstName, String lastName, Pageable pageable
    );
}
