package sn.uidt.locagest.locagest_backend.service;

import org.springframework.stereotype.Service;
import sn.uidt.locagest.locagest_backend.model.Location;
import sn.uidt.locagest.locagest_backend.model.Paiement;
import sn.uidt.locagest.locagest_backend.repository.LocationRepository;
import sn.uidt.locagest.locagest_backend.repository.PaiementRepository;

import java.time.LocalDateTime;

@Service
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final LocationRepository locationRepository;

    public PaiementService(PaiementRepository paiementRepository,
                           LocationRepository locationRepository) {
        this.paiementRepository = paiementRepository;
        this.locationRepository = locationRepository;
    }

    public Paiement payerLocation(Long locationId, Double montant, String mode) {

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location introuvable"));

        Paiement paiement = new Paiement();
        paiement.setLocation(location);
        paiement.setMontant(montant);
        paiement.setMode(mode);
        paiement.setDatePaiement(LocalDateTime.now());

        return paiementRepository.save(paiement);
    }
}
