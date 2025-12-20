package sn.uidt.locagest.locagest_backend.controller;

import org.springframework.web.bind.annotation.*;
import sn.uidt.locagest.locagest_backend.dto.LocationDTO;
import sn.uidt.locagest.locagest_backend.mapper.LocationMapper;
import sn.uidt.locagest.locagest_backend.model.Location;
import sn.uidt.locagest.locagest_backend.service.LocationService;

import java.util.List;

@RestController
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    // ➕ Créer une location
    @PostMapping
    public LocationDTO create(@RequestBody LocationDTO dto) {
        Location location = LocationMapper.toEntity(dto);
        return LocationMapper.toDTO(locationService.create(location));
    }

    //  Lister toutes les locations
    @GetMapping
    public List<LocationDTO> getAll() {
        return locationService.getAll()
                .stream()
                .map(LocationMapper::toDTO)
                .toList();
    }

    //  Retour du véhicule
    @PutMapping("/{id}/retour")
    public LocationDTO retourVehicule(@PathVariable Long id) {
        return LocationMapper.toDTO(locationService.retourVehicule(id));
    }
}
