package sn.uidt.locagest.locagest_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.uidt.locagest.locagest_backend.controller.ContratLocationController;
import sn.uidt.locagest.locagest_backend.dto.LocationDTO;
import sn.uidt.locagest.locagest_backend.exception.BusinessException;
import sn.uidt.locagest.locagest_backend.mapper.LocationMapper;
import sn.uidt.locagest.locagest_backend.model.ContratLocation;
import sn.uidt.locagest.locagest_backend.model.Location;
import sn.uidt.locagest.locagest_backend.model.StatutContrat;
import sn.uidt.locagest.locagest_backend.repository.ContratLocationRepository;
import sn.uidt.locagest.locagest_backend.repository.LocationRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
            // Générer un numéro de contrat automatique avec UUID
            String autoNumero = "CTR-" + UUID.randomUUID().toString().toUpperCase();
            request.setNumeroContrat(autoNumero);
        }

        // Créer le contrat
        ContratLocation contrat = new ContratLocation();
        contrat.setNumeroContrat(request.getNumeroContrat());
        contrat.setLocation(location);
        contrat.setDateCreation(request.getDateCreation() != null ? request.getDateCreation() : LocalDate.now());

        // Déterminer le statut automatiquement en fonction de la location
        if (request.getStatut() != null) {
            contrat.setStatut(request.getStatut());
        } else {
            // Statut automatique basé sur la date
            LocalDate today = LocalDate.now();
            if (location.getDateFin().isBefore(today)) {
                contrat.setStatut(StatutContrat.TERMINE);
            } else if (location.getDateDebut().isAfter(today)) {
                contrat.setStatut(StatutContrat.EN_ATTENTE);
            } else {
                contrat.setStatut(StatutContrat.ACTIF);
            }
        }

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
    //  RECHERCHER LES LOCATIONS SANS CONTRAT (BRUT)
    // =====================================================
    public List<Location> getLocationsSansContrat() {
        return locationRepository.findLocationsSansContrat();
    }

    // =====================================================
    //  RECHERCHER LES LOCATIONS SANS CONTRAT (FORMATÉES)
    // =====================================================
    public List<Map<String, Object>> getLocationsSansContratFormatted() {
        List<Location> locations = locationRepository.findLocationsSansContrat();

        return locations.stream().map(location -> {
            LocationDTO dto = LocationMapper.toDTO(location);

            // Formater la date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dateDebut = location.getDateDebut() != null ?
                    location.getDateDebut().format(formatter) : "N/A";
            String dateFin = location.getDateFin() != null ?
                    location.getDateFin().format(formatter) : "N/A";

            // Créer un label pour l'affichage dans le select
            String displayLabel = String.format("%s %s - %s %s (%s - %s) - %,.0f FCFA",
                    dto.getClientPrenom() != null ? dto.getClientPrenom() : "",
                    dto.getClientNom() != null ? dto.getClientNom() : "",
                    dto.getVehiculeMarque() != null ? dto.getVehiculeMarque() : "",
                    dto.getVehiculeModele() != null ? dto.getVehiculeModele() : "",
                    dateDebut,
                    dateFin,
                    dto.getMontantTotalLocation() != null ? dto.getMontantTotalLocation() : 0
            );

            // Utiliser HashMap au lieu de Map.of() pour supporter les valeurs null
            Map<String, Object> result = new HashMap<>();
            result.put("id", location.getId());
            result.put("displayLabel", displayLabel);
            result.put("clientPrenom", dto.getClientPrenom() != null ? dto.getClientPrenom() : "");
            result.put("clientNom", dto.getClientNom() != null ? dto.getClientNom() : "");
            result.put("vehiculeMarque", dto.getVehiculeMarque() != null ? dto.getVehiculeMarque() : "");
            result.put("vehiculeModele", dto.getVehiculeModele() != null ? dto.getVehiculeModele() : "");
            result.put("dateDebut", dateDebut);
            result.put("dateFin", dateFin);
            result.put("montantTotal", dto.getMontantTotalLocation() != null ? dto.getMontantTotalLocation() : 0);
            result.put("vehiculeId", dto.getVehiculeId() != null ? dto.getVehiculeId() : 0);
            result.put("clientId", dto.getClientId() != null ? dto.getClientId() : 0);

            return result;
        }).collect(Collectors.toList());
    }
}