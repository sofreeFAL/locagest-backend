package sn.uidt.locagest.locagest_backend.service;

import org.springframework.stereotype.Service;
import sn.uidt.locagest.locagest_backend.model.*;
import sn.uidt.locagest.locagest_backend.repository.*;

import java.util.List;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final VehiculeRepository vehiculeRepository;

    public LocationService(LocationRepository locationRepository,
                           VehiculeRepository vehiculeRepository) {
        this.locationRepository = locationRepository;
        this.vehiculeRepository = vehiculeRepository;
    }

    public Location create(Location location) {
        Vehicule vehicule = vehiculeRepository.findById(location.getVehicule().getId())
                .orElseThrow(() -> new RuntimeException("Véhicule introuvable"));

        vehicule.setDisponible(false);
        vehiculeRepository.save(vehicule);

        location.setStatut(StatutLocation.EN_COURS);
        location.setVehicule(vehicule);

        return locationRepository.save(location);
    }

    public List<Location> getAll() {
        return locationRepository.findAll();
    }

    public Location retourVehicule(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location introuvable"));

        Vehicule vehicule = location.getVehicule();
        vehicule.setDisponible(true);
        vehiculeRepository.save(vehicule);

        location.setStatut(StatutLocation.TERMINEE);
        return locationRepository.save(location);
    }
}
