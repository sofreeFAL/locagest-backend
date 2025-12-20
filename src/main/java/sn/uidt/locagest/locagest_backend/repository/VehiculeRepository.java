package sn.uidt.locagest.locagest_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.uidt.locagest.locagest_backend.model.Vehicule;

import java.util.List;

public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    // 🔍 Tous les véhicules disponibles
    List<Vehicule> findByDisponibleTrue();
}
