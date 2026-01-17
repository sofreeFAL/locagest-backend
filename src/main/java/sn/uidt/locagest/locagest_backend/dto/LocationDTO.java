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
    // INFORMATIONS CLIENT (AJOUTÉ)
    // =========================
    private String clientNom;
    private String clientPrenom;
    private String clientTelephone;

    // =========================
    // INFORMATIONS VÉHICULE (AJOUTÉ)
    // =========================
    private String vehiculeMarque;
    private String vehiculeModele;
    private String vehiculeImmatriculation;
    private Double vehiculePrixParJour;

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

    public String getClientNom() {
        return clientNom;
    }

    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }

    public String getClientPrenom() {
        return clientPrenom;
    }

    public void setClientPrenom(String clientPrenom) {
        this.clientPrenom = clientPrenom;
    }

    public String getClientTelephone() {
        return clientTelephone;
    }

    public void setClientTelephone(String clientTelephone) {
        this.clientTelephone = clientTelephone;
    }

    public String getVehiculeMarque() {
        return vehiculeMarque;
    }

    public void setVehiculeMarque(String vehiculeMarque) {
        this.vehiculeMarque = vehiculeMarque;
    }

    public String getVehiculeModele() {
        return vehiculeModele;
    }

    public void setVehiculeModele(String vehiculeModele) {
        this.vehiculeModele = vehiculeModele;
    }

    public String getVehiculeImmatriculation() {
        return vehiculeImmatriculation;
    }

    public void setVehiculeImmatriculation(String vehiculeImmatriculation) {
        this.vehiculeImmatriculation = vehiculeImmatriculation;
    }

    public Double getVehiculePrixParJour() {
        return vehiculePrixParJour;
    }

    public void setVehiculePrixParJour(Double vehiculePrixParJour) {
        this.vehiculePrixParJour = vehiculePrixParJour;
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