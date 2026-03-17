package auca.ac.rw.mediconnect.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import auca.ac.rw.mediconnect.model.Doctor;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

       Optional<Doctor> findByEmail(String email);

       Optional<Doctor> findByLicenseNumber(String licenseNumber);

       boolean existsByEmail(String email);

       boolean existsByLicenseNumber(String licenseNumber);

       Page<Doctor> findBySpecializationIgnoreCase(String specialization, Pageable pageable);

       Page<Doctor> findByLocationId(Long locationId, Pageable pageable);

       @Query("SELECT d FROM Doctor d " +
                     "JOIN d.location loc " +
                     "LEFT JOIN loc.parent parent1 " +
                     "LEFT JOIN parent1.parent parent2 " +
                     "LEFT JOIN parent2.parent parent3 " +
                     "WHERE loc.code = :provinceCode " +
                     "OR parent1.code = :provinceCode " +
                     "OR parent2.code = :provinceCode " +
                     "OR parent3.code = :provinceCode")
       List<Doctor> findAllByProvinceCode(@Param("provinceCode") String provinceCode);

       @Query("SELECT d FROM Doctor d " +
                     "JOIN d.location loc " +
                     "LEFT JOIN loc.parent parent1 " +
                     "LEFT JOIN parent1.parent parent2 " +
                     "LEFT JOIN parent2.parent parent3 " +
                     "WHERE LOWER(loc.name) = LOWER(:provinceName) " +
                     "OR LOWER(parent1.name) = LOWER(:provinceName) " +
                     "OR LOWER(parent2.name) = LOWER(:provinceName) " +
                     "OR LOWER(parent3.name) = LOWER(:provinceName)")
       List<Doctor> findAllByProvinceName(@Param("provinceName") String provinceName);
}