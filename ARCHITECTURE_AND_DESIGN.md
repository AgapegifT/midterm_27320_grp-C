# MediConnect RW - Complete Implementation Documentation

## Project Overview

MediConnect RW is a Spring Boot-based prescription management and pharmacy inventory system designed to meet all academic requirements for database relationships, pagination, sorting, and location-based querying.

---

## REQUIREMENT 1: Entity Relationship Diagram (ERD) - 3 Marks

### Our Database Schema (7 Tables)

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                        DATABASE RELATIONSHIPS                                │
│                         (Entity Diagram)                                      │
└──────────────────────────────────────────────────────────────────────────────┘

                              AUTHENTICATION LAYER
┌─────────────────┐           ┌──────────────────┐
│   users (PK)    │  1  :  1  │  profiles (PK)   │
├─────────────────┤───────────┤──────────────────┤
│ id (PK)         │           │ id (PK)          │
│ email (UNIQUE)  │           │ user_id (FK)     │
│ password        │           │ firstName        │
│ role (ENUM)     │           │ lastName         │
└─────────────────┘           │ email            │
                               │ licenseNumber    │
                               │ isApproved       │
                               │ isActive         │
                               └──────────────────┘
                                        │
                    ┌───────────────────┼───────────────────┐
                    │                   │                   │
          (Doctor Role)         (Patient Role)         (Pharmacy Role)
                    │                   │                   │
            ┌───────▼────┐      ┌───────▼────┐      ┌───────▼──────┐
            │Prescriptions│      │Prescriptions│      │  Pharmacies  │
            │(doctor_id)  │      │(patient_id) │      │(profile_id)  │
            └─────┬──────┘      └─────┬──────┘      └──────┬───────┘
                  │                   │                    │
                  │        ┌──────────┘                    │
                  │        │                               │
            ┌─────▼────────▼────────┐            ┌────────▼──────────┐
            │  prescriptions (PK)   │            │ pharmacy_medicines │
            ├───────────────────────┤            ├────────────────────┤
            │ id (PK)               │            │ id (PK)            │
            │ prescription_code     │            │ pharmacy_id (FK)   │
            │ doctor_id (FK)        │            │ medicine_id (FK)   │
            │ patient_id (FK)       │            │ stockQuantity      │
            │ pharmacy_id (FK)      │            │ price              │
            │ status (ENUM)         │            │ lastUpdated        │
            │ issueDate             │            └────────────────────┘
            │ expiryDate            │                    ▲
            │ dispensedDate         │                    │
            │ notes                 │                    │
            └───────────────────────┘              (Join Table - M:M)
                                                         │
                                                  ┌──────▼─────────┐
                                                  │  medicines (PK)│
                                                  ├────────────────┤
                                                  │ id (PK)        │
                                                  │ genericName    │
                                                  │ brandName      │
                                                  │ dosageForm     │
                                                  │ strength       │
                                                  │ requiresRx     │
                                                  └────────────────┘
```

### Relationship Explanations

#### 1. **ONE-TO-ONE: User ↔ Profile (REQUIREMENT 6 - 2 Marks)**

**Mapping:**
- `User.profile` (inverse side) with `@OneToOne(mappedBy = "user")`
- `Profile.user` (owning side) with `@JoinColumn(name = "user_id")`

**Database Schema:**
```sql
-- users table
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'DOCTOR', 'PHARMACY', 'PATIENT')
);

-- profiles table (user_id is the FOREIGN KEY)
CREATE TABLE profiles (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    license_number VARCHAR(50),
    is_approved BOOLEAN DEFAULT false,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

**Logic:**
- Each user account has exactly ONE profile
- Separates authentication (users table) from personal data (profiles table)
- When user is deleted, cascade deletes the profile automatically
- Allows role-specific fields (doctor has different fields than patient)

**Code Example:**
```java
User user = new User("doctor@hospital.rw", "password123", Role.DOCTOR);
Profile profile = new Profile(user, "Jean", "Smith");
profile.setLicenseNumber("DOC-001");
userRepository.save(user);  // Saves both user and profile
```

---

#### 2. **ONE-TO-MANY: Doctor → Prescriptions (REQUIREMENT 5 - 2 Marks)**

**Mapping:**
- `Profile.writes` or role-based: Doctors write many prescriptions
- `Prescription.doctor` with `@ManyToOne` and `@JoinColumn(name = "doctor_id")`

**Database Schema:**
```sql
CREATE TABLE prescriptions (
    id BIGINT PRIMARY KEY,
    prescription_code VARCHAR(50) UNIQUE,
    doctor_id BIGINT NOT NULL,    -- FOREIGN KEY
    patient_id BIGINT NOT NULL,
    pharmacy_id BIGINT,
    status ENUM('ACTIVE', 'DISPENSED', 'EXPIRED', 'CANCELLED'),
    issue_date DATETIME,
    FOREIGN KEY (doctor_id) REFERENCES profiles(id),
    FOREIGN KEY (patient_id) REFERENCES profiles(id),
    FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(id)
);
```

**Logic:**
- One Doctor (Profile with role='DOCTOR') writes MANY prescriptions
- Each prescription belongs to exactly ONE doctor
- Foreign key is in the "many" side (prescriptions table)
- Doctor owns the relationship

**Example Data:**
```
Doctor: Dr. Jane Smith (id=5)
  └── Prescription 1: "RX-2024-0001" (for John Doe)
  └── Prescription 2: "RX-2024-0002" (for Mary Johnson)
  └── Prescription 3: "RX-2024-0003" (for Peter Brown)
```

**Code Example:**
```java
Profile doctor = profileRepository.findById(5L).get();
List<Prescription> prescriptions = prescriptionRepository.findByDoctorId(5L);
// Returns all 3 prescriptions above
```

---

#### 3. **ONE-TO-MANY: Patient → Prescriptions**

**Similar to Doctor relationship:**
- One Patient receives MANY prescriptions
- Foreign key: `patient_id` in prescriptions table
- Query: `prescriptionRepository.findByPatientId(patientId)`

---

#### 4. **MANY-TO-MANY: Pharmacy ↔ Medicine (REQUIREMENT 4 - 3 Marks)**

**Why a Join Table?**
If we used `@ManyToMany` directly, we'd get:
```
CREATE TABLE pharmacy_medicines (
    pharmacy_id BIGINT,
    medicine_id BIGINT
);
```

But in real business, each (pharmacy, medicine) pair needs extra data:
- How many units are in stock?
- What price does THIS pharmacy charge?
- When was it last updated?

**Solution: Create explicit PharmacyMedicine entity**

**Database Schema:**
```sql
CREATE TABLE pharmacy_medicines (
    id BIGINT PRIMARY KEY,
    pharmacy_id BIGINT NOT NULL,    -- FK
    medicine_id BIGINT NOT NULL,    -- FK
    stock_quantity INTEGER,
    price DECIMAL(10,2),
    last_updated DATETIME,
    reorder_level INTEGER,
    status ENUM('ACTIVE', 'OUT_OF_STOCK', 'DISCONTINUED'),
    UNIQUE KEY (pharmacy_id, medicine_id),
    FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(id),
    FOREIGN KEY (medicine_id) REFERENCES medicines(id)
);

-- This creates the many-to-many relationship:
-- Pharmacy 1 --< PharmacyMedicine >-- Medicine 1
--              (join table)
```

**Logic:**
- One Medicine can be stocked in MANY pharmacies
- One Pharmacy stocks MANY medicines
- Join table stores extra data (quantity, price)

**Example Data:**
```
Pharmacies:
  1. City Pharmacy (Kigali)
  2. Central Pharmacy (Kigali)

Medicines:
  1. Paracetamol 500mg
  2. Amoxicillin 250mg

pharmacy_medicines table:
┌────┬──────────────┬──────────────┬─────────────┬─────────┐
│ id │ pharmacy_id  │ medicine_id  │ quantity    │ price   │
├────┼──────────────┼──────────────┼─────────────┼─────────┤
│ 1  │ 1 (City)     │ 1 (Panadol)  │ 100 units   │ 500 RWF │
│ 2  │ 1 (City)     │ 2 (Amoxil)   │ 50 units    │ 1000 RWF│
│ 3  │ 2 (Central)  │ 1 (Panadol)  │ 75 units    │ 480 RWF │
│ 4  │ 2 (Central)  │ 2 (Amoxil)   │ 40 units    │ 950 RWF │
└────┴──────────────┴──────────────┴─────────────┴─────────┘

Query: "Where can I find Paracetamol?"
SELECT p.* FROM pharmacy_medicines pm
JOIN pharmacies p ON pm.pharmacy_id = p.id
WHERE pm.medicine_id = 1;
Result: City Pharmacy (500 RWF), Central Pharmacy (480 RWF)

Query: "What does City Pharmacy stock?"
SELECT m.*, pm.stock_quantity, pm.price
FROM pharmacy_medicines pm
JOIN medicines m ON pm.medicine_id = m.id
WHERE pm.pharmacy_id = 1;
Result: Paracetamol (100 units, 500 RWF), Amoxil (50 units, 1000 RWF)
```

**Code Example:**
```java
// Add medicine to pharmacy inventory
Pharmacy pharmacy = pharmacyRepository.findById(1L).get();
Medicine medicine = medicineRepository.findByGenericName("Paracetamol").get();

PharmacyMedicine stock = new PharmacyMedicine(
    pharmacy,
    medicine,
    100,           // quantity
    new BigDecimal("500.00")  // price
);
pharmacyMedicineRepository.save(stock);

// Find all medicines in pharmacy
Page<PharmacyMedicine> medicines = pharmacyMedicineRepository
    .findByPharmacyId(1L, PageRequest.of(0, 20));
```

---

## REQUIREMENT 2: Implementation of Saving Location - 2 Marks

### How Location Data is Stored

Location is implemented as an **@Embeddable class**, NOT a separate table.

**Code:**
```java
@Embeddable
public class Location {
    private String province;        // "Kigali City"
    private String provinceCode;    // "01"
    private String district;        // "Gasabo"
    private String sector;          // "Kimironko"
    private String cell;
    private String village;
    private Double latitude;        // 1.9441
    private Double longitude;       // 30.0619
    private String streetAddress;
}

@Entity
public class Pharmacy {
    @Embedded
    private Location location;
}
```

**Database Implementation:**
Instead of two tables:
```sql
-- WRONG WAY (two tables):
CREATE TABLE locations (id, province, district);
CREATE TABLE pharmacies (id, name, location_id FK);
```

We use ONE table with embedded columns:
```sql
-- CORRECT WAY (one table):
CREATE TABLE pharmacies (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255),
    location_province VARCHAR(100),
    location_district VARCHAR(100),
    location_sector VARCHAR(100),
    location_latitude DOUBLE,
    location_longitude DOUBLE,
    ...
);
```

**Advantages:**
1. **No JOIN required**: Data is in same table
2. **Faster queries**: Single table scan instead of join
3. **Location always belongs to one pharmacy**: One-to-One semantics enforced
4. **Simpler code**: No need for LocationRepository

### How Relationships are Handled

When we save a pharmacy with location:
```java
Pharmacy p = new Pharmacy();
p.setName("City Pharmacy");
p.setLocation(new Location("Kigali", "Gasabo", "Kimironko", 1.9441, 30.0619));
pharmacyRepository.save(p);

// JPA automatically maps Location fields to "location_*" columns
```

When we query by province:
```java
// This works because Location is embedded!
List<Pharmacy> kigaliPharmacies = pharmacyRepository.findByLocationProvince("Kigali");

// Generated SQL (NO JOIN!):
// SELECT * FROM pharmacies WHERE location_province = 'Kigali'
```

---

## REQUIREMENT 3: Sorting & Pagination - 5 Marks

### What is Pagination?

**Problem:** Loading millions of records into memory crashes the server

**Solution:** Load only one "page" at a time
```
All Medicines: [M1, M2, M3, ... M1000]
     ↓↓↓ Pagination ↓↓↓
Page 0 (10 items): [M1-M10]
Page 1 (10 items): [M11-M20]
Page 2 (10 items): [M21-M30]
```

### How Pagination Works in Spring Data JPA

**Repository Method:**
```java
Page<Medicine> findByGenericNameContainingIgnoreCase(String name, Pageable pageable);
```

**Controller Implementation:**
```java
@GetMapping("/search")
public Page<Medicine> searchMedicines(
    @RequestParam String name,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "name,asc") String[] sort
) {
    // Create Pageable from request parameters
    Sort.Direction direction = Sort.Direction.ASC;
    String sortField = "name";
    
    if (sort[0].contains(",")) {
        String[] parts = sort[0].split(",");
        sortField = parts[0];
        direction = Sort.Direction.fromString(parts[1].toUpperCase());
    }
    
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
    
    // This returns Page<Medicine> with:
    // - content: List of 10 medicines
    // - totalElements: 1234 (total count)
    // - totalPages: 124 (1234 / 10)
    // - number: 0 (current page)
    return medicineRepository.findByGenericNameContainingIgnoreCase(name, pageable);
}
```

**API Usage Examples:**

```bash
# Page 1: First 10 medicines
GET /api/medicines/search?name=Paracet&page=0&size=10

# Response:
{
  "content": [
    { "id": 1, "genericName": "Paracetamol" },
    { "id": 2, "genericName": "Paracetamol Plus" },
    ... (8 more)
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 234,
  "totalPages": 24,
  "first": true,
  "last": false
}

# Page 2: Next 10 medicines (sorted by strength)
GET /api/medicines/search?name=Paracet&page=1&size=10&sort=strength,desc

# Get page with 20 items per page
GET /api/medicines/search?name=Paracet&page=0&size=20
```

### How Sorting Works

**Sorting different fields:**
```bash
# Sort by name ascending
GET /api/medicines?sort=name,asc

# Sort by strength descending  
GET /api/medicines?sort=strength,desc

# Multiple sorts
GET /api/medicines?sort=name,asc&sort=createdAt,desc
```

**Generated SQL:**
```sql
-- For: GET /api/medicines?sort=name,asc&page=0&size=10
SELECT * FROM medicines
ORDER BY name ASC
LIMIT 10 OFFSET 0;

-- For: GET /api/medicines?sort=strength,desc&page=1&size=20
SELECT * FROM medicines
ORDER BY strength DESC
LIMIT 20 OFFSET 20;
```

### Performance Benefits

| Metric | Without Pagination | With Pagination (10 items) |
|--------|-------------------|---------------------------|
| Memory Usage | 1,234 objects × 2KB = ~2.5MB | 10 objects × 2KB = ~20KB |
| Network Bandwidth | 2.5MB transfer | 20KB transfer |
| Database Fetch | Fetch 1,234 rows | Fetch 10 rows |
| Response Time | ~2 seconds | ~50ms |
| Scalability | Breaks at ~10K records | Works with millions |

---

## REQUIREMENT 7: existsBy() Method - 2 Marks

### What is existsBy()?

Efficient way to check if data exists WITHOUT loading the entire object.

**SQL generated:**
```sql
-- Instead of loading full object:
SELECT * FROM users WHERE email = 'user@example.com';

-- existsBy() generates more efficient:
SELECT COUNT(*) > 0 FROM users WHERE email = 'user@example.com';
```

### Repository Methods

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Method name tells Spring what to generate
    boolean existsByEmail(String email);
}

@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {
    boolean existsByLicenseNumber(String licenseNumber);
}

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    boolean existsByGenericName(String genericName);
}

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    boolean existsByPrescriptionCode(String code);
}
```

### Usage in Services

```java
@Service
public class RegistrationService {
    
    @Autowired
    private UserRepository userRepository;
    
    public User registerUser(User user) {
        // REQUIREMENT 7: Check existence first
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateEmailException("Email already registered!");
        }
        return userRepository.save(user);
    }
}

@Service
public class PharmacyService {
    
    @Autowired
    private PharmacyRepository pharmacyRepository;
    
    public boolean canRegisterPharmacy(String licenseNumber) {
        // Quick check without loading entire pharmacy
        if (pharmacyRepository.existsByLicenseNumber(licenseNumber)) {
            return false;  // Already registered
        }
        return true;
    }
}
```

---

## REQUIREMENT 8: Find Users by Province - 4 Marks

### The Requirement

"Retrieve all users from a given province using **province code OR province name**"

### Implementation

**Repository Methods:**
```java
@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {
    
    // Method 1: Find by province NAME
    List<Pharmacy> findByLocationProvince(String province);
    
    // Method 2: Find by province CODE  
    List<Pharmacy> findByLocationProvinceCode(String provinceCode);
    
    // Method 3: Find by BOTH (flexible query)
    @Query("SELECT p FROM Pharmacy p WHERE " +
           "p.location.province = :province OR " +
           "p.location.provinceCode = :province")
    List<Pharmacy> findByProvinceFlexible(@Param("province") String province);
    
    // With pagination (REQUIREMENT 3)
    Page<Pharmacy> findByProvinceFlexible(String province, Pageable pageable);
}
```

**Controller Implementation:**
```java
@GetMapping("/by-province")
public ResponseEntity<?> getPharmaciesByProvince(
    @RequestParam(required = false) String provinceName,
    @RequestParam(required = false) String provinceCode,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Pharmacy> result;
    
    // REQUIREMENT 8: Allow BOTH province name AND code
    if (provinceName != null && !provinceName.isEmpty()) {
        // User provided province name: "Kigali", "Eastern Province"
        result = pharmacyRepository.findByLocationProvince(provinceName, pageable);
    } else if (provinceCode != null && !provinceCode.isEmpty()) {
        // User provided province code: "01", "02"
        result = pharmacyRepository.findByLocationProvinceCode(provinceCode, pageable);
    } else {
        return ResponseEntity.badRequest()
            .body("Provide 'provinceName' or 'provinceCode'");
    }
    
    return ResponseEntity.ok(result);
}
```

**Query Logic Explanation:**

For Province NAME:
```sql
-- Query: findByLocationProvince("Kigali")
SELECT * FROM pharmacies WHERE location_province = 'Kigali' LIMIT 10 OFFSET 0;

Result: All pharmacies with location_province = 'Kigali'
```

For Province CODE:
```sql
-- Query: findByLocationProvinceCode("01")
SELECT * FROM pharmacies WHERE location_province_code = '01' LIMIT 10 OFFSET 0;

Result: All pharmacies with location_province_code = '01'
```

### API Usage Examples

```bash
# Get pharmacies in Kigali (by name)
GET /api/pharmacies/by-province?provinceName=Kigali&page=0&size=10

# Get pharmacies in province code 01 (by code)
GET /api/pharmacies/by-province?provinceCode=01&page=0&size=10

# Get pharmacies in Eastern Province (by name)
GET /api/pharmacies/by-province?provinceName=Eastern%20Province&page=0&size=10

# Get combined with pagination
GET /api/pharmacies/by-province?provinceName=Kigali&page=2&size=20
```

---

## Quick Reference: All 7+ Tables

| Table | Purpose | Key Relationships |
|-------|---------|-------------------|
| **users** | Authentication (email, password) | 1:1 with profiles |
| **profiles** | User details (name, license) | 1:1 with users; 1:N prescriptions |
| **pharmacies** | Pharmacy info + embedded location | 1:N with prescriptions; M:M with medicines |
| **medicines** | Drug master list | M:M with pharmacies |
| **pharmacy_medicines** | Join table (inventory) | M:1 to pharmacies; M:1 to medicines |
| **prescriptions** | Prescription records | M:1 to doctor; M:1 to patient; M:1 to pharmacy |

---

## Testing & Validation (REQUIREMENT 9 - Topics)

### What Students Should Know for Viva-Voce

1. **ERD Explanation**: Understand each table, column, and relationship
2. **One-to-One**: How User and Profile are linked
3. **One-to-Many**: How Doctor writes multiple Prescriptions
4. **Many-to-Many**: Why join table needed for Pharmacy ↔ Medicine
5. **Embedding**: How Location data is stored without separate table
6. **Pagination**: Why split large datasets into pages
7. **Sorting**: How to order results efficiently
8. **existsBy()**: Purpose and efficiency vs full queries
9. **Province Search**: Why both code and name support needed

---

## Next Steps: Running & Testing

See the separate TESTING_GUIDE.md for:
- API endpoint testing with Postman
- Database verification queries
- Performance monitoring
- Sample test data scenarios

