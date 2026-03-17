# 🏥 MediConnect RW - Healthcare Management System

<p align="center">
  <img src="https://img.shields.io/badge/Java-17%2B-blue.svg" alt="Java Version">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.0-green.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/PostgreSQL-15+-blue.svg" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Hibernate-6.x-orange.svg" alt="Hibernate">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License">
</p>

---

## 📋 Project Overview

**MediConnect RW** is a comprehensive healthcare management system developed using **Spring Boot 3.2.0** and **Java 17+**. This RESTful API application provides robust capabilities for managing medical records, prescriptions, medications, doctors, and patients in healthcare facilities across Rwanda.

### 🎯 Key Objectives

- Streamline patient registration and medical record management
- Enable efficient prescription creation and tracking
- Manage medication inventory with detailed drug information
- Support healthcare provider (doctor) management
- Geographic data management for Rwanda provinces

---

## 🏗️ Architecture

### Technology Stack

| Component | Technology |
|-----------|------------|
| **Framework** | Spring Boot 3.2.0 |
| **Language** | Java 17 |
| **ORM** | Hibernate 6.x / Spring Data JPA |
| **Database** | PostgreSQL 15+ |
| **Build Tool** | Apache Maven |
| **API Style** | RESTful JSON API |
| **Testing** | JUnit 5 / Spring Test |

### Project Structure

```
mediconnect/
├── src/
│   ├── main/
│   │   ├── java/auca/ac/rw/mediconnect/
│   │   │   ├── controller/          # REST API Controllers
│   │   │   │   ├── DoctorController.java
│   │   │   │   ├── PatientController.java
│   │   │   │   ├── PrescriptionController.java
│   │   │   │   ├── MedicationController.java
│   │   │   │   └── LocationController.java
│   │   │   ├── model/               # Entity Classes (JPA)
│   │   │   │   ├── Doctor.java
│   │   │   │   ├── Patient.java
│   │   │   │   ├── Prescription.java
│   │   │   │   ├── Medication.java
│   │   │   │   └── Location.java
│   │   │   ├── repository/          # Data Access Layer
│   │   │   │   ├── DoctorRepository.java
│   │   │   │   ├── PatientRepository.java
│   │   │   │   ├── PrescriptionRepository.java
│   │   │   │   ├── MedicationRepository.java
│   │   │   │   └── LocationRepository.java
│   │   │   ├── service/             # Business Logic
│   │   │   │   ├── DoctorService.java
│   │   │   │   ├── PatientService.java
│   │   │   │   ├── PrescriptionService.java
│   │   │   │   ├── MedicationService.java
│   │   │   │   └── LocationService.java
│   │   │   └── MediConnectRwApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/auca/ac/rw/mediconnect/
│           └── MediconnectApplicationTests.java
├── assets/images/                   # Documentation Screenshots
├── pom.xml
├── mvnw / mvnw.cmd
└── rebuild_and_run.bat
```

---

## 🎨 Features

### 1. Patient Management
- ✅ Register new patients with complete personal information
- ✅ View and search patient records
- ✅ Pagination and sorting support
- ✅ Patient history tracking
- ✅ National ID verification

### 2. Doctor Management  
- ✅ Doctor registration with specialization
- ✅ License number tracking
- ✅ Contact information management
- ✅ Association with location/province

### 3. Prescription Management
- ✅ Create and manage prescriptions
- ✅ Link prescriptions to patients and doctors
- ✅ Multiple medications per prescription (**Many-to-Many** relationship)
- ✅ Auto-generated prescription numbers (RX-*)
- ✅ Diagnosis and notes tracking

### 4. Medication Management
- ✅ Drug inventory management
- ✅ Medication details (name, code, dosage form, strength)
- ✅ Manufacturer tracking

### 5. Location/Province Management
- ✅ Rwanda provinces management
- ✅ Province code lookup queries

---

## 🗂️ Database Schema

### Entity Relationships

```
┌─────────────┐       ┌──────────────────┐       ┌─────────────┐
│   Doctor    │       │   Prescription   │       │   Patient   │
│  (1)────────┼───────<    (M)           >┼─────────(1)    │
└─────────────┘       └──────────────────┘       └─────────────┘
                              │
                              │ (Many-to-Many)
                              │
                        ┌─────▼─────┐
                        │ Medication│
                        └───────────┘
                               │
                               │ (Many-to-One)
                               ▼
                        ┌───────────┐
                        │  Location │
                        └───────────┘
```

### Database Tables

| Table | Description |
|-------|-------------|
| `doctors` | Doctor profiles with specialization |
| `patients` | Patient records with personal info |
| `prescriptions` | Medical prescriptions |
| `medications` | Drug inventory |
| `locations` | Rwanda provinces |
| `prescription_medications` | Junction table for M:N relationship |

---

## 📡 API Endpoints

### Patients API
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/patients` | Get all patients (paginated) |
| `GET` | `/api/patients/{id}` | Get patient by ID |
| `POST` | `/api/patients` | Create new patient |
| `PUT` | `/api/patients/{id}` | Update patient |
| `DELETE` | `/api/patients/{id}` | Delete patient |
| `GET` | `/api/patients/search?name={name}` | Search by name |

### Doctors API
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/doctors` | Get all doctors |
| `GET` | `/api/doctors/{id}` | Get doctor by ID |
| `POST` | `/api/doctors` | Create new doctor |
| `PUT` | `/api/doctors/{id}` | Update doctor |
| `DELETE` | `/api/doctors/{id}` | Delete doctor |

### Prescriptions API
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/prescriptions` | Get all prescriptions |
| `GET` | `/api/prescriptions/{id}` | Get prescription by ID |
| `POST` | `/api/prescriptions` | Create new prescription |
| `PUT` | `/api/prescriptions/{id}` | Update prescription |
| `DELETE` | `/api/prescriptions/{id}` | Delete prescription |

### Medications API
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/medications` | Get all medications |
| `GET` | `/api/medications/{id}` | Get medication by ID |
| `POST` | `/api/medications` | Create new medication |
| `PUT` | `/api/medications/{id}` | Update medication |
| `DELETE` | `/api/medications/{id}` | Delete medication |

### Locations API
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/locations` | Get all provinces |
| `GET` | `/api/locations/{code}` | Get province by code |
| `POST` | `/api/locations` | Create new province |

---

## 🚀 Getting Started

### Prerequisites

- ☕ **Java Development Kit (JDK)** 17 or higher
- 🐘 **PostgreSQL** 15 or higher
- 📦 **Apache Maven** 3.8+
- 💻 **IDE** (IntelliJ IDEA, Eclipse, or VS Code)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/AgapegifT/midterm_27320_grp-C.git
   cd midterm_27320_grp-C/mediconnect
   ```

2. **Create PostgreSQL database**
   ```sql
   CREATE DATABASE mediconnect_rw;
   CREATE USER mediconnect WITH PASSWORD 'mediconnect123';
   GRANT ALL PRIVILEGES ON DATABASE mediconnect_rw TO mediconnect;
   ```

3. **Configure Database** (Optional - defaults are set)
   ```properties
   # src/main/resources/application.properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/mediconnect_rw
   spring.datasource.username=mediconnect
   spring.datasource.password=mediconnect123
   ```

4. **Build the project**
   ```bash
   ./mvnw clean package
   ```

5. **Run the application**
   ```bash
   # Option 1: Using Maven
   ./mvnw spring-boot:run
   
   # Option 2: Using the provided script (Windows)
   rebuild_and_run.bat
   ```

6. **Access the API**
   - Base URL: `http://localhost:8080/api`
   - API Documentation: Use Postman or similar tools

---

## 📸 Application Screenshots

### Patient Management
<p align="center">
  <img src="assets/images/patient%20insert.PNG" alt="Patient Insert" width="45%">
  <img src="assets/images/insert%20and%20view%20patients.PNG" alt="View Patients" width="45%">
</p>

### Doctor Management
<p align="center">
  <img src="assets/images/insert%20and%20view%20doctors.PNG" alt="Doctor Management" width="60%">
</p>

### Prescription & Medication
<p align="center">
  <img src="assets/images/insert%20prescriptions.PNG" alt="Insert Prescriptions" width="45%">
  <img src="assets/images/doctor%20%20create%20prescription.PNG" alt="Create Prescription" width="45%">
</p>
<p align="center">
  <img src="assets/images/add%20medication%20to%20prescription%20many%20to%20many.PNG" alt="Add Medication" width="45%">
  <img src="assets/images/insert%20medicines.PNG" alt="Insert Medicines" width="45%">
</p>

### Location Management
<p align="center">
  <img src="assets/images/Create%20Province%20(Location).PNG" alt="Create Province" width="45%">
  <img src="assets/images/province%20query%20by%20code.PNG" alt="Query Province" width="45%">
</p>

### Advanced Features
<p align="center">
  <img src="assets/images/pagination%20and%20sorting.PNG" alt="Pagination & Sorting" width="60%">
</p>
<p align="center">
  <img src="assets/images/search%20patient%20esixtby.PNG" alt="Search Patient" width="45%">
  <img src="assets/images/medication%20insert%20test.PNG" alt="Medication Test" width="45%">
</p>

---

## 🧪 Testing

Run unit tests:
```bash
./mvnw test
```

Build and run the JAR:
```bash
./mvnw clean package
java -jar target/mediconnect-1.0.0.jar
```

---

## 📝 Configuration

### application.properties

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/api

# Database Configuration (PostgreSQL)
spring.datasource.url=jdbc:postgresql://localhost:5432/mediconnect_rw
spring.datasource.username=mediconnect
spring.datasource.password=mediconnect123
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

---

## 📦 Build Output

After successful build:
```
target/mediconnect-1.0.0.jar
```

Run the JAR directly:
```bash
java -jar target/mediconnect-1.0.0.jar
```

---

## 👥 Contributors

- **Group C** - Midterm Project Team
- **African University of Science and Technology (AUST)** - Rwanda Campus

---

## 📄 License

This project is licensed under the **MIT License**.

---

## 🙏 Acknowledgments

- 🏛️ African University of Science and Technology (AUST) - Rwanda Campus
- 📚 Course: Advanced Java Programming (AUCA RW)
- 👨‍🏫 Instructor: Course Instructor

---

<p align="center">
  Made with ❤️ for Healthcare Management
</p>

<p align="center">
  <img src="https://komarev.com/ghpvc/?username=AgapegifT&repo=midterm_27320_grp-C&label=Views&color=green&style=flat" alt="Profile Views">
</p>
