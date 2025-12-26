package sn.uidt.locagest.locagest_backend.dto;

import java.time.LocalDate;

public class LocationDTO {

    // =========================
    // IDENTIFIANTS
    // =========================
    private Long id;
    private Long clientId;
    private Long vehiculeId;

    // =========================
    // DATES
    // =========================
    private LocalDate dateDebut;
    private LocalDate dateFin;

    // =========================
    // PRIX (FIGÉ)
    // =========================
    private Double montantTotalLocation;

    // =========================
    // STATUT
    // =========================
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

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public Double getMontantTotalLocation() {
        return montantTotalLocation;
    }

    public void setMontantTotalLocation(Double montantTotalLocation) {
        this.montantTotalLocation = montantTotalLocation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
}
