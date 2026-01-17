package sn.uidt.locagest.locagest_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.uidt.locagest.locagest_backend.controller.ContratLocationController;
import sn.uidt.locagest.locagest_backend.exception.BusinessException;
import sn.uidt.locagest.locagest_backend.model.ContratLocation;
import sn.uidt.locagest.locagest_backend.model.Location;
import sn.uidt.locagest.locagest_backend.model.StatutContrat;
import sn.uidt.locagest.locagest_backend.repository.ContratLocationRepository;
import sn.uidt.locagest.locagest_backend.repository.LocationRepository;

import java.util.List;

@Service
public class ContratLocationService {

    private final LocationRepository locationRepository;
    private final ContratLocationRepository contratRepository;
    private final ContratPdfService pdfService;

    public ContratLocationService(
            LocationRepository locationRepository,
            ContratLocationRepository contratRepository,
            ContratPdfService pdfService
    ) {
        this.locationRepository = locationRepository;
        this.contratRepository = contratRepository;
        this.pdfService = pdfService;
    }

    // =====================================================
    //  LISTE DE TOUS LES CONTRATS
    // =====================================================
    public List<ContratLocation> getAllContrats() {
        return contratRepository.findAll();
    }

    // =====================================================
    //  RÉCUPÉRER UN CONTRAT PAR ID
    // =====================================================
    public ContratLocation getContratById(Long contratId) {
        return contratRepository.findById(contratId)
                .orElseThrow(() -> new BusinessException("Contrat introuvable"));
    }

    // =====================================================
    //  CRÉER UN CONTRAT
    // =====================================================
    @Transactional
    public ContratLocation createContrat(ContratLocationController.CreateContratRequest request) {
        // Vérifier que la location existe
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new BusinessException("Location introuvable"));

        // Vérifier si un contrat existe déjà pour cette location
        contratRepository.findByLocationId(request.getLocationId())
                .ifPresent(c -> {
                    throw new BusinessException("Un contrat existe déjà pour cette location");
                });

        // Vérifier que le numéro de contrat n'existe pas déjà
        if (request.getNumeroContrat() != null && !request.getNumeroContrat().isBlank()) {
            contratRepository.findByNumeroContrat(request.getNumeroContrat())
                    .ifPresent(c -> {
                        throw new BusinessException("Un contrat avec ce numéro existe déjà");
                    });
        } else {
            // Générer un numéro de contrat automatique
            String autoNumero = "CTR-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
            request.setNumeroContrat(autoNumero);
        }

        // Créer le contrat
        ContratLocation contrat = new ContratLocation();
        contrat.setNumeroContrat(request.getNumeroContrat());
        contrat.setLocation(location);
        contrat.setDateCreation(request.getDateCreation() != null ? request.getDateCreation() : java.time.LocalDate.now());
        contrat.setStatut(request.getStatut() != null ? request.getStatut() : StatutContrat.ACTIF);

        return contratRepository.save(contrat);
    }

    // =====================================================
    //  TERMINER UN CONTRAT
    // =====================================================
    @Transactional
    public ContratLocation terminerContrat(Long contratId) {
        ContratLocation contrat = contratRepository.findById(contratId)
                .orElseThrow(() -> new BusinessException("Contrat introuvable"));

        contrat.setStatut(StatutContrat.TERMINE);
        return contratRepository.save(contrat);
    }

    // =====================================================
    //  MODIFIER UN CONTRAT
    // =====================================================
    @Transactional
    public ContratLocation updateContrat(Long contratId, ContratLocationController.UpdateContratRequest request) {
        ContratLocation contrat = contratRepository.findById(contratId)
                .orElseThrow(() -> new BusinessException("Contrat introuvable"));

        // Vérifier l'unicité du numéro de contrat si modifié
        if (request.getNumeroContrat() != null &&
                !request.getNumeroContrat().isBlank() &&
                !request.getNumeroContrat().equals(contrat.getNumeroContrat())) {
            contratRepository.findByNumeroContrat(request.getNumeroContrat())
                    .ifPresent(c -> {
                        if (!c.getId().equals(contratId)) {
                            throw new BusinessException("Un autre contrat avec ce numéro existe déjà");
                        }
                    });
            contrat.setNumeroContrat(request.getNumeroContrat());
        }

        if (request.getStatut() != null) {
            contrat.setStatut(request.getStatut());
        }

        if (request.getDateCreation() != null) {
            contrat.setDateCreation(request.getDateCreation());
        }

        return contratRepository.save(contrat);
    }

    // =====================================================
    //  SUPPRIMER UN CONTRAT
    // =====================================================
    @Transactional
    public void deleteContrat(Long contratId) {
        ContratLocation contrat = contratRepository.findById(contratId)
                .orElseThrow(() -> new BusinessException("Contrat introuvable"));

        contratRepository.delete(contrat);
    }

    // =====================================================
    //  GÉNÉRER LE CONTRAT PDF À PARTIR D'UNE LOCATION
    // =====================================================
    public byte[] genererContratPdf(Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new BusinessException("Location introuvable"));

        // Vérifier que le contrat existe pour cette location
        ContratLocation contrat = contratRepository
                .findByLocationId(locationId)
                .orElseThrow(() -> new BusinessException("Contrat introuvable pour cette location"));

        return pdfService.genererContrat(contrat);
    }

    // =====================================================
    //  RECHERCHER LES LOCATIONS SANS CONTRAT
    // =====================================================
    public List<Location> getLocationsSansContrat() {
        return locationRepository.findLocationsSansContrat();
    }
}