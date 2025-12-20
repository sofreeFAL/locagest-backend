package sn.uidt.locagest.locagest_backend.controller;

import org.springframework.web.bind.annotation.*;
import sn.uidt.locagest.locagest_backend.model.Paiement;
import sn.uidt.locagest.locagest_backend.service.PaiementService;

@RestController
@RequestMapping("/paiements")
public class PaiementController {

    private final PaiementService paiementService;

    public PaiementController(PaiementService paiementService) {
        this.paiementService = paiementService;
    }

    @PostMapping("/payer")
    public Paiement payer(
            @RequestParam Long locationId,
            @RequestParam Double montant,
            @RequestParam String mode
    ) {
        return paiementService.payerLocation(locationId, montant, mode);
    }
}
