# 🏥 MediConnect RW

<p align="center">
  <img src="https://img.shields.io/badge/Java-17+-blue?style=flat&logo=java" alt="Java">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2-green?style=flat&logo=spring" alt="Spring Boot">
  <img src="https://img.shields.io/badge/PostgreSQL-15+-blue?style=flat&logo=postgresql" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=flat" alt="License">
</p>

A healthcare management REST API for managing patients, doctors, prescriptions, and medications in Rwandan healthcare facilities.

## ✨ Features

- 👨‍⚕️ **Doctor Management** - Register and manage doctors with specialization
- 🏥 **Patient Management** - Patient records with search & pagination  
- 💊 **Prescription System** - Create prescriptions with multiple medications (Many-to-Many)
- 💉 **Medication Inventory** - Drug management with dosage tracking
- 🗺️ **Location/Province** - Rwanda provinces management

## 🗄️ Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         LOCATION                                 │
│  id | name | code | locationType | parent_id (self-ref)         │
│                                                                   │
│  Province (KGL) → District (GSB) → Sector (KMR)                 │
└───────────────────────┬─────────────────────────────────────────┘
                        │ One-to-Many
           ┌────────────┴────────────┐
           ▼                         ▼
┌──────────────────┐       ┌──────────────────────┐
│     PATIENT      │       │       DOCTOR          │
│  id | firstName  │       │  id | firstName       │
│  lastName | email│       │  lastName | email     │
│  nationalId      │       │  licenseNumber        │
│  location_id(FK) │       │  specialization       │
└────────┬─────────┘       │  location_id(FK)      │
         │                 └──────────┬────────────┘
         │ One-to-Many               │ One-to-Many
         └──────────┬────────────────┘
                    ▼
         ┌──────────────────────┐
         │     PRESCRIPTION     │
         │  id | prescNumber    │
         │  patient_id(FK)      │
         │  doctor_id(FK)       │
         │  issueDate           │
         │  expiryDate          │
         │  diagnosis | notes   │
         └──────────┬───────────┘
                    │ Many-to-Many
                    ▼
    ┌───────────────────────────────┐
    │   prescription_medications    │  ← Join Table
    │  prescription_id|medication_id│
    └───────────────────────────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │      MEDICATION      │
         │  id | name | code    │
         │  dosageForm          │
         │  strength            │
         │  manufacturer        │
         └──────────────────────┘
```

### Relationships Summary

| Relationship | Entities | Type |
|---|---|---|
| Location → Location | Self-referencing hierarchy | One-to-Many |
| Location → Patient | Province/District/Sector → Patient | One-to-Many |
| Location → Doctor | Province/District/Sector → Doctor | One-to-Many |
| Patient → Prescription | Patient has many prescriptions | One-to-Many |
| Doctor → Prescription | Doctor writes many prescriptions | One-to-Many |
| Prescription ↔ Medication | Via `prescription_medications` join table | Many-to-Many |

---
## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 3.2 |
| Language | Java 17 |
| Database | PostgreSQL |
| ORM | Hibernate / Spring Data JPA |
| Build | Apache Maven |

## 🚀 Quick Start

```bash
# Clone & build
git clone https://github.com/AgapegifT/midterm_27320_grp-C.git
cd midterm_27320_grp-C/mediconnect
./mvnw clean package

# Run
java -jar target/mediconnect-1.0.0.jar
```

**API Base URL:** `http://localhost:8080/api`


```
LOCATION ───< PATIENT      LOCATION ───< DOCTOR
    │                         │
    └─────────────────────────┼───────< PRESCRIPTION >───────< MEDICATION
                              │              │
                              └──────────────┴─ prescription_medications
```

## 🌐 API Endpoints

### 📍 Location Endpoints

| Method | URL | Description |
|---|---|---|
| POST | `/api/locations` | Create a location |
| GET | `/api/locations?page=0&size=10&sortBy=name` | Get all (paginated) |
| GET | `/api/locations/{id}` | Get by ID |
| GET | `/api/locations/province/{code}` | Get by province code |
| GET | `/api/locations/exists/code/{code}` | Check if code exists |

### 👤 Patient Endpoints

| Method | URL | Description |
|---|---|---|
| POST | `/api/patients` | Create a patient |
| GET | `/api/patients?page=0&size=10&sortBy=lastName&sortDir=asc` | Get all (paginated + sorted) |
| GET | `/api/patients/{id}` | Get by ID |
| GET | `/api/patients/province/code/{code}` | ⭐ Get by province code |
| GET | `/api/patients/province/name/{name}` | ⭐ Get by province name |
| GET | `/api/patients/exists/email/{email}` | Check if email exists |

### 👨‍⚕️ Doctor Endpoints

| Method | URL | Description |
|---|---|---|
| POST | `/api/doctors` | Create a doctor |
| GET | `/api/doctors?page=0&size=10` | Get all (paginated) |
| GET | `/api/doctors/specialization/{spec}` | Get by specialization |
| GET | `/api/doctors/province/code/{code}` | Get by province |

### 💊 Medication Endpoints

| Method | URL | Description |
|---|---|---|
| POST | `/api/medications` | Create a medication |
| GET | `/api/medications?page=0&size=10` | Get all (paginated) |
| GET | `/api/medications/{id}` | Get by ID |
| GET | `/api/medications/exists/code/{code}` | Check if code exists |

### 📋 Prescription Endpoints

| Method | URL | Description |
|---|---|---|
| POST | `/api/prescriptions` | Create a prescription |
| GET | `/api/prescriptions?page=0&size=10` | Get all (paginated) |
| POST | `/api/prescriptions/{id}/medications/{medId}` | ⭐ Add medication (Many-to-Many) |
| DELETE | `/api/prescriptions/{id}/medications/{medId}` | Remove medication |
| GET | `/api/prescriptions/medication/{medId}` | Get by medication |

---

## 🧪 Testing with Postman

### Base URL
```
http://localhost:8080/api
```

> ⚠️ Always set Body → **raw** → **JSON** in Postman for POST requests.

### Complete Test Sequence

#### 1️⃣ Create Province
```json
POST /api/locations
{
  "name": "Kigali",
  "code": "KGL",
  "locationType": "PROVINCE"
}
```

#### 2️⃣ Create District
```json
POST /api/locations
{
  "name": "Gasabo",
  "code": "GSB",
  "locationType": "DISTRICT",
  "parent": { "id": 1 }
}
```

#### 3️⃣ Create Sector
```json
POST /api/locations
{
  "name": "Kimironko",
  "code": "KMR",
  "locationType": "SECTOR",
  "parent": { "id": 2 }
}
```

#### 4️⃣ Create Doctor
```json
POST /api/doctors
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@hospital.rw",
  "phoneNumber": "0788000001",
  "licenseNumber": "LIC001",
  "specialization": "Cardiology",
  "location": { "id": 2 }
}
```

#### 5️⃣ Create Patient
```json
POST /api/patients
{
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@gmail.com",
  "phoneNumber": "0788000002",
  "nationalId": "1199870012345678",
  "dateOfBirth": "1998-05-15",
  "location": { "id": 3 }
}
```

#### 6️⃣ Create Medication
```json
POST /api/medications
{
  "name": "Amoxicillin",
  "code": "AMOX001",
  "dosageForm": "Tablet",
  "strength": "500mg",
  "manufacturer": "RwandaPharma"
}
```

#### 7️⃣ Create Prescription
```json
POST /api/prescriptions
{
  "prescriptionNumber": "RX20260001",
  "issueDate": "2026-03-17",
  "expiryDate": "2026-04-17",
  "diagnosis": "Bacterial Infection",
  "notes": "Take after meals",
  "patient": { "id": 1 },
  "doctor": { "id": 1 }
}
```

#### 8️⃣ Add Medication to Prescription — Many-to-Many ⭐
```
POST /api/prescriptions/1/medications/1
(No body required)
```

#### 9️⃣ Pagination & Sorting ⭐
```
GET /api/patients?page=0&size=10&sortBy=lastName&sortDir=asc
```

#### 🔟 existsBy() Check ⭐
```
GET /api/patients/exists/email/jane.smith@gmail.com
```
Expected: `{"exists": true}`

#### 1️⃣1️⃣ Province Query by Code ⭐⭐
```
GET /api/patients/province/code/KGL
```

#### 1️⃣2️⃣ Province Query by Name ⭐⭐
```
GET /api/patients/province/name/Kigali
```

---

## ⚙️ Key Implementations

### 🔁 Self-Referencing Location Hierarchy

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_id")
private Location parent;

@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
@JsonIgnore
private List<Location> children = new ArrayList<>();
```

### 📄 Pagination & Sorting

```java
Sort sort = sortDir.equalsIgnoreCase("asc")
    ? Sort.by(sortBy).ascending()
    : Sort.by(sortBy).descending();
Pageable pageable = PageRequest.of(page, size, sort);
return patientService.getAllPatients(pageable);
```

### 🔗 Many-to-Many Relationship

```java
@ManyToMany
@JoinTable(
    name = "prescription_medications",
    joinColumns = @JoinColumn(name = "prescription_id"),
    inverseJoinColumns = @JoinColumn(name = "medication_id")
)
private List<Medication> medications = new ArrayList<>();
```

### ✅ existsBy() Methods

```java
boolean existsByEmail(String email);
boolean existsByCode(String code);
boolean existsByNationalId(String nationalId);
```

### 🗺️ Province Query by Code (JPQL)

```java
@Query("SELECT p FROM Patient p " +
       "JOIN p.location loc " +
       "LEFT JOIN loc.parent parent1 " +
       "LEFT JOIN parent1.parent parent2 " +
       "WHERE loc.code = :provinceCode " +
       "OR parent1.code = :provinceCode " +
       "OR parent2.code = :provinceCode")
List<Patient> findAllByProvinceCode(@Param("provinceCode") String code);
## 📁 Project Structure

```
mediconnect/
├── src/main/java/auca/ac/rw/mediconnect/
│   ├── controller/    # REST APIs
│   ├── model/         # JPA Entities
│   ├── repository/   # Data Access
│   └── service/      # Business Logic
├── assets/images/     # Screenshots
├── docs/ERD.html     # Interactive ERD
├── pom.xml
└── mvnw
```

## 📸 Screenshots

<p align="center">
  <img src="assets/images/patient%20insert.PNG" width="45%">
  <img src="assets/images/insert%20and%20view%20patients.PNG" width="45%">
</p>

<p align="center">
  <img src="assets/images/insert%20prescriptions.PNG" width="45%">
  <img src="assets/images/pagination%20and%20sorting.PNG" width="45%">
</p>

## ⚙️ Configuration

Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mediconnect_rw
spring.datasource.username=mediconnect
spring.datasource.password=mediconnect123
server.port=8080
```

## 👨‍💻 Author

| Field | Details |
|---|---|
| Name | Twakira Agape Gift |
| Student ID | 27320 |
| Group | C |
| Email | agapegift223@gmail.com |
| GitHub | [@AgapegifT](https://github.com/AgapegifT) |
| Institution | Adventist University of Central Africa (AUCA) |

---

<div align="center">

Made with ❤️ for AUCA Mid-Term Assessment 2026

</div>
