package auca.ac.rw.mediconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import auca.ac.rw.mediconnect.model.Location;
import auca.ac.rw.mediconnect.service.LocationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/locations")
@CrossOrigin(origins = "*")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @PostMapping
    public ResponseEntity<Location> createLocation(@RequestBody Location location) {
        Location created = locationService.createLocation(location);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<Location>> getAllLocations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Location> locations = locationService.getAllLocations(pageable);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable Long id) {
        Location location = locationService.getLocationById(id);
        return ResponseEntity.ok(location);
    }

    @GetMapping("/province/{provinceCode}")
    public ResponseEntity<Location> getLocationByProvinceCode(@PathVariable String provinceCode) {
        Location location = locationService.getLocationByCode(provinceCode);
        return ResponseEntity.ok(location);
    }

    @GetMapping("/children/{parentId}")
    public ResponseEntity<List<Location>> getChildLocations(@PathVariable Long parentId) {
        List<Location> children = locationService.getChildLocations(parentId);
        return ResponseEntity.ok(children);
    }

    @GetMapping("/roots")
    public ResponseEntity<List<Location>> getRootLocations() {
        List<Location> roots = locationService.getRootLocations();
        return ResponseEntity.ok(roots);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Location> updateLocation(
            @PathVariable Long id,
            @RequestBody Location locationDetails) {
        Location updated = locationService.updateLocation(id, locationDetails);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Location deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exists/code/{code}")
    public ResponseEntity<Map<String, Boolean>> checkCodeExists(@PathVariable String code) {
        boolean exists = locationService.existsByCode(code);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
}