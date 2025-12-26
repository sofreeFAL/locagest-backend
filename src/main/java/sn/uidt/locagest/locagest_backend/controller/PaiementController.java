package sn.uidt.locagest.locagest_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import sn.uidt.locagest.locagest_backend.dto.PaiementDTO;
import sn.uidt.locagest.locagest_backend.model.Paiement;
import sn.uidt.locagest.locagest_backend.model.StatutPaiement;
import sn.uidt.locagest.locagest_backend.service.PaiementService;

import java.util.List;

@RestController
@RequestMapping("/paiements")
public class PaiementController {

    private final PaiementService paiementService;

    public PaiementController(PaiementService paiementService) {
        this.paiementService = paiementService;
    }

    // =====================================================
    //  PAYER UNE LOCATION
    // USER + ADMIN
    // =====================================================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/location/{locationId}")
    public ResponseEntity<PaiementDTO> payer(
            @PathVariable Long locationId,
            @RequestParam String modePaiement
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paiementService.payerLocation(locationId, modePaiement));
    }

    // =====================================================
    //  LISTER TOUS LES PAIEMENTS
    // USER + ADMIN
    // =====================================================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public List<PaiementDTO> getAll() {
        return paiementService.getAll();
    }

    // =====================================================
    //  PAIEMENTS D’UNE LOCATION
    // USER + ADMIN
    // =====================================================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/location/{locationId}")
    public List<PaiementDTO> getByLocation(@PathVariable Long locationId) {
        return paiementService.getByLocation(locationId);
    }

    // =====================================================
    //  PAIEMENTS PAR STATUT
    // USER + ADMIN
    // =====================================================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/statut/{statut}")
    public List<PaiementDTO> getByStatut(@PathVariable StatutPaiement statut) {
        return paiementService.getByStatut(statut);
    }


}
