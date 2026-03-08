package com.example.mediconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * REQUIREMENT 2: Saving Location - EMBEDDABLE OBJECT
 * REQUIREMENT 4: Many-to-Many relationship with Medicine (via PharmacyMedicine)
 * 
 * This entity represents a pharmacy in the system.
 * 
 * KEY RELATIONSHIPS:
 * 1. Pharmacy ↔ Profile (One-to-One): Each pharmacy is operated by a user with a profile
 * 2. Pharmacy ↔ Medicine (Many-to-Many): Through PharmacyMedicine join table
 * 3. Pharmacy ↔ Prescription (One-to-Many): Pharmacies dispense prescriptions
 * 
 * LOCATION DATA:
 * Instead of creating a separate locations table, we EMBED the Location object.
 * This means Location fields are stored directly in the pharmacy table with "location_" prefix.
 * Example columns: location_province, location_district, location_latitude, location_longitude
 * 
 * WHY EMBEDDING?
 * - A location belongs only to one pharmacy (not shared)
 * - We avoid unnecessary table joins
 * - Simpler queries when searching by location
 * - Better performance
 */
@Entity
@Table(name = "pharmacies")
public class Pharmacy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * ONE-TO-ONE: Link to the user profile of the pharmacy owner/manager
     * This pharmacy belongs to one user profile
     */
    @OneToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;
    
    // Basic Information
    private String name;
    private String registrationNumber;
    private String licenseNumber;
    private LocalDateTime licenseExpiry;
    
    // Contact Information
    private String email;
    private String phoneNumber;
    private String website;
    
    /**
     * REQUIREMENT 2: EMBEDDED LOCATION OBJECT
     * This @Embedded annotation tells JPA to store Location as columns in the pharmacy table
     * NOT as a separate table!
     * 
     * Database columns will be created with "location_" prefix:
     * - location_province
     * - location_district
     * - location_sector
     * - location_latitude
     * - location_longitude
     * - location_street_address
     * etc.
     * 
     * STORAGE EXPLANATION:
     * When we save a Pharmacy with a Location:
     * Pharmacy pharmacy = new Pharmacy();
     * pharmacy.setLocation(new Location("Kigali", "Gasabo", "Kimironko", 1.9441, 30.0619));
     * pharmacyRepository.save(pharmacy);
     * 
     * This saves to ONE row in pharmacy table with location data in location_* columns
     * No separate locations table is created!
     * 
     * RELATIONSHIP HANDLING:
     * If we query pharmacies by province:
     * List<Pharmacy> kigaliPharmacies = pharmacyRepository.findByLocationProvince("Kigali");
     * 
     * This generates SQL:
     * SELECT * FROM pharmacies WHERE location_province = 'Kigali'
     * 
     * No JOINs needed! Simple and efficient!
     */
    @Embedded
    private Location location;
    
    // Status Information
    private Boolean isApproved = false;    // Admin must approve new pharmacies
    private Boolean isActive = true;       // Can be disabled by admin
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * ONE-TO-MANY: This pharmacy stocks many medicines
     * Through PharmacyMedicine join table
     * mappedBy="pharmacy" means PharmacyMedicine.pharmacy is the owner
     */
    @OneToMany(mappedBy = "pharmacy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PharmacyMedicine> medicines = new ArrayList<>();
    
    /**
     * ONE-TO-MANY: This pharmacy dispenses many prescriptions
     */
    @OneToMany(mappedBy = "pharmacy")
    private List<Prescription> prescriptions = new ArrayList<>();

    // Constructors
    public Pharmacy() {}

    public Pharmacy(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public LocalDateTime getLicenseExpiry() {
        return licenseExpiry;
    }

    public void setLicenseExpiry(LocalDateTime licenseExpiry) {
        this.licenseExpiry = licenseExpiry;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
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

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<PharmacyMedicine> getMedicines() {
        return medicines;
    }

    public void setMedicines(List<PharmacyMedicine> medicines) {
        this.medicines = medicines;
    }

    public List<Prescription> getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(List<Prescription> prescriptions) {
        this.prescriptions = prescriptions;
    }
    
    @Override
    public String toString() {
        return "Pharmacy{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location=" + (location != null ? location.getProvince() + ", " + location.getDistrict() : "null") +
                ", isApproved=" + isApproved +
                '}';
    }
}
