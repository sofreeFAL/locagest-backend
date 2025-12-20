package sn.uidt.locagest.locagest_backend.dto;

import java.time.LocalDateTime;

public class PaiementDTO {

    private Long id;
    private Long locationId;
    private double montant;
    private LocalDateTime datePaiement;
    private String mode;
    private String statut;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public LocalDateTime getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDateTime datePaiement) { this.datePaiement = datePaiement; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
