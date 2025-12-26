package sn.uidt.locagest.locagest_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.uidt.locagest.locagest_backend.model.Client;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {

    boolean existsByEmail(String email);
    boolean existsByTelephone(String telephone);
    boolean existsByNumeroCni(String numeroCni);

    // =========================
    //  RECHERCHE GLOBALE (POSTGRESQL SAFE)
    // =========================
    @Query("""
    SELECT c FROM Client c
    WHERE (:nom IS NULL OR c.nom ILIKE CONCAT('%', CAST(:nom AS string), '%'))
      AND (:prenom IS NULL OR c.prenom ILIKE CONCAT('%', CAST(:prenom AS string), '%'))
      AND (:telephone IS NULL OR c.telephone LIKE CONCAT('%', CAST(:telephone AS string), '%'))
      AND (:email IS NULL OR c.email ILIKE CONCAT('%', CAST(:email AS string), '%'))
      AND (:numeroCni IS NULL OR c.numeroCni LIKE CONCAT('%', CAST(:numeroCni AS string), '%'))
""")
    List<Client> search(
            @Param("nom") String nom,
            @Param("prenom") String prenom,
            @Param("telephone") String telephone,
            @Param("email") String email,
            @Param("numeroCni") String numeroCni
    );

}
