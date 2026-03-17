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

## 🗄️ Database Schema

View the interactive ERD: **[ERD.html](docs/ERD.html)**

```
LOCATION ───< PATIENT      LOCATION ───< DOCTOR
    │                         │
    └─────────────────────────┼───────< PRESCRIPTION >───────< MEDICATION
                              │              │
                              └──────────────┴─ prescription_medications
```

## 📡 API Endpoints

| Resource | Endpoints |
|----------|-----------|
| Patients | `/patients` - GET, POST |
| Doctors | `/doctors` - GET, POST |
| Prescriptions | `/prescriptions` - GET, POST |
| Medications | `/medications` - GET, POST |
| Locations | `/locations` - GET, POST |

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

## 👤 Author

**Group C** - Midterm Project  
African University of Science and Technology (AUST) - Rwanda  
📧 agapegift223@gmail.com

---

<p align="center">Built with ❤️ for Healthcare</p>
