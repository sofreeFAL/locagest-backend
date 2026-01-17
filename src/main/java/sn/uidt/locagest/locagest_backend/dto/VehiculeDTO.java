package sn.uidt.locagest.locagest_backend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VehiculeDTO {

    private Long id;
    private String marque;
    private String modele;
    private String immatriculation;
    private Double prixParJour;
    private Boolean disponible;

    // AJOUTER CE CHAMP POUR LE STATUT
    @JsonProperty("statut")
    private String statut; // "DISPONIBLE", "LOUE", "EN_MAINTENANCE"

    public VehiculeDTO() {}

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

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    // AJOUTER CES GETTERS/SETTERS
    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
}