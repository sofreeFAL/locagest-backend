package sn.uidt.locagest.locagest_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sn.uidt.locagest.locagest_backend.model.ContratLocation;

import java.util.List;
import java.util.Optional;

public interface ContratLocationRepository extends JpaRepository<ContratLocation, Long> {

    Optional<ContratLocation> findByLocationId(Long locationId);

    Optional<ContratLocation> findByNumeroContrat(String numeroContrat);

    // Trouver tous les contrats avec leurs locations chargées
    @Query("SELECT c FROM ContratLocation c LEFT JOIN FETCH c.location l LEFT JOIN FETCH l.client LEFT JOIN FETCH l.vehicule")
    List<ContratLocation> findAllWithLocations();
}