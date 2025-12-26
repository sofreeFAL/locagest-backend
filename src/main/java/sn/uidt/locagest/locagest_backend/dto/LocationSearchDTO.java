package sn.uidt.locagest.locagest_backend.dto;

import java.time.LocalDate;

public class LocationSearchDTO {

    private Long clientId;
    private Long vehiculeId;
    private String statut;

    //  FILTRES SUR DATE DE DÉBUT
    private LocalDate dateDebutMin;
    private LocalDate dateDebutMax;

    //  FILTRES SUR DATE DE FIN
    private LocalDate dateFinMin;
    private LocalDate dateFinMax;

    //  FILTRES SUR MONTANT
    private Double montantMin;
    private Double montantMax;

    // =========================
    // GETTERS & SETTERS
    // =========================

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getVehiculeId() {
        return vehiculeId;
    }

    public void setVehiculeId(Long vehiculeId) {
        this.vehiculeId = vehiculeId;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDate getDateDebutMin() {
        return dateDebutMin;
    }

    public void setDateDebutMin(LocalDate dateDebutMin) {
        this.dateDebutMin = dateDebutMin;
    }

    public LocalDate getDateDebutMax() {
        return dateDebutMax;
    }

    public void setDateDebutMax(LocalDate dateDebutMax) {
        this.dateDebutMax = dateDebutMax;
    }

    public LocalDate getDateFinMin() {
        return dateFinMin;
    }

    public void setDateFinMin(LocalDate dateFinMin) {
        this.dateFinMin = dateFinMin;
    }

    public LocalDate getDateFinMax() {
        return dateFinMax;
    }

    public void setDateFinMax(LocalDate dateFinMax) {
        this.dateFinMax = dateFinMax;
    }

    public Double getMontantMin() {
        return montantMin;
    }

    public void setMontantMin(Double montantMin) {
        this.montantMin = montantMin;
    }

    public Double getMontantMax() {
        return montantMax;
    }

    public void setMontantMax(Double montantMax) {
        this.montantMax = montantMax;
    }
}
