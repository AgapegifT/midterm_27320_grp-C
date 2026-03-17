# MediConnect RW - Healthcare Management System

<p align="center">
  <img src="https://img.shields.io/badge/Java-17%2B-blue.svg" alt="Java Version">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.x-green.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Hibernate-6.x-orange.svg" alt="Hibernate">
  <img src="https://img.shields.io/badge/MySQL-8.0-blue.svg" alt="MySQL">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License">
</p>

## 📋 Overview

MediConnect RW is a comprehensive healthcare management system developed as a Java Spring Boot application for managing medical records, prescriptions, medications, doctors, and patients in a healthcare facility.

## 🏥 Features

### Core Functionality
- **Patient Management**
  - Register and manage patient records
  - View patient history
  - Search patients by various criteria
  - Pagination and sorting support

- **Doctor Management**
  - Doctor registration and profile management
  - Specialization tracking
  - Contact information management

- **Prescription Management**
  - Create and manage prescriptions
  - Link prescriptions to patients and doctors
  - Multiple medications per prescription (Many-to-Many relationship)

- **Medication Management**
  - Drug inventory management
  - Medication details (name, dosage, description)

- **Location/Province Management**
  - Geographic data management for Rwanda provinces
  - Province code lookup

## 🛠️ Technology Stack

| Component | Technology |
|-----------|------------|
| **Framework** | Spring Boot 3.x |
| **Language** | Java 17+ |
| **ORM** | Hibernate / Spring Data JPA |
| **Database** | MySQL 8.0 |
| **Build Tool** | Apache Maven |
| **Testing** | JUnit / Spring Test |

## 📁 Project Structure

```
mediconnect/
├── src/
│   ├── main/
│   │   ├── java/auca/ac/rw/mediconnect/
│   │   │   ├── controller/      # REST API Controllers
│   │   │   │   ├── DoctorController.java
│   │   │   │   ├── PatientController.java
│   │   │   │   ├── PrescriptionController.java
│   │   │   │   ├── MedicationController.java
│   │   │   │   └── LocationController.java
│   │   │   ├── model/           # Entity Classes
│   │   │   │   ├── Doctor.java
│   │   │   │   ├── Patient.java
│   │   │   │   ├── Prescription.java
│   │   │   │   ├── Medication.java
│   │   │   │   └── Location.java
│   │   │   ├── repository/      # Data Access Layer
│   │   │   │   ├── DoctorRepository.java
│   │   │   │   ├── PatientRepository.java
│   │   │   │   ├── PrescriptionRepository.java
│   │   │   │   ├── MedicationRepository.java
│   │   │   │   └── LocationRepository.java
│   │   │   ├── service/         # Business Logic
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
├── pom.xml
├── mvnw / mvnw.cmd
└── rebuild_and_run.bat
```

## 🚀 Getting Started

### Prerequisites

- Java Development Kit (JDK) 17 or higher
- Apache Maven 3.8+
- MySQL 8.0 or higher
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/AgapegifT/midterm_27320_grp-C.git
   cd midterm_27320_grp-C
   ```

2. **Configure Database**
   
   Update `src/main/resources/application.properties` with your database credentials:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/mediconnect?createDatabaseIfNotExist=true
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **Build the project**
   ```bash
   ./mvnw clean package
   ```

4. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```
   
   Or on Windows:
   ```bash
   rebuild_and_run.bat
   ```

5. **Access the application**
   - API Base URL: `http://localhost:8080`
   - H2 Console (if enabled): `http://localhost:8080/h2-console`

## 📚 API Endpoints

### Patients
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/patients` | Get all patients (with pagination) |
| GET | `/api/patients/{id}` | Get patient by ID |
| POST | `/api/patients` | Create new patient |
| PUT | `/api/patients/{id}` | Update patient |
| DELETE | `/api/patients/{id}` | Delete patient |
| GET | `/api/patients/search?name={name}` | Search patients by name |

### Doctors
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/doctors` | Get all doctors |
| GET | `/api/doctors/{id}` | Get doctor by ID |
| POST | `/api/doctors` | Create new doctor |
| PUT | `/api/doctors/{id}` | Update doctor |
| DELETE | `/api/doctors/{id}` | Delete doctor |

### Prescriptions
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/prescriptions` | Get all prescriptions |
| GET | `/api/prescriptions/{id}` | Get prescription by ID |
| POST | `/api/prescriptions` | Create new prescription |
| PUT | `/api/prescriptions/{id}` | Update prescription |
| DELETE | `/api/prescriptions/{id}` | Delete prescription |

### Medications
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/medications` | Get all medications |
| GET | `/api/medications/{id}` | Get medication by ID |
| POST | `/api/medications` | Create new medication |
| PUT | `/api/medications/{id}` | Update medication |
| DELETE | `/api/medications/{id}` | Delete medication |

### Locations
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/locations` | Get all provinces |
| GET | `/api/locations/{code}` | Get province by code |
| POST | `/api/locations` | Create new province |

## 🗂️ Database Schema

### Entity Relationships

```
Doctor (1) ──────< Prescription (M) >────── (1) Patient
                                      │
                                      └< PrescriptionItem >
                                      │
                            (M) >────── Medication
```

- **Doctor** → One-to-Many with Prescription
- **Patient** → One-to-Many with Prescription
- **Prescription** → Many-to-Many with Medication (through prescription_items)
- **Location** → Independent entity for geographic data

## 📝 Configuration

### application.properties

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/mediconnect?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

## 🧪 Testing

Run the tests using Maven:
```bash
./mvnw test
```

## 📦 Build Output

After building, the JAR file will be generated at:
```
target/mediconnect-1.0.0.jar
```

Run the JAR file directly:
```bash
java -jar target/mediconnect-1.0.0.jar
```

## 👥 Contributors

- **Group C** - Midterm Project Team

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- African University of Science and Technology (AUST) - Rwanda Campus
- Course: Advanced Java Programming (AUCA RW)
- Instructor: [Your Instructor's Name]

---

<p align="center">Made with ❤️ for Healthcare Management</p>
