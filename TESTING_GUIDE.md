# MediConnect RW - Complete Testing & Implementation Guide

## Quick Start: Build & Run

### 1. Build the project
```bash
mvn clean package
```

### 2. Run the application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

---

## TESTING ALL REQUIREMENTS

This guide provides step-by-step instructions to test each requirement. You can use:
- **Postman** (GUI tool) - Easiest for beginners
- **cURL** (Command line)
- **Browser** - For GET requests only

---

## REQUIREMENT 1: ERD with 5+ Tables (3 Marks)

### What to Verify
✓ 7 tables created in database  
✓ All relationships properly mapped  
✓ All foreign keys present  

### How to Check
After running the app, the database will auto-create all tables.

**Check H2 Database Console:**
1. Open browser: http://localhost:8080/h2-console
2. Login (default settings)
3. See all tables created:
   - users
   - profiles
   - pharmacies
   - medicines
   - pharmacy_medicines
   - prescriptions
   - stock (legacy)

### What Your Lecturer Will Ask
"Explain the ERD. How many tables? What are the relationships?"

**Answer Template:**
"We have 7 tables following a normalized design:
1. Users (1:1) Profiles - Store auth vs personal data separately
2. Profiles (1:N) Prescriptions - Doctor writes many prescriptions
3. Pharmacies (M:N) Medicines - Via PharmacyMedicine join table
4. Pharmacies have embedded Location - No separate table
The design follows database normalization and avoids data redundancy."

---

## REQUIREMENT 2: Location Implementation (2 Marks)

### What You're Demonstrating
- Location is embedded (no separate table)
- Location data stored in pharmacy table with "location_" prefix
- Can query by province/district without JOINs

### Test 1: Save a Pharmacy with Location

**API Endpoint:** `POST /api/pharmacies`

**Postman Instructions:**
1. Open Postman
2. Method: POST
3. URL: `http://localhost:8080/api/pharmacies`
4. Headers: `Content-Type: application/json`
5. Body (JSON):
```json
{
  "name": "City Pharmacy",
  "email": "city@pharmacy.rw",
  "phoneNumber": "+250788123456",
  "licenseNumber": "PHARM-001",
  "location": {
    "province": "Kigali",
    "provinceCode": "01",
    "district": "Gasabo",
    "sector": "Kimironko",
    "latitude": 1.9441,
    "longitude": 30.0619,
    "streetAddress": "KN 2 St, Kigali"
  }
}
```

6. Send
7. Expected Response (201 Created):
```json
{
  "id": 1,
  "name": "City Pharmacy",
  "location": {
    "province": "Kigali",
    "district": "Gasabo",
    "sector": "Kimironko",
    "latitude": 1.9441,
    "longitude": 30.0619
  }
}
```

### Test 2: Verify Location in Database

**Using H2 Console:**
```sql
-- Check pharmacy table structure
DESC pharmacies;

-- You should see columns:
-- - location_province VARCHAR(100)
-- - location_district VARCHAR(100)
-- - location_sector VARCHAR(100)
-- - location_latitude DOUBLE
-- - location_longitude DOUBLE

-- Verify the data was saved
SELECT id, name, location_province, location_district, location_sector 
FROM pharmacies;

-- Result should show:
-- | 1 | City Pharmacy | Kigali | Gasabo | Kimironko |
```

### REQUIREMENT 2 Explanation for Your Lecturer

"Location is implemented as an **@Embeddable class**, not a separate entity. This means:
- Location data is stored directly in the pharmacy table
- No separate locations table created
- Saves a JOIN operation when querying by location
- Location logically belongs to ONE pharmacy (enforcement at ORM level)
- Faster queries and simpler data model"

---

## REQUIREMENT 3: Pagination & Sorting (5 Marks)

### Test 1: Basic Pagination

**Setup:** First, add several medicines to the database

**API Endpoint:** `GET /api/medicines?page=0&size=5`

**Postman:**
1. Method: GET
2. URL: `http://localhost:8080/api/medicines?page=0&size=5`
3. Send
4. See response with:
   - content: [5 medicines]
   - totalElements: (total count)
   - totalPages: (calculated)

**Using cURL:**
```bash
curl -X GET "http://localhost:8080/api/medicines?page=0&size=5"
```

### Test 2: Sorting

**API Endpoint:** `GET /api/medicines?page=0&size=10&sort=name,asc`

**Postman URL:** `http://localhost:8080/api/medicines?page=0&size=10&sort=name,asc`

**Response:** Medicines sorted alphabetically by name

### Test 3: Pagination with Sorting

**API Endpoint:** `GET /api/medicines?page=0&size=5&sort=strength,desc`

Combined: Get 5 medicines, sorted by strength (highest first), first page

### Create Test Medicines First

**API Endpoint:** `POST /api/medicines`

**Request Body:**
```json
{
  "genericName": "Paracetamol",
  "brandName": "Panadol",
  "description": "Pain reliever",
  "dosageForm": "Tablet",
  "strength": "500mg",
  "requiresPrescription": false
}
```

Repeat for:
- Amoxicillin 250mg
- Ibuprofen 400mg
- Aspirin 100mg
- Metformin 500mg

Then test pagination on these 5+ medicines

### REQUIREMENT 3 Performance Explanation

"**Pagination:**
- Without:  SELECT all 1,234 medicines → Load into memory → Send to client (slow!)
- With:    SELECT 10 medicines LIMIT 10 OFFSET 0 → Fast response!

**Benefits:**
- Memory: From 2.5MB to 20KB
- Speed: From 2s to 50ms
- Scalability: Works with millions of records

**Sorting:**
- Offload to database (optimal)
- Database optimizes with indexes
- Client doesn't sort in code (faster)"

---

## REQUIREMENT 4: Many-to-Many Relationship (3 Marks)

### Understanding Many-to-Many

**Real-world scenario:**
- Pharmacy 'City' stocks medicines: Paracetamol (100 units, 500 RWF), Amoxil (50 units, 1000 RWF)
- Pharmacy 'Central' stocks medicines: Paracetamol (75 units, 480 RWF), Amoxil (40 units, 950 RWF)
- Medicine 'Paracetamol' is in: City, Central, Downtown (3 different pharmacies)

### Database View

**pharmacy_medicines Table (Join Table):**
```
┌────┬──────────────┬──────────────┬─────────────┬────────┐
│ id │ pharmacy_id  │ medicine_id  │ quantity    │ price  │
├────┼──────────────┼──────────────┼─────────────┼────────┤
│ 1  │ 1 (City)     │ 1 (Panadol)  │ 100 units   │ 500 RWF│
│ 2  │ 1 (City)     │ 2 (Amoxil)   │ 50 units    │ 1000 RWF
│ 3  │ 2 (Central)  │ 1 (Panadol)  │ 75 units    │ 480 RWF│
│ 4  │ 2 (Central)  │ 2 (Amoxil)   │ 40 units    │ 950 RWF│
└────┴──────────────┴──────────────┴─────────────┴────────┘
```

### Test: Add Medicine to Pharmacy Inventory

**Prerequisites:**
1. Create a pharmacy (from REQUIREMENT 2 test)
2. Create a medicine

**API Endpoint:** `POST /api/pharmacies/{pharmacyId}/medicines`

**Postman:**
1. Method: POST
2. URL: `http://localhost:8080/api/pharmacies/1/medicines`
3. Body:
```json
{
  "medicine": {
    "id": 1
  },
  "stockQuantity": 100,
  "price": "500.00",
  "reorderLevel": 20,
  "status": "ACTIVE"
}
```

4. Send
5. Response: PharmacyMedicine record created

**Verify in H2 Console:**
```sql
SELECT * FROM pharmacy_medicines;

-- Should show:
-- | 1 | 1 | 1 | 100 | 500.00 | ACTIVE |
```

### Test: Get All Medicines in a Pharmacy

**API Endpoint:** `GET /api/pharmacies/1/medicines?page=0&size=10`

**Response:** List of PharmacyMedicine objects for pharmacy 1

### REQUIREMENT 4 Explanation

"**Many-to-Many Relationship:**

The join table (pharmacy_medicines) allows:
1. One pharmacy to stock many medicines
2. One medicine to be found in many pharmacies
3. Each relationship stores extra data: quantity and price

This is more efficient than a simple junction table because:
- We can query: 'Where is Paracetamol available and at what price?'
- We can track: 'What medicines does City Pharmacy stock?'
- We avoid: Redundant pricing (each pharmacy sets own price)"

---

## REQUIREMENT 5: One-to-Many Relationship (2 Marks)

### Understanding One-to-Many

**Doctor → Prescriptions (One-to-Many)**
- One Doctor writes MANY prescriptions
- Foreign key in prescriptions table: doctor_id

### Test: Create a Prescription

**Prerequisites:**
1. Create User with role=DOCTOR (this automatically creates Profile)
2. Create another User with role=PATIENT
3. Create a Pharmacy

**Step 1: Create Doctor User**

**API Endpoint:** `POST /api/users`

```json
{
  "email": "dr.jane@hospital.rw",
  "password": "secure123",
  "role": "DOCTOR"
}
```

This creates user_id=1. The profile is auto-created.

**Step 2: Create Patient User**

```json
{
  "email": "john@patient.rw",
  "password": "secure123",
  "role": "PATIENT"
}
```

This creates user_id=2. The profile is auto-created.

**Step 3: Create Prescription**

**API Endpoint:** `POST /api/prescriptions`

```json
{
  "prescriptionCode": "RX-2024-0001",
  "doctor": { "id": 1 },
  "patient": { "id": 2 },
  "pharmacy": { "id": 1 },
  "status": "ACTIVE",
  "notes": "Take 2 tablets twice daily with food"
}
```

### Test: Get All Prescriptions by Doctor

**API Endpoint:** `GET /api/prescriptions/doctor/1?page=0&size=10`

**Response:** All prescriptions written by doctor with id=1

**SQL Generated:**
```sql
SELECT * FROM prescriptions 
WHERE doctor_id = 1 
LIMIT 10 OFFSET 0;
```

### REQUIREMENT 5 Explanation

"**One-to-Many Relationship:**

Doctor (Profile) has ONE-TO-MANY relationship with Prescriptions:
- doctor_id is foreign key in prescriptions table
- Points to profiles.id (the doctor's profile)
- One doctor can have many prescriptions
- Each prescription belongs to exactly one doctor

This relationship allows:
- Query: 'Show all prescriptions written by Dr. Jane'
- Validation: 'Delete doctor only if no prescriptions exist'
- Audit: 'Report prescriptions per doctor'"

---

## REQUIREMENT 6: One-to-One Relationship (2 Marks)

### Understanding One-to-One

**User ↔ Profile**
- Each User account has exactly ONE Profile
- Foreign key in profiles table: user_id (unique constraint)

### Test: Create User and Profile

**API Endpoint:** `POST /api/users`

```json
{
  "email": "alice@hospital.rw",
  "password": "password123",
  "role": "ADMIN"
}
```

**Response:**
```json
{
  "id": 1,
  "email": "alice@hospital.rw",
  "role": "ADMIN",
  "profile": null  // Profile will be created separately or auto-created
}
```

**Get User with Profile:**

**API Endpoint:** `GET /api/users/1`

**Response:** User with embedded profile data

### Database Verification

**H2 Console:**
```sql
-- Check foreign key
SELECT u.id, u.email, p.id, p.first_name, p.user_id
FROM users u
LEFT JOIN profiles p ON u.id = p.user_id;

-- Should show:
-- | 1 | alice@hospital.rw | 1 | Alice | 1 |
```

### REQUIREMENT 6 Explanation

"**One-to-One Relationship:**

User and Profile are linked 1:1:
- User table: Stores email, password (authentication)
- Profile table: Stores firstName, lastName, license (personal data)
- Link: profile.user_id → users.id (unique constraint ensures 1:1)

Why separate tables?
- Security: Authentication separate from personal data
- Flexibility: Profile can have role-specific fields
- Clean design: Single Responsibility Principle"

---

## REQUIREMENT 7: existsBy() Method (2 Marks)

### Purpose

Efficiently check if data exists before creating duplicates

### Test 1: Check Email Already Registered

**Scenario:** User tries to register with existing email

**API Endpoint:** `POST /api/users/register`

```json
{
  "email": "alice@hospital.rw",
  "password": "password123",
  "role": "DOCTOR"
}
```

**Expected:** Error message "Email already registered"

**Under the Hood:**
```java
if (userRepository.existsByEmail("alice@hospital.rw")) {
    throw new DuplicateEmailException("Email already registered!");
}
```

**SQL Generated:**
```sql
SELECT COUNT(*) > 0 FROM users WHERE email = 'alice@hospital.rw';
-- Returns: true (email exists)
```

### Test 2: Check License Number

**API Endpoint:** Check before approving pharmacy

**Request:** Verify pharmacy license is unique

```java
// In PharmacyService
public void approvPharmacy(Pharmacy pharmacy) {
    if (pharmacyRepository.existsByLicenseNumber(pharmacy.getLicenseNumber())) {
        throw new DuplicateLicenseException("License already used!");
    }
    pharmacy.setIsApproved(true);
    pharmacyRepository.save(pharmacy);
}
```

### REQUIREMENT 7 Explanation

"**existsBy() Method:**

Spring Data JPA generates existence-checking methods from method names:
- existsByEmail(email) - Returns boolean, doesn't load object
- existsByLicenseNumber(license) - Also doesn't load object
- existsByPrescriptionCode(code) - Etc.

This is efficient because:
- SQL: COUNT(*) > 0 is faster than SELECT *
- Memory: Don't load entire object
- Use case: Pre-validation before INSERT"

---

## REQUIREMENT 8: Find by Province (4 Marks)

### The Requirement

"Retrieve all **users** (pharmacies) from a given province using **province code OR province name**"

### Location Data Structure

```
Provinces in Rwanda:
  - Kigali (code: 01)
  - Southern Province (code: 02)
  - Western Province (code: 03)
  - Northern Province (code: 04)
  - Eastern Province (code: 05)
```

### Test 1: Search by Province NAME

**Add Multiple Pharmacies First:**

```json
{
  "name": "Kigali Downtown",
  "location": {
    "province": "Kigali",
    "provinceCode": "01",
    "district": "Kicukiro",
    "sector": "Kimisagara"
  }
}
```

```json
{
  "name": "Muhima Pharmacy",
  "location": {
    "province": "Kigali",
    "provinceCode": "01",
    "district": "Gasabo",
    "sector": "Gisozi"
  }
}
```

```json
{
  "name": "Karongi Clinic",
  "location": {
    "province": "Western Province",
    "provinceCode": "03",
    "district": "Karongi",
    "sector": "Muruta"
  }
}
```

**API Endpoint:** `GET /api/pharmacies/by-province?provinceName=Kigali`

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Kigali Downtown",
      "location": { "province": "Kigali", ... }
    },
    {
      "id": 2,
      "name": "Muhima Pharmacy",
      "location": { "province": "Kigali", ... }
    }
  ],
  "totalElements": 2,
  "totalPages": 1
}
```

**SQL Generated:**
```sql
SELECT * FROM pharmacies 
WHERE location_province = 'Kigali'
LIMIT 10 OFFSET 0;
```

### Test 2: Search by Province CODE

**API Endpoint:** `GET /api/pharmacies/by-province?provinceCode=01`

**Same result as above** (province code 01 = Kigali)

### Test 3: Search with Pagination

**API Endpoint:** `GET /api/pharmacies/by-province?provinceName=Kigali&page=0&size=10`

Returns first 10 pharmacies in Kigali

### REQUIREMENT 8 Explanation

"**Find by Province:**

The system supports TWO ways to search by province:
1. **By Province Name**: 'Kigali', 'Eastern Province'
   - User-friendly
   - findByLocationProvince(provinceName)
2. **By Province Code**: '01', '02', '03'
   - System-friendly (APIs, databases)
   - findByLocationProvinceCode(provinceCode)

Both methods:
- Query the embedded location_* columns (no JOIN)
- Support pagination (REQUIREMENT 3)
- Support sorting

**Use case:**
- Mobile app dropdown: Shows province names
- API backend: Uses codes for efficiency
- Report: Uses either based on context"

---

## Database Query Examples (With Screenshots)

### H2 Console Access

1. Open: `http://localhost:8080/h2-console`
2. Driver: `org.h2.Driver`
3. JDBC URL: `jdbc:h2:mem:testdb`
4. User: `sa`
5. Password: (leave blank)
6. Click Connect

### Example Queries

**Show all tables:**
```sql
SHOW TABLES;
```

**Count medicines per pharmacy:**
```sql
SELECT 
    p.name,
    COUNT(pm.id) as medicine_count
FROM pharmacies p
LEFT JOIN pharmacy_medicines pm ON p.id = pm.pharmacy_id
GROUP BY p.id, p.name;
```

**Find medicines by pharmacy location:**
```sql
SELECT 
    m.generic_name,
    pm.stock_quantity,
    pm.price,
    p.name as pharmacy_name,
    p.location_province
FROM pharmacy_medicines pm
JOIN medicines m ON pm.medicine_id = m.id
JOIN pharmacies p ON pm.pharmacy_id = p.id
WHERE p.location_province = 'Kigali'
ORDER BY m.generic_name;
```

**Prescriptions per doctor:**
```sql
SELECT 
    pr.first_name,
    pr.last_name,
    COUNT(p.id) as prescription_count
FROM profiles pr
LEFT JOIN prescriptions p ON pr.id = p.doctor_id
WHERE pr.user_id IN (
    SELECT id FROM users WHERE role = 'DOCTOR'
)
GROUP BY pr.id, pr.first_name, pr.last_name;
```

---

## Common Issues & Troubleshooting

### Issue: HTTP 404 Not Found

**Cause:** Wrong endpoint URL
**Solution:**
- Check the exact endpoint path
- Verify request method (GET, POST, etc.)
- Check path parameters

### Issue: HTTP 400 Bad Request

**Cause:** Missing or wrong request body
**Solution:**
- Verify JSON structure
- Check required fields
- Use proper data types

### Issue: HTTP 409 Conflict

**Cause:** Constraint violation (e.g., duplicate email)
**Solution:**
- Verify data uniqueness
- Check for duplicates before creation

### Issue: Pages are empty

**Cause:** No data in database
**Solution:**
- Add test data first
- Create medicines, pharmacies before querying
- Check database via H2 console

---

## Viva-Voce Preparation (REQUIREMENT 9 - 7 Marks)

### Questions You Should Be Able to Answer

1. **"Why do we have both users and profiles tables?"**
   - Answer: Separation of concerns. Users table for authentication (email/password), profiles for personal data. Allows role-specific fields.

2. **"What is the relationship between User and Profile?"**
   - Answer: One-to-One. Each user has exactly one profile. Mapped with user_id foreign key in profiles table.

3. **"Explain the many-to-many relationship between Pharmacy and Medicine."**
   - Answer: We created PharmacyMedicine join table. This allows storing extra data (price, quantity). One medicine in many pharmacies, one pharmacy stocks many medicines.

4. **"Why is Location an Embedded object?"**
   - Answer: Location belongs to ONE pharmacy only. Embedding avoids unnecessary table joins. Faster queries, simpler design.

5. **"What is pagination and why does it matter?"**
   - Answer: Splitting large result sets into pages. Improves performance (memory, speed, scalability). Instead of loading 10,000 records, load 10 at a time.

6. **"How does existsBy() work?"**
   - Answer: Spring generates efficient existence-checking methods. Uses COUNT(*) > 0 instead of loading full objects. Prevents duplicates.

7. **"Explain the province search system."**
   - Answer: Support both province name (Kigali) and code (01). Queries embedded location data directly without JOINs. Can combine with pagination.

---

## Summary Checklist

- [ ] REQUIREMENT 1: Created 7 tables with proper relationships
- [ ] REQUIREMENT 2: Location is embedded and can be queried
- [ ] REQUIREMENT 3: Pagination & sorting tested on medicines endpoint
- [ ] REQUIREMENT 4: Many-to-many tested with pharmacy medicines
- [ ] REQUIREMENT 5: One-to-many tested with prescriptions by doctor
- [ ] REQUIREMENT 6: One-to-one tested with user-profile
- [ ] REQUIREMENT 7: existsBy() works to check duplicates
- [ ] REQUIREMENT 8: Province search works by name AND code with pagination
- [ ] REQUIREMENT 9: Can explain all concepts for viva-voce

---

## Grade Expectation

If all tests pass:
- ERD Diagram: 3 marks ✓
- Location Implementation: 2 marks ✓
- Pagination & Sorting: 5 marks ✓
- Many-to-Many: 3 marks ✓
- One-to-Many: 2 marks ✓
- One-to-One: 2 marks ✓
- existsBy(): 2 marks ✓
- Province Search: 4 marks ✓
- Viva-Voce (clear explanation): 7 marks ✓

**Total: 30 Marks ✓**

