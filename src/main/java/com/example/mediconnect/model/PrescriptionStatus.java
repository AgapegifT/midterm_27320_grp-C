package com.example.mediconnect.model;

/**
 * Enum for prescription status lifecycle
 */
public enum PrescriptionStatus {
    ACTIVE,      // Prescription is valid and not yet filled
    DISPENSED,   // Pharmacy has dispensed the medicine
    EXPIRED,     // Prescription is beyond its validity period
    CANCELLED    // Prescription was cancelled by doctor or patient
}
