package com.example.mediconnect.model;

import jakarta.persistence.Embeddable;

/**
 * REQUIREMENT 2: Implementation of Saving Location
 * 
 * This is an @Embeddable class, which means Location data is stored directly in the Pharmacy table
 * WITHOUT creating a separate table. When we embed a class, JPA adds its fields as columns to the parent entity.
 * 
 * Example: Instead of having a locations table with pharmacy_id foreign key,
 * we have city, province, district, latitude, longitude columns directly in the pharmacy table.
 * 
 * Benefits:
 * - No need for extra JOINs in queries
 * - Cleaner object model
 * - Location always belongs to one pharmacy
 * - Reduces table joins for performance
 * 
 * How it's stored:
 * In the pharmacy table, you'll see columns like:
 * - location_province VARCHAR(100)
 * - location_district VARCHAR(100)
 * - location_city VARCHAR(100)
 * - location_latitude DOUBLE
 * - location_longitude DOUBLE
 */
@Embeddable
public class Location {
    
    /**
     * Province code (e.g., "01" for Kigali, "02" for Southern Province)
     */
    private String provinceCode;
    
    /**
     * Full province name (e.g., "Kigali City")
     */
    private String province;
    
    /**
     * District name (e.g., "Gasabo", "Kicukiro")
     */
    private String district;
    
    /**
     * Sector within district (e.g., "Kimironko", "Gisozi")
     */
    private String sector;
    
    /**
     * Cell within sector
     */
    private String cell;
    
    /**
     * Village - most granular level
     */
    private String village;
    
    /**
     * Geographic coordinates for map-based search
     */
    private Double latitude;
    private Double longitude;
    
    /**
     * Street address as text
     */
    private String streetAddress;
    
    // Constructors
    public Location() {}
    
    public Location(String province, String district, String sector, Double latitude, Double longitude) {
        this.province = province;
        this.district = district;
        this.sector = sector;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    // Getters and Setters
    public String getProvinceCode() {
        return provinceCode;
    }
    
    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }
    
    public String getProvince() {
        return province;
    }
    
    public void setProvince(String province) {
        this.province = province;
    }
    
    public String getDistrict() {
        return district;
    }
    
    public void setDistrict(String district) {
        this.district = district;
    }
    
    public String getSector() {
        return sector;
    }
    
    public void setSector(String sector) {
        this.sector = sector;
    }
    
    public String getCell() {
        return cell;
    }
    
    public void setCell(String cell) {
        this.cell = cell;
    }
    
    public String getVillage() {
        return village;
    }
    
    public void setVillage(String village) {
        this.village = village;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public String getStreetAddress() {
        return streetAddress;
    }
    
    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }
    
    @Override
    public String toString() {
        return "Location{" +
                "province='" + province + '\'' +
                ", district='" + district + '\'' +
                ", sector='" + sector + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
