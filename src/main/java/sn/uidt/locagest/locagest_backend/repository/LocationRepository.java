package sn.uidt.locagest.locagest_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.uidt.locagest.locagest_backend.model.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
