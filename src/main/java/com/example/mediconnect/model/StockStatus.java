package com.example.mediconnect.model;

/**
 * Enumeration for stock status in PharmacyMedicine
 */
public enum StockStatus {
    ACTIVE,              // Medicine is in stock and available
    OUT_OF_STOCK,       // Medicine is out of stock temporarily
    DISCONTINUED,       // Medicine is no longer stocked
    LOW_STOCK,          // Medicine is below reorder level
    PENDING_DELIVERY    // Medicine ordered but not yet received
}
