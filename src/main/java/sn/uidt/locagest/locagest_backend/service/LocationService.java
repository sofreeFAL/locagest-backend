package sn.uidt.locagest.locagest_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.uidt.locagest.locagest_backend.dto.LocationDTO;
import sn.uidt.locagest.locagest_backend.dto.LocationSearchDTO;
import sn.uidt.locagest.locagest_backend.exception.BusinessException;
import sn.uidt.locagest.locagest_backend.model.*;
import sn.uidt.locagest.locagest_backend.repository.ClientRepository;
import sn.uidt.locagest.locagest_backend.repository.LocationRepository;
import sn.uidt.locagest.locagest_backend.repository.PaiementRepository;
import sn.uidt.locagest.locagest_backend.repository.VehiculeRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final ClientRepository clientRepository;
    private final VehiculeRepository vehiculeRepository;
    private final PaiementRepository paiementRepository;

    public LocationService(
            LocationRepository locationRepository,
            ClientRepository clientRepository,
            VehiculeRepository vehiculeRepository,
            PaiementRepository paiementRepository
    ) {
        this.locationRepository = locationRepository;
        this.clientRepository = clientRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.paiementRepository = paiementRepository;
    }

    // =====================================================
    //  CRÉER UNE LOCATION
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

        if (!vehicule.isDisponible()) {
            throw new BusinessException("Véhicule déjà loué");
        }

        long nbJours = ChronoUnit.DAYS.between(
                dto.getDateDebut(),
                dto.getDateFin()
        );

        if (nbJours <= 0) {
            nbJours = 1;
        }

        double montantTotal = nbJours * vehicule.getPrixParJour();

        Location location = new Location();
        location.setClient(client);
        location.setVehicule(vehicule);
        location.setDateDebut(dto.getDateDebut());
        location.setDateFin(dto.getDateFin());
        location.setMontantTotalLocation(montantTotal);
        location.setStatut(StatutLocation.EN_COURS);

        vehicule.setDisponible(false);

        return locationRepository.save(location);
    }

    // =====================================================
    //  LISTER LES LOCATIONS
    // =====================================================
    public List<Location> getAll() {
        return locationRepository.findAll();
    }

    // =====================================================
    //  HISTORIQUE GLOBAL (TERMINÉES)
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
    //  MODIFIER LE PRIX (ADMIN – AVANT DÉMARRAGE)
    // =====================================================
    @Transactional
    public Location modifierPrixLocation(Long id, Double nouveauMontant) {

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Location introuvable"));

        if (!location.getDateDebut().isAfter(LocalDate.now())) {
            throw new BusinessException(
                    "Impossible de modifier le prix : la location a déjà commencé"
            );
        }

        if (nouveauMontant == null || nouveauMontant <= 0) {
            throw new BusinessException("Montant invalide");
        }

        location.setMontantTotalLocation(nouveauMontant);
        return locationRepository.save(location);
    }

    // =====================================================
    //  PROLONGER UNE LOCATION (ADMIN)
    // =====================================================
    @Transactional
    public Location prolongerLocation(Long id, LocalDate nouvelleDateFin) {

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Location introuvable"));

        if (location.getStatut() == StatutLocation.TERMINEE) {
            throw new BusinessException("Location déjà terminée");
        }

        if (nouvelleDateFin.isBefore(location.getDateFin())) {
            throw new BusinessException("Nouvelle date invalide");
        }

        long joursAjoutes = ChronoUnit.DAYS.between(
                location.getDateFin(),
                nouvelleDateFin
        );

        if (joursAjoutes <= 0) {
            throw new BusinessException("Aucun jour supplémentaire");
        }

        double supplement = joursAjoutes * location.getVehicule().getPrixParJour();

        location.setDateFin(nouvelleDateFin);
        location.setMontantTotalLocation(
                location.getMontantTotalLocation() + supplement
        );

        return locationRepository.save(location);
    }

    // =====================================================
    // RETOUR DU VÉHICULE (PAIEMENT OBLIGATOIRE)
    // =====================================================
    @Transactional
    public Location retourVehicule(Long id) {

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Location introuvable"));

        if (location.getStatut() == StatutLocation.TERMINEE) {
            throw new BusinessException("Location déjà terminée");
        }

        //  RÈGLE MÉTIER : PAIEMENT OBLIGATOIRE
        boolean paiementEffectue = paiementRepository
                .existsByLocationIdAndStatut(id, StatutPaiement.PAYE);

        if (!paiementEffectue) {
            throw new BusinessException(
                    "Retour refusé : la location n’a pas encore été payée"
            );
        }

        Vehicule vehicule = location.getVehicule();
        vehicule.setDisponible(true);

        location.setStatut(StatutLocation.TERMINEE);

        return locationRepository.save(location);
    }
}
