package auca.ac.rw.mediconnect.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import auca.ac.rw.mediconnect.model.Medication;

import java.util.Optional;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {

    Optional<Medication> findByCode(String code);

    Optional<Medication> findByNameIgnoreCase(String name);

    boolean existsByCode(String code);

    boolean existsByNameIgnoreCase(String name);

    Page<Medication> findByDosageFormIgnoreCase(String dosageForm, Pageable pageable);

    Page<Medication> findByManufacturerIgnoreCase(String manufacturer, Pageable pageable);

    Page<Medication> findByNameContainingIgnoreCase(String name, Pageable pageable);
}