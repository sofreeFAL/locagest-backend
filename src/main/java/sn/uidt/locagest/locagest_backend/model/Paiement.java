package sn.uidt.locagest.locagest_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "paiements",
        uniqueConstraints = {
                //  Une location ne peut être payée qu'une seule fois
                @UniqueConstraint(columnNames = "location_id")
        }
)
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================
    // MONTANT PAYÉ (FIGÉ)
    // =========================
    @Column(nullable = false)
    private Double montant;

    // =========================
    // MODE DE PAIEMENT
    // =========================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModePaiement mode;

    // =========================
    // STATUT DU PAIEMENT
    // =========================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPaiement statut;

    // =========================
    // DATE DU PAIEMENT
    // =========================
    @Column(nullable = false)
    private LocalDateTime datePaiement;

    // =========================
    // RELATION AVEC LOCATION
    // =========================
    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    public Paiement() {}

    // =========================
    // GETTERS & SETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public ModePaiement getMode() {
        return mode;
    }

    public void setMode(ModePaiement mode) {
        this.mode = mode;
    }

    public StatutPaiement getStatut() {
        return statut;
    }

    public void setStatut(StatutPaiement statut) {
        this.statut = statut;
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDateTime datePaiement) {
        this.datePaiement = datePaiement;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
