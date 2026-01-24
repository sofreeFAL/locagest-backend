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

    @Column(name = "prix_par_jour", nullable = false)
    private Double prixParJour;

    // CHAMP UNIQUE POUR LE STATUT
    @Column(nullable = false, length = 20)
    private String statut = "DISPONIBLE"; // Valeurs: DISPONIBLE, LOUE, EN_MAINTENANCE

    // CHAMP DÉRIVÉ (calculé automatiquement)
    @Transient // Ce champ n'est pas persisté en base, il est calculé
    public boolean isDisponible() {
        return "DISPONIBLE".equals(statut);
    }

    // Méthode pour définir le statut et synchroniser la disponibilité
    public void setStatut(String statut) {
        this.statut = statut != null ? statut.toUpperCase() : "DISPONIBLE";
    }

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

    public String getStatut() {
        return statut;
    }

    // Méthode dépréciée - à utiliser uniquement pour la compatibilité
    @Deprecated
    public void setDisponible(boolean disponible) {
        // Cette méthode ne fait rien car la disponibilité est dérivée du statut
        // Elle est gardée pour la compatibilité avec le code existant
        if (disponible) {
            this.statut = "DISPONIBLE";
        } else if ("EN_MAINTENANCE".equals(this.statut)) {
            // Si le véhicule est en maintenance, on garde ce statut
            // Sinon on met "LOUE"
            this.statut = "LOUE";
        }
    }
}