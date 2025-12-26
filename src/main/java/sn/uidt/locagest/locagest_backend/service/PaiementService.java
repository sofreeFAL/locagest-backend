package sn.uidt.locagest.locagest_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.uidt.locagest.locagest_backend.dto.PaiementDTO;
import sn.uidt.locagest.locagest_backend.exception.BusinessException;
import sn.uidt.locagest.locagest_backend.model.*;
import sn.uidt.locagest.locagest_backend.repository.LocationRepository;
import sn.uidt.locagest.locagest_backend.repository.PaiementRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final LocationRepository locationRepository;

    public PaiementService(PaiementRepository paiementRepository,
                           LocationRepository locationRepository) {
        this.paiementRepository = paiementRepository;
        this.locationRepository = locationRepository;
    }

    // =====================================================
    //  PAYER UNE LOCATION
    // USER + ADMIN
    // =====================================================
    @Transactional
    public PaiementDTO payerLocation(Long locationId, String modePaiement) {

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new BusinessException("Location introuvable"));

        //  Interdire double paiement PAYÉ
        if (paiementRepository.existsByLocationIdAndStatut(
                locationId, StatutPaiement.PAYE)) {
            throw new BusinessException("Cette location est déjà payée");
        }

        Double montant = location.getMontantTotalLocation();
        if (montant == null || montant <= 0) {
            throw new BusinessException("Montant de location invalide");
        }

        ModePaiement mode;
        try {
            mode = ModePaiement.valueOf(modePaiement.toUpperCase());
        } catch (Exception e) {
            throw new BusinessException("Mode de paiement invalide");
        }

        Paiement paiement = new Paiement();
        paiement.setLocation(location);
        paiement.setMontant(montant);
        paiement.setMode(mode);
        paiement.setStatut(StatutPaiement.PAYE);
        paiement.setDatePaiement(LocalDateTime.now());

        return toDTO(paiementRepository.save(paiement));
    }

    // =====================================================
    // LISTER TOUS LES PAIEMENTS
    // USER + ADMIN
    // =====================================================
    public List<PaiementDTO> getAll() {
        return paiementRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // =====================================================
    //  PAIEMENTS D’UNE LOCATION
    // USER + ADMIN
    // =====================================================
    public List<PaiementDTO> getByLocation(Long locationId) {
        return paiementRepository.findAllByLocationId(locationId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // =====================================================
    //  PAIEMENTS PAR STATUT
    // USER + ADMIN
    // =====================================================
    public List<PaiementDTO> getByStatut(StatutPaiement statut) {
        return paiementRepository.findByStatut(statut)
                .stream()
                .map(this::toDTO)
                .toList();
    }


    // =====================================================
    // MAPPER Paiement → DTO
    // =====================================================
    private PaiementDTO toDTO(Paiement p) {
        PaiementDTO dto = new PaiementDTO();
        dto.setId(p.getId());
        dto.setLocationId(p.getLocation().getId());
        dto.setMontant(p.getMontant());
        dto.setModePaiement(p.getMode().name());
        dto.setStatut(p.getStatut().name());
        dto.setDatePaiement(p.getDatePaiement());
        return dto;
    }
}
