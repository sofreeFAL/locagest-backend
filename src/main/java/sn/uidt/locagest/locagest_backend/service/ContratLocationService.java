package sn.uidt.locagest.locagest_backend.service;

import org.springframework.stereotype.Service;
import sn.uidt.locagest.locagest_backend.exception.BusinessException;
import sn.uidt.locagest.locagest_backend.model.ContratLocation;
import sn.uidt.locagest.locagest_backend.model.Location;
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
    //  GÉNÉRER LE CONTRAT PDF À PARTIR D’UNE LOCATION
    // =====================================================
    public byte[] genererContratPdf(Long locationId) {

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new BusinessException("Location introuvable"));

        // sécurité métier : contrat doit exister
        ContratLocation contrat = contratRepository
                .findByLocationId(locationId)
                .orElseThrow(() -> new BusinessException("Contrat introuvable pour cette location"));

        return pdfService.genererContrat(contrat);
    }
}
