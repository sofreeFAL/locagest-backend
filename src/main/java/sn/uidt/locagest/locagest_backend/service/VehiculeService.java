package sn.uidt.locagest.locagest_backend.service;

import org.springframework.stereotype.Service;
import sn.uidt.locagest.locagest_backend.model.Vehicule;
import sn.uidt.locagest.locagest_backend.repository.VehiculeRepository;

import java.util.List;

@Service
public class VehiculeService {

    private final VehiculeRepository vehiculeRepository;

    public VehiculeService(VehiculeRepository vehiculeRepository) {
        this.vehiculeRepository = vehiculeRepository;
    }

    public Vehicule create(Vehicule vehicule) {
        vehicule.setDisponible(true);
        return vehiculeRepository.save(vehicule);
    }

    public List<Vehicule> getAll() {
        return vehiculeRepository.findAll();
    }

    public List<Vehicule> getDisponibles() {
        return vehiculeRepository.findByDisponibleTrue();
    }

    public Vehicule getById(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Véhicule introuvable"));
    }

    public Vehicule changerDisponibilite(Long id, boolean disponible) {
        Vehicule vehicule = getById(id);
        vehicule.setDisponible(disponible);
        return vehiculeRepository.save(vehicule);
    }

    public void delete(Long id) {
        vehiculeRepository.deleteById(id);
    }
}
