package com.example.mediconnect.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * REQUIREMENT 4: Many-to-Many Relationship - JOIN TABLE ENTITY
 * 
 * THE CRITICAL CONCEPT:
 * This is the "joining" table that connects Pharmacy and Medicine in a many-to-many relationship.
 * 
 * Why do we need this entity?
 * =============================
 * 
 * Simple many-to-many (without @ManyToMany):
 * If Pharmacy and Medicine had @ManyToMany, JPA would auto-create a join table with only:
 * - pharmacy_id (FK)
 * - medicine_id (FK)
 * 
 * But in real life, we need to store:
 * - How many units are in stock? (quantity)
 * - What price does THIS pharmacy charge? (price might differ between pharmacies)
 * - When was stock last updated?
 * 
 * SOLUTION: Create explicit PharmacyMedicine entity with these extra columns!
 * 
 * DATABASE SCHEMA:
 * pharmacy_medicines table:
 * ├─ id (PK)
 * ├─ pharmacy_id (FK to pharmacies)
 * ├─ medicine_id (FK to medicines)
 * ├─ stock_quantity (How many units)
 * ├─ price (What the pharmacy charges)
 * └─ last_updated (When was this updated)
 * 
 * RELATIONSHIP MAPPING:
 * PharmacyMedicine has TWO @ManyToOne relationships:
 * - Many PharmacyMedicine → One Pharmacy
 * - Many PharmacyMedicine → One Medicine
 * 
 * This creates the many-to-many through the join table!
 * 
 * EXAMPLE DATA:
 * pharmacy_medicines table:
 * ┌────┬──────────────┬──────────────┬─────────────┬────────┐
 * │ id │ pharmacy_id │ medicine_id  │ quantity    │ price  │
 * ├────┼──────────────┼──────────────┼─────────────┼────────┤
 * │ 1  │ 1 (City Rx)  │ 5 (Panadol)  │ 100 units   │ 500 RWF│
 * │ 2  │ 1 (City Rx)  │ 10 (Amoxil)  │ 50 units    │ 1000 RWF
 * │ 3  │ 2 (Central)  │ 5 (Panadol)  │ 75 units    │ 480 RWF│
 * │ 4  │ 2 (Central)  │ 10 (Amoxil)  │ 40 units    │ 950 RWF│
 * └────┴──────────────┴──────────────┴─────────────┴────────┘
 * 
 * From this table we can query:
 * - "Where can I find Panadol?" → rows where medicine_id = 5 → get all pharmacy_ids
 * - "What has City Rx in stock?" → rows where pharmacy_id = 1 → get all medicine_ids
 * - "What price for Panadol at City Rx?" → Find the row, read price column
 */
@Entity
@Table(name = "pharmacy_medicines")
public class PharmacyMedicine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * MANY-TO-ONE: Many stock records belong to ONE pharmacy
     * Foreign key: pharmacy_id in this table points to pharmacies.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;
    
    /**
     * MANY-TO-ONE: Many stock records point to ONE medicine
     * Foreign key: medicine_id in this table points to medicines.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;
    
    /**
     * Additional data on the relationship:
     * How many units does this pharmacy have of this medicine?
     */
    private Integer stockQuantity = 0;
    
    /**
     * What does this specific pharmacy charge for this medicine?
     * Different pharmacies may charge different prices
     */
    private BigDecimal price;
    
    /**
     * Timestamp for inventory tracking
     */
    private LocalDateTime lastUpdated = LocalDateTime.now();
    
    /**
     * Reorder level - when stock falls below this, pharmacist should reorder
     */
    private Integer reorderLevel = 10;
    
    /**
     * Status: Active, Out of Stock, Discontinued, etc.
     */
    @Enumerated(EnumType.STRING)
    private StockStatus status = StockStatus.ACTIVE;
    
    // Constructors
    public PharmacyMedicine() {}
    
    public PharmacyMedicine(Pharmacy pharmacy, Medicine medicine, Integer quantity, BigDecimal price) {
        this.pharmacy = pharmacy;
        this.medicine = medicine;
        this.stockQuantity = quantity;
        this.price = price;
        this.lastUpdated = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Pharmacy getPharmacy() {
        return pharmacy;
    }
    
    public void setPharmacy(Pharmacy pharmacy) {
        this.pharmacy = pharmacy;
    }
    
    public Medicine getMedicine() {
        return medicine;
    }
    
    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }
    
    public Integer getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public Integer getReorderLevel() {
        return reorderLevel;
    }
    
    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
    }
    
    public StockStatus getStatus() {
        return status;
    }
    
    public void setStatus(StockStatus status) {
        this.status = status;
    }
    
    /**
     * Helper method: Is this item in stock?
     */
    public Boolean isInStock() {
        return stockQuantity > 0 && status == StockStatus.ACTIVE;
    }
    
    /**
     * Helper method: Should we reorder?
     */
    public Boolean needsReorder() {
        return stockQuantity <= reorderLevel;
    }
    
    @Override
    public String toString() {
        return "PharmacyMedicine{" +
                "id=" + id +
                ", pharmacy=" + (pharmacy != null ? pharmacy.getName() : "null") +
                ", medicine=" + (medicine != null ? medicine.getGenericName() : "null") +
                ", quantity=" + stockQuantity +
                ", price=" + price +
                '}';
    }
}
