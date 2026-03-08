package com.example.mediconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * REQUIREMENT 6: Implementation of One-to-One Relationship
 * 
 * RELATIONSHIP MAPPING:
 * - User has ONE Profile (1:1)
 * - Profile owns the relationship (has the @JoinColumn)
 * - Foreign key: user_id in profiles table
 * 
 * Why separate User and Profile?
 * - User table: Stores authentication data (email, password_hash)
 * - Profile table: Stores personal/role-specific data (firstName, lastName, license, etc.)
 * - This separation improves security (auth logic separate from profile data)
 * - Allows role-specific fields for doctors, pharmacies, etc.
 */
@Entity
@Table(name = "profiles")
public class Profile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * ONE-TO-ONE: Each profile belongs to exactly one user
     * This side owns the relationship (contains @JoinColumn)
     * When we DELETE a user, the cascade will delete the profile too
     */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // Personal Information
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    
    // Professional/Role-Specific Information
    private String licenseNumber;           // For doctors and pharmacies
    private String licenseExpiry;          
    private String specialization;          // For doctors only
    private String pharmacyRegistration;   // For pharmacies only
    
    // Status & Verification
    private Boolean isApproved = false;    // Admin must approve doctors/pharmacies
    private Boolean isActive = true;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    // Constructors
    public Profile() {}
    
    public Profile(User user, String firstName, String lastName, String email) {
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getLicenseNumber() {
        return licenseNumber;
    }
    
    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
    
    public String getLicenseExpiry() {
        return licenseExpiry;
    }
    
    public void setLicenseExpiry(String licenseExpiry) {
        this.licenseExpiry = licenseExpiry;
    }
    
    public String getSpecialization() {
        return specialization;
    }
    
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
    
    public String getPharmacyRegistration() {
        return pharmacyRegistration;
    }
    
    public void setPharmacyRegistration(String pharmacyRegistration) {
        this.pharmacyRegistration = pharmacyRegistration;
    }
    
    public Boolean getIsApproved() {
        return isApproved;
    }
    
    public void setIsApproved(Boolean isApproved) {
        this.isApproved = isApproved;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Helper method: Get full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    @Override
    public String toString() {
        return "Profile{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", isApproved=" + isApproved +
                '}';
    }
}
