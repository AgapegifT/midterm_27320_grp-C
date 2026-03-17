package auca.ac.rw.mediconnect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import auca.ac.rw.mediconnect.model.Location;
import auca.ac.rw.mediconnect.repository.LocationRepository;

import java.util.List;

@Service
@Transactional
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    public Location createLocation(Location location) {
        if (location.getCode() != null && locationRepository.existsByCode(location.getCode())) {
            throw new RuntimeException("Location with code " + location.getCode() + " already exists");
        }

        if (locationRepository.existsByNameIgnoreCase(location.getName())) {
            throw new RuntimeException("Location with name " + location.getName() + " already exists");
        }

        if (location.getParent() != null) {
            Location parent = locationRepository.findById(location.getParent().getId())
                    .orElseThrow(() -> new RuntimeException("Parent location not found"));
            location.setParent(parent);
        }

        return locationRepository.save(location);
    }

    public Page<Location> getAllLocations(Pageable pageable) {
        return locationRepository.findAll(pageable);
    }

    public Location getLocationById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + id));
    }

    public Location getLocationByCode(String code) {
        return locationRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Location not found with code: " + code));
    }

    public List<Location> getChildLocations(Long parentId) {
        return locationRepository.findByParentId(parentId);
    }

    public List<Location> getRootLocations() {
        return locationRepository.findByParentIsNull();
    }

    public List<Location> getLocationsByProvinceCode(String provinceCode) {
        return locationRepository.findAllInProvince(provinceCode);
    }

    public List<Location> getLocationsByProvinceName(String provinceName) {
        return locationRepository.findAllInProvinceByName(provinceName);
    }

    public Location updateLocation(Long id, Location locationDetails) {
        Location location = getLocationById(id);

        location.setName(locationDetails.getName());
        location.setCode(locationDetails.getCode());
        location.setLocationType(locationDetails.getLocationType());

        if (locationDetails.getParent() != null) {
            Location parent = getLocationById(locationDetails.getParent().getId());
            location.setParent(parent);
        }

        return locationRepository.save(location);
    }

    public void deleteLocation(Long id) {
        Location location = getLocationById(id);
        locationRepository.delete(location);
    }

    public boolean existsByCode(String code) {
        return locationRepository.existsByCode(code);
    }
}