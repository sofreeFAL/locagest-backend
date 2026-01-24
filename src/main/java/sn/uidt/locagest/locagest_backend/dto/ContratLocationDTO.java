package sn.uidt.locagest.locagest_backend.dto;

import java.time.LocalDate;

public class ContratLocationDTO {

    private Long id;
    private String numeroContrat;
    private Long locationId;
    private LocalDate dateCreation;
    private Double montant;
    private String statut;

    // Informations de la location (pour l'affichage)
    private String clientNom;
    private String clientPrenom;
    private String vehiculeMarque;
    private String vehiculeModele;
    private String vehiculeImmatriculation;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Double montantTotalLocation;

    // GETTERS & SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroContrat() { return numeroContrat; }
    public void setNumeroContrat(String numeroContrat) { this.numeroContrat = numeroContrat; }

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }

    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getClientNom() { return clientNom; }
    public void setClientNom(String clientNom) { this.clientNom = clientNom; }

    public String getClientPrenom() { return clientPrenom; }
    public void setClientPrenom(String clientPrenom) { this.clientPrenom = clientPrenom; }

    public String getVehiculeMarque() { return vehiculeMarque; }
    public void setVehiculeMarque(String vehiculeMarque) { this.vehiculeMarque = vehiculeMarque; }

    public String getVehiculeModele() { return vehiculeModele; }
    public void setVehiculeModele(String vehiculeModele) { this.vehiculeModele = vehiculeModele; }

    public String getVehiculeImmatriculation() { return vehiculeImmatriculation; }
    public void setVehiculeImmatriculation(String vehiculeImmatriculation) { this.vehiculeImmatriculation = vehiculeImmatriculation; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public Double getMontantTotalLocation() { return montantTotalLocation; }
    public void setMontantTotalLocation(Double montantTotalLocation) { this.montantTotalLocation = montantTotalLocation; }
}