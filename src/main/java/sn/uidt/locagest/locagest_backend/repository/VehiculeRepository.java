package sn.uidt.locagest.locagest_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.uidt.locagest.locagest_backend.model.Vehicule;

import java.util.List;

public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    //  Vérifier doublon
    boolean existsByImmatriculation(String immatriculation);

    //  Liste ordonnée
    List<Vehicule> findAllByOrderByIdAsc();

    //  Véhicules disponibles
    List<Vehicule> findByDisponibleTrueOrderByIdAsc();

    //  Recherche robuste multi-critères
    @Query("""
        SELECT v FROM Vehicule v
        WHERE (:marque IS NULL OR :marque = '' 
              OR LOWER(v.marque) LIKE LOWER(CONCAT('%', :marque, '%')))
          AND (:modele IS NULL OR :modele = '' 
              OR LOWER(v.modele) LIKE LOWER(CONCAT('%', :modele, '%')))
          AND (:immatriculation IS NULL OR :immatriculation = '' 
              OR LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :immatriculation, '%')))
          AND (:disponible IS NULL OR v.disponible = :disponible)
        ORDER BY v.id ASC
    """)
    List<Vehicule> search(
            @Param("marque") String marque,
            @Param("modele") String modele,
            @Param("immatriculation") String immatriculation,
            @Param("disponible") Boolean disponible
    );
}
