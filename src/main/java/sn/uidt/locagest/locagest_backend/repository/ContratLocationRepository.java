package sn.uidt.locagest.locagest_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.uidt.locagest.locagest_backend.model.ContratLocation;

import java.util.Optional;

public interface ContratLocationRepository extends JpaRepository<ContratLocation, Long> {

    Optional<ContratLocation> findByLocationId(Long locationId);
}
