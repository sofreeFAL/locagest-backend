package sn.uidt.locagest.locagest_backend.dto;

import java.time.LocalDateTime;

public class PaiementDTO {

    // =========================
    // IDENTIFIANTS
    // =========================
    private Long id;
    private Long locationId;

    // =========================
    // MONTANT (LECTURE SEULE)
    // =========================
    private Double montant;

    // =========================
    // INFOS PAIEMENT
    // =========================
    private LocalDateTime datePaiement;
    private String modePaiement;
    private String statut;

    // =========================
    // GETTERS & SETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Double getMontant() {
        return montant;
    }

    //  PAS UTILISÉ EN ENTRÉE
    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDateTime datePaiement) {
        this.datePaiement = datePaiement;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement = modePaiement;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
}
