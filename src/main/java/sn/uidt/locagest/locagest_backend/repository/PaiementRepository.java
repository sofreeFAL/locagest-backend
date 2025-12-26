package sn.uidt.locagest.locagest_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.uidt.locagest.locagest_backend.model.Paiement;
import sn.uidt.locagest.locagest_backend.model.StatutPaiement;

import java.util.List;
import java.util.Optional;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    //  Vérifier si une location a AU MOINS UN paiement
    boolean existsByLocationId(Long locationId);

    //  Vérifier si une location a un paiement PAYÉ
    boolean existsByLocationIdAndStatut(Long locationId, StatutPaiement statut);

    //  Trouver le paiement d’une location
    Optional<Paiement> findByLocationId(Long locationId);

    // Historique des paiements par statut
    List<Paiement> findByStatut(StatutPaiement statut);

    //  Historique des paiements d’une location
    List<Paiement> findAllByLocationId(Long locationId);
}
