package sn.uidt.locagest.locagest_backend.service;

import org.springframework.scheduling.annotation.Scheduled;
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

        // Vérifier si le véhicule est disponible (pas déjà loué ET pas en maintenance)
        if (!vehicule.isDisponible()) {
            throw new BusinessException("Véhicule non disponible (déjà loué ou en maintenance)");
        }

        long nbJours = ChronoUnit.DAYS.between(
                dto.getDateDebut(),
                dto.getDateFin()
        );
        if (nbJours <= 0) nbJours = 1;

        double montantTotal = nbJours * vehicule.getPrixParJour();

        // --- DÉTERMINER LE STATUT INITIAL ---
        LocalDate today = LocalDate.now();
        StatutLocation statutInitial;

        if (dto.getDateDebut().isAfter(today)) {
            // Date de début dans le futur = À VENIR
            statutInitial = StatutLocation.A_VENIR;
        } else if (dto.getDateDebut().isEqual(today) ||
                (dto.getDateDebut().isBefore(today) && dto.getDateFin().isAfter(today))) {
            // Commence aujourd'hui ou est en cours = EN COURS
            statutInitial = StatutLocation.EN_COURS;
        } else if (dto.getDateFin().isBefore(today)) {
            // Date de fin déjà passée = TERMINÉE
            statutInitial = StatutLocation.TERMINEE;
        } else {
            // Par défaut
            statutInitial = StatutLocation.EN_COURS;
        }

        // --- LOCATION ---
        Location location = new Location();
        location.setClient(client);
        location.setVehicule(vehicule);
        location.setDateDebut(dto.getDateDebut());
        location.setDateFin(dto.getDateFin());
        location.setMontantTotalLocation(montantTotal);
        location.setStatut(statutInitial); // Utiliser le statut calculé

        // IMPORTANT : Marquer le véhicule comme indisponible seulement si la location est EN_COURS
        // Si c'est A_VENIR, le véhicule reste disponible pour d'autres locations futures
        if (statutInitial == StatutLocation.EN_COURS) {
            vehicule.setDisponible(false);
        }

        Location savedLocation = locationRepository.save(location);

        // --- CONTRAT AUTO ---
        ContratLocation contrat = new ContratLocation();
        contrat.setLocation(savedLocation);
        contrat.setNumeroContrat("CTR-" + UUID.randomUUID());
        contrat.setDateCreation(LocalDate.now());

        // Déterminer le statut du contrat en fonction du statut de la location
        if (statutInitial == StatutLocation.A_VENIR) {
            contrat.setStatut(StatutContrat.EN_ATTENTE); // Contrat en attente
        } else if (statutInitial == StatutLocation.EN_COURS) {
            contrat.setStatut(StatutContrat.ACTIF); // Contrat actif
        } else if (statutInitial == StatutLocation.TERMINEE) {
            contrat.setStatut(StatutContrat.TERMINE); // Contrat terminé
        } else {
            contrat.setStatut(StatutContrat.ACTIF); // Par défaut
        }

        contratRepository.save(contrat);

        return savedLocation;
    }

    // =====================================================
    //  LISTER TOUTES LES LOCATIONS
    // =====================================================
    public List<Location> getAll() {
        return locationRepository.findAll();
    }

    // =====================================================
    //  LISTER LES LOCATIONS EN COURS
    // =====================================================
    public List<Location> getLocationsEnCours() {
        return locationRepository.findByStatut(StatutLocation.EN_COURS);
    }

    // =====================================================
    //  LISTER LES LOCATIONS À VENIR
    // =====================================================
    public List<Location> getLocationsAVenir() {
        return locationRepository.findByStatut(StatutLocation.A_VENIR);
    }

    // =====================================================
    //  HISTORIQUE (LOCATIONS TERMINÉES)
    // =====================================================
    public List<Location> getHistorique() {
        return locationRepository.findByStatut(StatutLocation.TERMINEE);
    }

    public List<Location> getHistoriqueParClient(Long clientId) {
        return locationRepository.findHistoriqueParClient(clientId);
    }

    public List<Location> getHistoriqueParVehicule(Long vehiculeId) {
        return locationRepository.findHistoriqueParVehicule(vehiculeId);
    }

    // =====================================================
    //  RECHERCHE AVANCÉE
    // =====================================================
    public List<Location> searchAdvanced(LocationSearchDTO dto) {
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

        // Vérifier que la location est À VENIR
        if (location.getStatut() != StatutLocation.A_VENIR) {
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

        if (location.getStatut() == StatutLocation.TERMINEE ||
                location.getStatut() == StatutLocation.ANNULEE) {
            throw new BusinessException("Location terminée ou annulée");
        }

        // Vérifier que la prolongation est pour une location EN COURS
        if (location.getStatut() != StatutLocation.EN_COURS) {
            throw new BusinessException("Seules les locations EN COURS peuvent être prolongées");
        }

        if (nouvelleDateFin.isBefore(location.getDateFin())) {
            throw new BusinessException("La nouvelle date doit être après la date de fin actuelle");
        }

        long joursAjoutes = ChronoUnit.DAYS.between(
                location.getDateFin(),
                nouvelleDateFin
        );

        if (joursAjoutes <= 0) {
            throw new BusinessException("Aucun jour ajouté");
        }

        double supplement = joursAjoutes * location.getVehicule().getPrixParJour();

        location.setDateFin(nouvelleDateFin);
        location.setMontantTotalLocation(
                location.getMontantTotalLocation() + supplement
        );

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
        boolean paiementEffectue =
                paiementRepository.existsByLocationIdAndStatut(id, StatutPaiement.PAYE);

        if (!paiementEffectue) {
            throw new BusinessException("Retour refusé : paiement non effectué");
        }

        // Libérer le véhicule
        location.getVehicule().setDisponible(true);
        location.setStatut(StatutLocation.TERMINEE);

        // --- FIN CONTRAT ---
        ContratLocation contrat = contratRepository
                .findByLocationId(id)
                .orElseThrow(() -> new BusinessException("Contrat introuvable"));

        contrat.setStatut(StatutContrat.TERMINE);
        contratRepository.save(contrat);

        return locationRepository.save(location);
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
            location.getVehicule().setDisponible(true);
        }

        location.setStatut(StatutLocation.ANNULEE);

        // --- ANNULER LE CONTRAT ---
        ContratLocation contrat = contratRepository
                .findByLocationId(id)
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

        if (location.getStatut() != StatutLocation.A_VENIR) {
            throw new BusinessException("Seules les locations À VENIR peuvent être démarrées");
        }

        LocalDate today = LocalDate.now();
        if (location.getDateDebut().isAfter(today)) {
            throw new BusinessException("La date de début n'est pas encore arrivée");
        }

        // Marquer le véhicule comme indisponible
        location.getVehicule().setDisponible(false);
        location.setStatut(StatutLocation.EN_COURS);

        // --- ACTIVER LE CONTRAT ---
        ContratLocation contrat = contratRepository
                .findByLocationId(id)
                .orElseThrow(() -> new BusinessException("Contrat introuvable"));

        contrat.setStatut(StatutContrat.ACTIF);
        contratRepository.save(contrat);

        return locationRepository.save(location);
    }

    // =====================================================
    //  MISE À JOUR AUTOMATIQUE DES STATUTS
    // =====================================================
    @Scheduled(cron = "0 0 0 * * ?") // Exécuté tous les jours à minuit
    @Transactional
    public void updateLocationsStatus() {
        List<Location> locations = locationRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Location location : locations) {
            // Ne pas modifier les locations annulées
            if (location.getStatut() == StatutLocation.ANNULEE) {
                continue;
            }

            StatutLocation newStatut = calculateStatus(location, today);

            // Ne mettre à jour que si le statut a changé
            if (!location.getStatut().equals(newStatut)) {

                // Gérer la disponibilité du véhicule
                if (location.getVehicule() != null) {
                    if (newStatut == StatutLocation.EN_COURS &&
                            location.getStatut() == StatutLocation.A_VENIR) {
                        // La location passe de À VENIR à EN COURS
                        location.getVehicule().setDisponible(false);
                    } else if (newStatut == StatutLocation.TERMINEE) {
                        // La location se termine
                        location.getVehicule().setDisponible(true);
                    }
                }

                location.setStatut(newStatut);
                locationRepository.save(location);

                // Mettre à jour le statut du contrat si nécessaire
                updateContratStatus(location, newStatut);
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

        // Vérifier que la location est À VENIR
        if (location.getStatut() != StatutLocation.A_VENIR) {
            throw new BusinessException("Seules les locations À VENIR peuvent être modifiées");
        }

        // Validation des données
        if (dto.getDateDebut() == null || dto.getDateFin() == null) {
            throw new BusinessException("Dates de location obligatoires");
        }

        // CORRECTION : Comparer dateDebut et dateFin (bug dans votre code)
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
            Vehicule vehicule = vehiculeRepository.findById(dto.getVehiculeId())
                    .orElseThrow(() -> new BusinessException("Véhicule introuvable"));

            // Vérifier si le véhicule est disponible (sauf si c'est le même véhicule)
            if (!location.getVehicule().getId().equals(vehicule.getId()) && !vehicule.isDisponible()) {
                throw new BusinessException("Véhicule non disponible");
            }

            // Libérer l'ancien véhicule
            if (!location.getVehicule().getId().equals(vehicule.getId())) {
                location.getVehicule().setDisponible(true);
            }

            location.setVehicule(vehicule);
        }

        // Mettre à jour les dates
        location.setDateDebut(dto.getDateDebut());
        location.setDateFin(dto.getDateFin());

        // Recalculer le montant
        long nbJours = ChronoUnit.DAYS.between(
                dto.getDateDebut(),
                dto.getDateFin()
        );
        if (nbJours <= 0) nbJours = 1;

        double montantTotal = nbJours * location.getVehicule().getPrixParJour();
        location.setMontantTotalLocation(montantTotal);

        // Recalculer le statut en fonction des nouvelles dates
        LocalDate today = LocalDate.now();
        if (dto.getDateDebut().isAfter(today)) {
            location.setStatut(StatutLocation.A_VENIR);
        } else if (dto.getDateFin().isBefore(today)) {
            location.setStatut(StatutLocation.TERMINEE);
        } else {
            location.setStatut(StatutLocation.EN_COURS);
        }

        return locationRepository.save(location);
    }

    // =====================================================
    //  SUPPRIMER UNE LOCATION (ADMIN)
    // =====================================================
    @Transactional
    public void deleteLocation(Long id) {

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Location introuvable"));

        // Vérifier que la location est À VENIR
        if (location.getStatut() != StatutLocation.A_VENIR) {
            throw new BusinessException("Seules les locations À VENIR peuvent être supprimées");
        }

        // Supprimer le contrat associé s'il existe
        contratRepository.findByLocationId(id).ifPresent(contratRepository::delete);

        // Libérer le véhicule si nécessaire
        if (location.getVehicule() != null && location.getStatut() == StatutLocation.EN_COURS) {
            location.getVehicule().setDisponible(true);
        }

        // Supprimer la location
        locationRepository.delete(location);
    }

    // =====================================================
    //  CALCULER LE STATUT D'UNE LOCATION
    // =====================================================
    private StatutLocation calculateStatus(Location location, LocalDate today) {
        if (location.getDateDebut().isAfter(today)) {
            return StatutLocation.A_VENIR;
        } else if (location.getDateFin().isBefore(today)) {
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
            ContratLocation contrat = contratRepository
                    .findByLocationId(location.getId())
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
            // Log l'erreur mais ne pas bloquer la mise à jour de la location
            System.err.println("Erreur mise à jour contrat pour location " + location.getId() + ": " + e.getMessage());
        }
    }

    // =====================================================
    //  TROUVER UNE LOCATION PAR ID
    // =====================================================
    public Location findById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Location introuvable"));
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

                // Vérifier si les dates se chevauchent
                boolean chevauchement =
                        (dateDebut.isBefore(location.getDateFin()) || dateDebut.isEqual(location.getDateFin())) &&
                                (dateFin.isAfter(location.getDateDebut()) || dateFin.isEqual(location.getDateDebut()));

                if (chevauchement) {
                    return true; // Conflit trouvé
                }
            }
        }
        return false; // Pas de conflit
    }
}