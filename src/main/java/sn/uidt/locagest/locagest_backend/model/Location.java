package sn.uidt.locagest.locagest_backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================
    // DATES
    // =========================
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    // =========================
    // PRIX FIGÉ DE LA LOCATION
    // =========================
    @Column(name = "montant_total_location", nullable = false)
    private Double montantTotalLocation;

    // =========================
    // STATUT
    // =========================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutLocation statut;

    // =========================
    // RELATIONS
    // =========================
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    // =========================
    // GETTERS & SETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public StatutLocation getStatut() {
        return statut;
    }

    public void setStatut(StatutLocation statut) {
        this.statut = statut;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }
}
