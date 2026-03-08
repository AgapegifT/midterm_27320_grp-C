package com.example.mediconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * REQUIREMENT 4: Many-to-Many Relationship Setup
 * 
 * Many-to-Many: Medicine ↔ Pharmacy (through PharmacyMedicine join table)
 * 
 * RELATIONSHIP EXPLANATION:
 * - One Medicine can be stocked in many Pharmacies
 * - One Pharmacy can stock many Medicines
 * - To store additional data (quantity, price), we use a JOIN TABLE (PharmacyMedicine entity)
 * 
 * This Medicine entity has:
 * - @OneToMany relationship to PharmacyMedicine
 * - This means: "One medicine is stocked in multiple pharmacy locations"
 * 
 * The actual many-to-many connection flows through:
 * medicine → pharmacyMedicines (OneToMany) → pharmacy (via ManyToOne in PharmacyMedicine)
 */
@Entity
@Table(name = "medicines")
public class Medicine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic Information
    @Column(unique = true, nullable = false)
    private String genericName;      // e.g., "Paracetamol"
    
    private String brandName;        // e.g., "Panadol"
    
    @Column(length = 1000)
    private String description;
    
    // Medication Details
    private String dosageForm;       // e.g., "Tablet", "Capsule", "Liquid"
    private String strength;         // e.g., "500mg"
    
    // Regulations
    private Boolean requiresPrescription = false;  // Controlled substance?
    
    // Tracking
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * MANY-TO-MANY: One medicine is stocked in many pharmacies
     * This relationship is managed through PharmacyMedicine join table
     * mappedBy="medicine" means PharmacyMedicine.medicine is the owner
     */
    @OneToMany(mappedBy = "medicine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PharmacyMedicine> pharmacyStocks = new ArrayList<>();

    // Constructors
    public Medicine() {}

    public Medicine(String genericName, String brandName, String description) {
        this.genericName = genericName;
        this.brandName = brandName;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDosageForm() {
        return dosageForm;
    }

    public void setDosageForm(String dosageForm) {
        this.dosageForm = dosageForm;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public Boolean getRequiresPrescription() {
        return requiresPrescription;
    }

    public void setRequiresPrescription(Boolean requiresPrescription) {
        this.requiresPrescription = requiresPrescription;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<PharmacyMedicine> getPharmacyStocks() {
        return pharmacyStocks;
    }

    public void setPharmacyStocks(List<PharmacyMedicine> pharmacyStocks) {
        this.pharmacyStocks = pharmacyStocks;
    }
    
    @Override
    public String toString() {
        return "Medicine{" +
                "id=" + id +
                ", genericName='" + genericName + '\'' +
                ", brandName='" + brandName + '\'' +
                ", strength='" + strength + '\'' +
                '}';
    }
}
