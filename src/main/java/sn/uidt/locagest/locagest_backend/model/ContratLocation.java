package sn.uidt.locagest.locagest_backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "contrats")
public class ContratLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String numeroContrat;

    @OneToOne
    @JoinColumn(name = "location_id", nullable = false, unique = true)
    private Location location;

    @Column(nullable = false)
    private LocalDate dateCreation;

    @Enumerated(EnumType.STRING)
    private StatutContrat statut;

    // ===== GETTERS / SETTERS =====
    public Long getId() { return id; }
    public String getNumeroContrat() { return numeroContrat; }
    public void setNumeroContrat(String numeroContrat) { this.numeroContrat = numeroContrat; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }
    public StatutContrat getStatut() { return statut; }
    public void setStatut(StatutContrat statut) { this.statut = statut; }
}
