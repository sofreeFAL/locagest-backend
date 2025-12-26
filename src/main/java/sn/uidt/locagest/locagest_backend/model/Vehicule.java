package sn.uidt.locagest.locagest_backend.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "vehicules",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "immatriculation")
        }
)
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String marque;

    @Column(nullable = false)
    private String modele;

    @Column(nullable = false, unique = true)
    private String immatriculation;

    // 💰 PRIX FIXÉ PAR L’ADMIN (OBLIGATOIRE)
    @Column(name = "prix_par_jour", nullable = false)
    private Double prixParJour;

    // 🚗 DISPONIBILITÉ DU VÉHICULE
    @Column(nullable = false)
    private boolean disponible = true;

    // ===== GETTERS & SETTERS =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public Double getPrixParJour() {
        return prixParJour;
    }

    public void setPrixParJour(Double prixParJour) {
        this.prixParJour = prixParJour;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}
