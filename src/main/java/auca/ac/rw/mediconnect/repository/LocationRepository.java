package auca.ac.rw.mediconnect.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import auca.ac.rw.mediconnect.model.Location;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

       Optional<Location> findByCode(String code);

       Optional<Location> findByNameIgnoreCase(String name);

       List<Location> findByParentId(Long parentId);

       List<Location> findByParentIsNull();

       boolean existsByCode(String code);

       boolean existsByNameIgnoreCase(String name);

       @Query("SELECT l FROM Location l WHERE l.code = :provinceCode OR " +
                     "(l.parent IS NOT NULL AND l.parent.code = :provinceCode) OR " +
                     "(l.parent IS NOT NULL AND l.parent.parent IS NOT NULL AND l.parent.parent.code = :provinceCode)")
       List<Location> findAllInProvince(@Param("provinceCode") String provinceCode);

       @Query("SELECT l FROM Location l WHERE LOWER(l.name) = LOWER(:provinceName) OR " +
                     "(l.parent IS NOT NULL AND LOWER(l.parent.name) = LOWER(:provinceName)) OR " +
                     "(l.parent IS NOT NULL AND l.parent.parent IS NOT NULL AND LOWER(l.parent.parent.name) = LOWER(:provinceName))")
       List<Location> findAllInProvinceByName(@Param("provinceName") String provinceName);
}