package sn.uidt.locagest.locagest_backend.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import sn.uidt.locagest.locagest_backend.model.ContratLocation;
import sn.uidt.locagest.locagest_backend.service.ContratLocationService;

import java.util.List;

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
}
