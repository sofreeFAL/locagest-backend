package sn.uidt.locagest.locagest_backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.uidt.locagest.locagest_backend.dto.LocationDTO;
import sn.uidt.locagest.locagest_backend.dto.LocationSearchDTO;
import sn.uidt.locagest.locagest_backend.exception.BusinessException;
import sn.uidt.locagest.locagest_backend.model.*;
import sn.uidt.locagest.locagest_backend.repository.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final ClientRepository clientRepository;
    private final VehiculeRepository vehiculeRepository;
    private final PaiementRepository paiementRepository;
    private final ContratLocationRepository contratRepository;

    public LocationService(
            LocationRepository locationRepository,
            ClientRepository clientRepository,
            VehiculeRepository vehiculeRepository,
            PaiementRepository paiementRepository,
            ContratLocationRepository contratRepository
    ) {
        this.locationRepository = locationRepository;
        this.clientRepository = clientRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.paiementRepository = paiementRepository;
        this.contratRepository = contratRepository;
    }

    // =====================================================
    //  CRÉER UNE LOCATION + CONTRAT AUTO
    // =====================================================
    @Transactional
    public Location create(LocationDTO dto) {
        if (dto.getClientId() == null || dto.getVehiculeId() == null) {
            throw new BusinessException("Client et véhicule obligatoires");
        }

        if (dto.getDateDebut() == null || dto.getDateFin() == null) {
            throw new BusinessException("Dates de location obligatoires");
        }

        if (dto.getDateFin().isBefore(dto.getDateDebut())) {
            throw new BusinessException("Date de fin invalide");
        }

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new BusinessException("Client introuvable"));

        Vehicule vehicule = vehiculeRepository.findById(dto.getVehiculeId())
                .orElseThrow(() -> new BusinessException("Véhicule introuvable"));

        // Vérifier si le véhicule est disponible
        if (!"DISPONIBLE".equals(vehicule.getStatut())) {
            throw new BusinessException("Véhicule non disponible (statut: " + vehicule.getStatut() + ")");
        }

        long nbJours = ChronoUnit.DAYS.between(dto.getDateDebut(), dto.getDateFin());
        if (nbJours <= 0) nbJours = 1;

        double montantTotal = nbJours * vehicule.getPrixParJour();

        // Calculer le statut initial
        LocalDate today = LocalDate.now();
        StatutLocation statutInitial = calculateStatus(dto.getDateDebut(), dto.getDateFin(), today);

        // Créer la location
        Location location = new Location();
        location.setClient(client);
        location.setVehicule(vehicule);
        location.setDateDebut(dto.getDateDebut());
        location.setDateFin(dto.getDateFin());
        location.setMontantTotalLocation(montantTotal);
        location.setStatut(statutInitial);

        // Mettre à jour le statut du véhicule
        updateVehicleStatusBasedOnLocation(vehicule, statutInitial);

        Location savedLocation = locationRepository.save(location);

        // Créer le contrat
        ContratLocation contrat = new ContratLocation();
        contrat.setLocation(savedLocation);
        contrat.setNumeroContrat("CTR-" + UUID.randomUUID());
        contrat.setDateCreation(LocalDate.now());

        // Déterminer le statut du contrat
        if (statutInitial == StatutLocation.A_VENIR) {
            contrat.setStatut(StatutContrat.EN_ATTENTE);
        } else if (statutInitial == StatutLocation.EN_COURS) {
            contrat.setStatut(StatutContrat.ACTIF);
        } else if (statutInitial == StatutLocation.TERMINEE) {
            contrat.setStatut(StatutContrat.TERMINE);
        } else {
            contrat.setStatut(StatutContrat.ACTIF);
        }

        contratRepository.save(contrat);

        return savedLocation;
    }

    // =====================================================
    //  LISTER TOUTES LES LOCATIONS (AVEC RECALCUL DES STATUTS)
    // =====================================================
    public List<Location> getAll() {
        List<Location> locations = locationRepository.findAll();
        LocalDate today = LocalDate.now();

        // Recalculer les statuts avant de retourner
        for (Location location : locations) {
            if (location.getStatut() != StatutLocation.ANNULEE) {
                StatutLocation newStatut = calculateStatus(location.getDateDebut(),
                        location.getDateFin(),
                        today);
                if (!location.getStatut().equals(newStatut)) {
                    updateLocationAndVehicleStatus(location, newStatut);
                }
            }
        }

        return locationRepository.findAll();
    }

    // =====================================================
    //  LISTER LES LOCATIONS EN COURS
    // =====================================================
    public List<Location> getLocationsEnCours() {
        // D'abord mettre à jour tous les statuts
        updateLocationsStatus();

        return locationRepository.findByStatut(StatutLocation.EN_COURS);
    }

    // =====================================================
    //  LISTER LES LOCATIONS À VENIR
    // =====================================================
    public List<Location> getLocationsAVenir() {
        updateLocationsStatus();
        return locationRepository.findByStatut(StatutLocation.A_VENIR);
    }

    // =====================================================
    //  HISTORIQUE (LOCATIONS TERMINÉES)
    // =====================================================
    public List<Location> getHistorique() {
        updateLocationsStatus();
        return locationRepository.findByStatut(StatutLocation.TERMINEE);
    }

    public List<Location> getHistoriqueParClient(Long clientId) {
        updateLocationsStatus();
        return locationRepository.findHistoriqueParClient(clientId);
    }

    public List<Location> getHistoriqueParVehicule(Long vehiculeId) {
        updateLocationsStatus();
        return locationRepository.findHistoriqueParVehicule(vehiculeId);
    }

    // =====================================================
    //  RECHERCHE AVANCÉE
    // =====================================================
    public List<Location> searchAdvanced(LocationSearchDTO dto) {
        // Mettre à jour les statuts avant recherche
        updateLocationsStatus();

        return locationRepository.searchAdvanced(
                dto.getClientId(),
                dto.getVehiculeId(),
                dto.getStatut() == null ? null : StatutLocation.valueOf(dto.getStatut()),
                dto.getDateDebutMin(),
                dto.getDateDebutMax(),
                dto.getDateFinMin(),
                dto.getDateFinMax(),
                dto.getMontantMin(),
                dto.getMontantMax()
        );
    }

    // =====================================================
    //  MODIFIER PRIX (ADMIN) - UNIQUEMENT POUR LOCATIONS À VENIR
    // =====================================================
    @Transactional
    public Location modifierPrixLocation(Long id, Double nouveauMontant) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Location introuvable"));

        // Recalculer le statut avant vérification
        LocalDate today = LocalDate.now();
        StatutLocation currentStatus = calculateStatus(location.getDateDebut(),
                location.getDateFin(),
                today);

        if (currentStatus != StatutLocation.A_VENIR) {
            throw new BusinessException("Seules les locations À VENIR peuvent être modifiées");
        }

        if (nouveauMontant == null || nouveauMontant <= 0) {
            throw new BusinessException("Montant invalide");
        }

        location.setMontantTotalLocation(nouveauMontant);
        return locationRepository.save(location);
    }

    // =====================================================
    //  PROLONGER (ADMIN) - UNIQUEMENT POUR LOCATIONS EN COURS
    // =====================================================
    @Transactional
    public Location prolongerLocation(Long id, LocalDate nouvelleDateFin) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Location introuvable"));

        // Recalculer le statut
        LocalDate today = LocalDate.now();
        StatutLocation currentStatus = calculateStatus(location.getDateDebut(),
                location.getDateFin(),
                today);

        if (currentStatus == StatutLocation.TERMINEE ||
                currentStatus == StatutLocation.ANNULEE) {
            throw new BusinessException("Location terminée ou annulée");
        }

        if (currentStatus != StatutLocation.EN_COURS) {
            throw new BusinessException("Seules les locations EN COURS peuvent être prolongées");
        }

        if (nouvelleDateFin.isBefore(location.getDateFin())) {
            throw new BusinessException("La nouvelle date doit être après la date de fin actuelle");
        }

        long joursAjoutes = ChronoUnit.DAYS.between(location.getDateFin(), nouvelleDateFin);

        if (joursAjoutes <= 0) {
            throw new BusinessException("Aucun jour ajouté");
        }

        double supplement = joursAjoutes * location.getVehicule().getPrixParJour();

        location.setDateFin(nouvelleDateFin);
        location.setMontantTotalLocation(location.getMontantTotalLocation() + supplement);

        return locationRepository.save(location);
    }

    // =====================================================
    //  RETOUR VÉHICULE + FIN CONTRAT (ADMIN)
    // =====================================================
    @Transactional
    public Location retourVehicule(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Location introuvable"));

        if (location.getStatut() == StatutLocation.TERMINEE ||
                location.getStatut() == StatutLocation.ANNULEE) {
            throw new BusinessException("Location déjà terminée ou annulée");
        }

        // Vérifier que la location est EN COURS
        if (location.getStatut() != StatutLocation.EN_COURS) {
            throw new BusinessException("Seules les locations EN COURS peuvent être retournées");
        }

        // Vérifier si le paiement a été effectué
        boolean paiementEffectue = paiementRepository.existsByLocationIdAndStatut(id, StatutPaiement.PAYE);

        if (!paiementEffectue) {
            throw new BusinessException("Retour refusé : paiement non effectué");
        }

        // Libérer le véhicule
        location.getVehicule().setStatut("DISPONIBLE");
        location.getVehicule().setDisponible(true);
        location.setStatut(StatutLocation.TERMINEE);

        // Mettre à jour le contrat
        ContratLocation contrat = contratRepository.findByLocationId(id)
                .orElseThrow(() -> new BusinessException("Contrat introuvable"));

        contrat.setStatut(StatutContrat.TERMINE);
        contratRepository.save(contrat);

        return locationRepository.save(location);
    }
    // =====================================================
//  RÉPARER LA SYNCHRONISATION LOCATIONS-VÉHICULES
// =====================================================
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public String reparerSynchronisation() {
        List<Vehicule> vehicules = vehiculeRepository.findAll();
        int corrections = 0;

        for (Vehicule vehicule : vehicules) {
            // Trouver toutes les locations pour ce véhicule
            List<Location> locations = locationRepository.findAll().stream()
                    .filter(l -> l.getVehicule().getId().equals(vehicule.getId()))
                    .filter(l -> l.getStatut() != StatutLocation.ANNULEE)
                    .collect(Collectors.toList());

            // Vérifier s'il y a une location en cours
            boolean hasLocationEnCours = locations.stream()
                    .anyMatch(l -> l.getStatut() == StatutLocation.EN_COURS);

            if (hasLocationEnCours) {
                // Véhicule devrait être LOUÉ
                if (!"LOUE".equals(vehicule.getStatut())) {
                    vehicule.setStatut("LOUE");
                    vehiculeRepository.save(vehicule);
                    corrections++;
                }
            } else {
                // Véhicule devrait être DISPONIBLE (sauf s'il est en maintenance)
                if ("LOUE".equals(vehicule.getStatut())) {
                    vehicule.setStatut("DISPONIBLE");
                    vehiculeRepository.save(vehicule);
                    corrections++;
                }
            }
        }

        return corrections + " véhicules corrigés sur " + vehicules.size();
    }
    // =====================================================
    //  ANNULER UNE LOCATION (ADMIN)
    // =====================================================
    @Transactional
    public Location annulerLocation(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Location introuvable"));

        if (location.getStatut() == StatutLocation.TERMINEE ||
                location.getStatut() == StatutLocation.ANNULEE) {
            throw new BusinessException("Location déjà terminée ou annulée");
        }

        // Libérer le véhicule si la location était EN_COURS
        if (location.getStatut() == StatutLocation.EN_COURS) {
            location.getVehicule().setStatut("DISPONIBLE");
            location.getVehicule().setDisponible(true);
        }

        location.setStatut(StatutLocation.ANNULEE);

        // Annuler le contrat
        ContratLocation contrat = contratRepository.findByLocationId(id)
                .orElseThrow(() -> new BusinessException("Contrat introuvable"));

        contrat.setStatut(StatutContrat.ANNULE);
        contratRepository.save(contrat);

        return locationRepository.save(location);
    }

    // =====================================================
    //  DÉMARRER UNE LOCATION (passer de À VENIR à EN COURS)
    // =====================================================
    @Transactional
    public Location demarrerLocation(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Location introuvable"));

        LocalDate today = LocalDate.now();
        StatutLocation currentStatus = calculateStatus(location.getDateDebut(),
                location.getDateFin(),
                today);

        if (currentStatus != StatutLocation.A_VENIR) {
            throw new BusinessException("Seules les locations À VENIR peuvent être démarrées");
        }

        if (location.getDateDebut().isAfter(today)) {
            throw new BusinessException("La date de début n'est pas encore arrivée");
        }

        // Marquer le véhicule comme loué
        location.getVehicule().setStatut("LOUE");
        location.getVehicule().setDisponible(false);
        location.setStatut(StatutLocation.EN_COURS);

        // Activer le contrat
        ContratLocation contrat = contratRepository.findByLocationId(id)
                .orElseThrow(() -> new BusinessException("Contrat introuvable"));

        contrat.setStatut(StatutContrat.ACTIF);
        contratRepository.save(contrat);

        return locationRepository.save(location);
    }

    // =====================================================
    //  MISE À JOUR AUTOMATIQUE DES STATUTS (EXÉCUTÉE TOUTES LES HEURES)
    // =====================================================
    @Scheduled(cron = "0 0 * * * ?") // Exécuté toutes les heures à la minute 0
    @Transactional
    public void updateLocationsStatus() {
        List<Location> locations = locationRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Location location : locations) {
            // Ne pas modifier les locations annulées
            if (location.getStatut() == StatutLocation.ANNULEE) {
                continue;
            }

            StatutLocation newStatut = calculateStatus(location.getDateDebut(),
                    location.getDateFin(),
                    today);

            // Ne mettre à jour que si le statut a changé
            if (!location.getStatut().equals(newStatut)) {
                updateLocationAndVehicleStatus(location, newStatut);
            }
        }
    }

    // =====================================================
    //  MODIFIER UNE LOCATION (ADMIN)
    // =====================================================
    @Transactional
    public Location updateLocation(Long id, LocationDTO dto) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Location introuvable"));

        // Recalculer le statut actuel
        LocalDate today = LocalDate.now();
        StatutLocation currentStatus = calculateStatus(location.getDateDebut(),
                location.getDateFin(),
                today);

        if (currentStatus != StatutLocation.A_VENIR) {
            throw new BusinessException("Seules les locations À VENIR peuvent être modifiées");
        }

        // Validation des données
        if (dto.getDateDebut() == null || dto.getDateFin() == null) {
            throw new BusinessException("Dates de location obligatoires");
        }

        if (dto.getDateFin().isBefore(dto.getDateDebut())) {
            throw new BusinessException("Date de fin invalide");
        }

        // Mettre à jour le client si fourni
        if (dto.getClientId() != null) {
            Client client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new BusinessException("Client introuvable"));
            location.setClient(client);
        }

        // Mettre à jour le véhicule si fourni
        if (dto.getVehiculeId() != null) {
            Vehicule nouveauVehicule = vehiculeRepository.findById(dto.getVehiculeId())
                    .orElseThrow(() -> new BusinessException("Véhicule introuvable"));

            // Vérifier si le véhicule est disponible
            if (!"DISPONIBLE".equals(nouveauVehicule.getStatut())) {
                throw new BusinessException("Véhicule non disponible (statut: " + nouveauVehicule.getStatut() + ")");
            }

            // Libérer l'ancien véhicule s'il est différent
            if (!location.getVehicule().getId().equals(nouveauVehicule.getId())) {
                location.getVehicule().setStatut("DISPONIBLE");
                location.getVehicule().setDisponible(true);
                vehiculeRepository.save(location.getVehicule());
            }

            location.setVehicule(nouveauVehicule);
        }

        // Mettre à jour les dates
        location.setDateDebut(dto.getDateDebut());
        location.setDateFin(dto.getDateFin());

        // Recalculer le montant
        long nbJours = ChronoUnit.DAYS.between(dto.getDateDebut(), dto.getDateFin());
        if (nbJours <= 0) nbJours = 1;

        double montantTotal = nbJours * location.getVehicule().getPrixParJour();
        location.setMontantTotalLocation(montantTotal);

        // Recalculer le statut en fonction des nouvelles dates
        StatutLocation newStatut = calculateStatus(dto.getDateDebut(), dto.getDateFin(), today);
        location.setStatut(newStatut);

        // Mettre à jour le statut du véhicule
        updateVehicleStatusBasedOnLocation(location.getVehicule(), newStatut);

        return locationRepository.save(location);
    }

    // =====================================================
    //  SUPPRIMER UNE LOCATION (ADMIN)
    // =====================================================
    @Transactional
    public void deleteLocation(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Location introuvable"));

        // Recalculer le statut actuel
        LocalDate today = LocalDate.now();
        StatutLocation currentStatus = calculateStatus(location.getDateDebut(),
                location.getDateFin(),
                today);

        if (currentStatus != StatutLocation.A_VENIR) {
            throw new BusinessException("Seules les locations À VENIR peuvent être supprimées");
        }

        // Supprimer le contrat associé s'il existe
        contratRepository.findByLocationId(id).ifPresent(contratRepository::delete);

        // Supprimer la location
        locationRepository.delete(location);
    }

    // =====================================================
    //  CALCULER LE STATUT D'UNE LOCATION
    // =====================================================
    private StatutLocation calculateStatus(LocalDate dateDebut, LocalDate dateFin, LocalDate today) {
        if (dateDebut.isAfter(today)) {
            return StatutLocation.A_VENIR;
        } else if (dateFin.isBefore(today)) {
            return StatutLocation.TERMINEE;
        } else {
            return StatutLocation.EN_COURS;
        }
    }

    // =====================================================
    //  METTRE À JOUR LE STATUT DU CONTRAT
    // =====================================================
    private void updateContratStatus(Location location, StatutLocation newLocationStatut) {
        try {
            ContratLocation contrat = contratRepository.findByLocationId(location.getId())
                    .orElse(null);

            if (contrat != null) {
                if (newLocationStatut == StatutLocation.A_VENIR) {
                    contrat.setStatut(StatutContrat.EN_ATTENTE);
                } else if (newLocationStatut == StatutLocation.EN_COURS) {
                    contrat.setStatut(StatutContrat.ACTIF);
                } else if (newLocationStatut == StatutLocation.TERMINEE) {
                    contrat.setStatut(StatutContrat.TERMINE);
                } else if (newLocationStatut == StatutLocation.ANNULEE) {
                    contrat.setStatut(StatutContrat.ANNULE);
                }
                contratRepository.save(contrat);
            }
        } catch (Exception e) {
            System.err.println("Erreur mise à jour contrat pour location " + location.getId() + ": " + e.getMessage());
        }
    }

    // =====================================================
    //  METTRE À JOUR LE STATUT DU VÉHICULE
    // =====================================================
    private void updateVehicleStatusBasedOnLocation(Vehicule vehicule, StatutLocation locationStatut) {
        if (vehicule == null) return;

        if (locationStatut == StatutLocation.EN_COURS) {
            // La location est en cours
            vehicule.setStatut("LOUE");
            vehicule.setDisponible(false);
        } else if (locationStatut == StatutLocation.TERMINEE ||
                locationStatut == StatutLocation.ANNULEE) {
            // La location est terminée ou annulée
            vehicule.setStatut("DISPONIBLE");
            vehicule.setDisponible(true);
        }
        // Pour A_VENIR, on ne change pas le statut du véhicule (il reste DISPONIBLE)
    }

    // =====================================================
//  METTRE À JOUR LOCATION ET VÉHICULE
// =====================================================
    private void updateLocationAndVehicleStatus(Location location, StatutLocation newStatut) {
        if (location == null) return;

        StatutLocation oldStatut = location.getStatut();

        // Mettre à jour le statut de la location
        location.setStatut(newStatut);

        // Mettre à jour le statut du véhicule seulement s'il a changé
        if (!oldStatut.equals(newStatut)) {
            updateVehicleStatusBasedOnLocation(location.getVehicule(), newStatut);
        }

        // Sauvegarder la location
        locationRepository.save(location);

        // Mettre à jour le statut du contrat
        updateContratStatus(location, newStatut);
    }

    // =====================================================
    //  TROUVER UNE LOCATION PAR ID (AVEC RECALCUL DU STATUT)
    // =====================================================
    public Location findById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Location introuvable"));

        // Recalculer le statut si nécessaire
        if (location.getStatut() != StatutLocation.ANNULEE) {
            LocalDate today = LocalDate.now();
            StatutLocation newStatut = calculateStatus(location.getDateDebut(),
                    location.getDateFin(),
                    today);
            if (!location.getStatut().equals(newStatut)) {
                updateLocationAndVehicleStatus(location, newStatut);
            }
        }

        return location;
    }

    // =====================================================
    //  VÉRIFIER LES CONFLITS DE LOCATION POUR UN VÉHICULE
    // =====================================================
    public boolean hasLocationConflict(Long vehiculeId, LocalDate dateDebut, LocalDate dateFin) {
        List<Location> locations = locationRepository.findAll();

        for (Location location : locations) {
            if (location.getVehicule().getId().equals(vehiculeId) &&
                    location.getStatut() != StatutLocation.ANNULEE &&
                    location.getStatut() != StatutLocation.TERMINEE) {

                boolean chevauchement =
                        (dateDebut.isBefore(location.getDateFin()) || dateDebut.isEqual(location.getDateFin())) &&
                                (dateFin.isAfter(location.getDateDebut()) || dateFin.isEqual(location.getDateDebut()));

                if (chevauchement) {
                    return true;
                }
            }
        }
        return false;
    }
}