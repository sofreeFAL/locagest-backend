package sn.uidt.locagest.locagest_backend.dto;

public class VehiculeDTO {

    private Long id;

    private String marque;
    private String modele;
    private String immatriculation;

    //  PRIX FIXÉ PAR L’ADMIN À LA CRÉATION
    private Double prixParJour;

    //  DISPONIBILITÉ (LECTURE SEULE POUR USER)
    private Boolean disponible;

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
}
