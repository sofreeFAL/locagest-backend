package sn.uidt.locagest.locagest_backend.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.uidt.locagest.locagest_backend.model.ContratLocation;
import sn.uidt.locagest.locagest_backend.model.StatutContrat;
import sn.uidt.locagest.locagest_backend.service.ContratLocationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contrats")
public class ContratLocationController {

    private final ContratLocationService service;

    public ContratLocationController(ContratLocationService service) {
        this.service = service;
    }

    // =====================================================
    //  LISTE DE TOUS LES CONTRATS
    // USER + ADMIN
    // =====================================================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public ResponseEntity<List<ContratLocation>> getAllContrats() {
        return ResponseEntity.ok(service.getAllContrats());
    }

    // =====================================================
    //  RÉCUPÉRER UN CONTRAT PAR ID
    // USER + ADMIN
    // =====================================================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{contratId}")
    public ResponseEntity<ContratLocation> getContratById(
            @PathVariable Long contratId
    ) {
        return ResponseEntity.ok(service.getContratById(contratId));
    }

    // =====================================================
    //  CRÉER UN CONTRAT
    // USER + ADMIN
    // =====================================================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping
    public ResponseEntity<ContratLocation> createContrat(@RequestBody CreateContratRequest request) {
        return ResponseEntity.ok(service.createContrat(request));
    }

    // =====================================================
    //  TERMINER UN CONTRAT
    // USER + ADMIN
    // =====================================================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/{contratId}/terminer")
    public ResponseEntity<ContratLocation> terminerContrat(@PathVariable Long contratId) {
        return ResponseEntity.ok(service.terminerContrat(contratId));
    }

    // =====================================================
    //  EXPORT CONTRAT PDF PAR LOCATION
    // USER + ADMIN
    // =====================================================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/locations/{locationId}/pdf")
    public ResponseEntity<byte[]> genererContratPdf(
            @PathVariable Long locationId
    ) {
        byte[] pdf = service.genererContratPdf(locationId);
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=contrat-location-" + locationId + ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // =====================================================
    //  MODIFIER UN CONTRAT
    // USER + ADMIN
    // =====================================================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/{contratId}")
    public ResponseEntity<ContratLocation> updateContrat(
            @PathVariable Long contratId,
            @RequestBody UpdateContratRequest request
    ) {
        return ResponseEntity.ok(service.updateContrat(contratId, request));
    }

    // =====================================================
    //  SUPPRIMER UN CONTRAT
    // ADMIN SEULEMENT
    // =====================================================
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{contratId}")
    public ResponseEntity<Void> deleteContrat(@PathVariable Long contratId) {
        service.deleteContrat(contratId);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    //  LISTE DES LOCATIONS SANS CONTRAT (FORMATÉES)
    // USER + ADMIN
    // =====================================================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/locations/sans-contrat")
    public ResponseEntity<List<Map<String, Object>>> getLocationsSansContrat() {
        return ResponseEntity.ok(service.getLocationsSansContratFormatted());
    }

    // =====================================================
    //  DTO POUR LA CRÉATION
    // =====================================================
    public static class CreateContratRequest {
        private String numeroContrat;
        private Long locationId;
        private LocalDate dateCreation;
        private StatutContrat statut;

        // Getters et Setters
        public String getNumeroContrat() {
            return numeroContrat;
        }

        public void setNumeroContrat(String numeroContrat) {
            this.numeroContrat = numeroContrat;
        }

        public Long getLocationId() {
            return locationId;
        }

        public void setLocationId(Long locationId) {
            this.locationId = locationId;
        }

        public LocalDate getDateCreation() {
            return dateCreation;
        }

        public void setDateCreation(LocalDate dateCreation) {
            this.dateCreation = dateCreation;
        }

        public StatutContrat getStatut() {
            return statut;
        }

        public void setStatut(StatutContrat statut) {
            this.statut = statut;
        }
    }

    // =====================================================
    //  DTO POUR LA MODIFICATION
    // =====================================================
    public static class UpdateContratRequest {
        private String numeroContrat;
        private StatutContrat statut;
        private LocalDate dateCreation;

        // Getters et Setters
        public String getNumeroContrat() {
            return numeroContrat;
        }

        public void setNumeroContrat(String numeroContrat) {
            this.numeroContrat = numeroContrat;
        }

        public StatutContrat getStatut() {
            return statut;
        }

        public void setStatut(StatutContrat statut) {
            this.statut = statut;
        }

        public LocalDate getDateCreation() {
            return dateCreation;
        }

        public void setDateCreation(LocalDate dateCreation) {
            this.dateCreation = dateCreation;
        }
    }
}