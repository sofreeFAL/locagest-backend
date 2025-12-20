package sn.uidt.locagest.locagest_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.uidt.locagest.locagest_backend.model.Paiement;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {
}
