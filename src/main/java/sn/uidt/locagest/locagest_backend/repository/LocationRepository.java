package sn.uidt.locagest.locagest_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sn.uidt.locagest.locagest_backend.model.Location;
import sn.uidt.locagest.locagest_backend.model.StatutLocation;

import java.time.LocalDate;
import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

    // =========================================================
    // 🔍 RECHERCHE SIMPLE (STATUT / CLIENT / IMMATRICULATION)
    // =========================================================
    @Query("""
        SELECT l FROM Location l
        WHERE (:statut IS NULL OR l.statut = :statut)
          AND (:clientNom IS NULL 
               OR LOWER(l.client.nom) LIKE LOWER(CONCAT('%', :clientNom, '%')))
          AND (:immatriculation IS NULL 
               OR LOWER(l.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :immatriculation, '%')))
    """)
    List<Location> searchSimple(
            @Param("statut") StatutLocation statut,
            @Param("clientNom") String clientNom,
            @Param("immatriculation") String immatriculation
    );

    // =========================================================
    // 🔍 RECHERCHE AVANCÉE (TOUS CRITÈRES)
    // =========================================================
    @Query("""
        SELECT l FROM Location l
        WHERE (:clientId IS NULL OR l.client.id = :clientId)
          AND (:vehiculeId IS NULL OR l.vehicule.id = :vehiculeId)
          AND (:statut IS NULL OR l.statut = :statut)

          AND (:dateDebutMin IS NULL OR l.dateDebut >= :dateDebutMin)
          AND (:dateDebutMax IS NULL OR l.dateDebut <= :dateDebutMax)

          AND (:dateFinMin IS NULL OR l.dateFin >= :dateFinMin)
          AND (:dateFinMax IS NULL OR l.dateFin <= :dateFinMax)

          AND (:montantMin IS NULL OR l.montantTotalLocation >= :montantMin)
          AND (:montantMax IS NULL OR l.montantTotalLocation <= :montantMax)
    """)
    List<Location> searchAdvanced(
            @Param("clientId") Long clientId,
            @Param("vehiculeId") Long vehiculeId,
            @Param("statut") StatutLocation statut,

            @Param("dateDebutMin") LocalDate dateDebutMin,
            @Param("dateDebutMax") LocalDate dateDebutMax,

            @Param("dateFinMin") LocalDate dateFinMin,
            @Param("dateFinMax") LocalDate dateFinMax,

            @Param("montantMin") Double montantMin,
            @Param("montantMax") Double montantMax
    );

    // =========================================================
    // 📚 HISTORIQUE GLOBAL (LOCATIONS TERMINÉES)
    // =========================================================
    List<Location> findByStatut(StatutLocation statut);

    // =========================================================
    // 📚 HISTORIQUE PAR CLIENT
    // =========================================================
    @Query("""
        SELECT l FROM Location l
        WHERE l.statut = sn.uidt.locagest.locagest_backend.model.StatutLocation.TERMINEE
          AND l.client.id = :clientId
        ORDER BY l.dateFin DESC
    """)
    List<Location> findHistoriqueParClient(@Param("clientId") Long clientId);

    // =========================================================
    // 📚 HISTORIQUE PAR VÉHICULE
    // =========================================================
    @Query("""
        SELECT l FROM Location l
        WHERE l.statut = sn.uidt.locagest.locagest_backend.model.StatutLocation.TERMINEE
          AND l.vehicule.id = :vehiculeId
        ORDER BY l.dateFin DESC
    """)
    List<Location> findHistoriqueParVehicule(@Param("vehiculeId") Long vehiculeId);
}
