package sn.uidt.locagest.locagest_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.uidt.locagest.locagest_backend.dto.VehiculeDTO;
import sn.uidt.locagest.locagest_backend.exception.BusinessException;
import sn.uidt.locagest.locagest_backend.model.Vehicule;
import sn.uidt.locagest.locagest_backend.repository.VehiculeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final LocationService locationService;

    public VehiculeService(VehiculeRepository vehiculeRepository, LocationService locationService) {
        this.vehiculeRepository = vehiculeRepository;
        this.locationService = locationService;
    }

    // =========================
    // CRÉER UN VÉHICULE (ADMIN) - CORRIGÉ
    // =========================
    @Transactional
    public VehiculeDTO create(VehiculeDTO dto) {
        if (dto.getMarque() == null || dto.getMarque().isBlank()) {
            throw new BusinessException("La marque est obligatoire");
        }

        if (dto.getModele() == null || dto.getModele().isBlank()) {
            throw new BusinessException("Le modèle est obligatoire");
        }

        if (dto.getImmatriculation() == null || dto.getImmatriculation().isBlank()) {
            throw new BusinessException("L'immatriculation est obligatoire");
        }

        if (dto.getPrixParJour() == null || dto.getPrixParJour() <= 0) {
            throw new BusinessException(
                    "Le prix de location par jour est obligatoire et doit être supérieur à 0"
            );
        }

        if (vehiculeRepository.existsByImmatriculation(dto.getImmatriculation())) {
            throw new BusinessException("Véhicule déjà existant (immatriculation)");
        }

        Vehicule vehicule = new Vehicule();
        vehicule.setMarque(dto.getMarque());
        vehicule.setModele(dto.getModele());
        vehicule.setImmatriculation(dto.getImmatriculation());
        vehicule.setPrixParJour(dto.getPrixParJour());

        // GÉRER LE STATUT
        if (dto.getStatut() != null && !dto.getStatut().isBlank()) {
            String statut = dto.getStatut().toUpperCase().trim();

            // Valider les statuts autorisés
            if (statut.equals("DISPONIBLE") || statut.equals("LOUE") || statut.equals("EN_MAINTENANCE")) {
                vehicule.setStatut(statut);
            } else {
                throw new BusinessException("Statut invalide. Valeurs autorisées: DISPONIBLE, LOUE, EN_MAINTENANCE");
            }
        } else {
            // Par défaut
            vehicule.setStatut("DISPONIBLE");
        }

        return toDTO(vehiculeRepository.save(vehicule));
    }

    // =========================
    //  LISTER TOUS (USER + ADMIN) - AVEC SYNC DES STATUTS
    // =========================
    public List<VehiculeDTO> getAll() {
        // D'abord s'assurer que les statuts des locations sont à jour
        locationService.updateLocationsStatus();

        return vehiculeRepository.findAllByOrderByIdAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    //  VÉHICULES DISPONIBLES
    // =========================
    public List<VehiculeDTO> getDisponibles() {
        // D'abord s'assurer que les statuts des locations sont à jour
        locationService.updateLocationsStatus();

        return vehiculeRepository.findByDisponibleTrueOrderByIdAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    //  TROUVER PAR ID
    // =========================
    public VehiculeDTO getById(Long id) {
        // S'assurer que les statuts sont à jour
        locationService.updateLocationsStatus();

        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Véhicule introuvable"));
        return toDTO(vehicule);
    }

    // =========================
    //  MODIFIER (ADMIN) - CORRIGÉ
    // =========================
    @Transactional
    public VehiculeDTO update(Long id, VehiculeDTO dto) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Véhicule introuvable"));

        if (dto.getMarque() == null || dto.getMarque().isBlank()) {
            throw new BusinessException("La marque est obligatoire");
        }

        if (dto.getModele() == null || dto.getModele().isBlank()) {
            throw new BusinessException("Le modèle est obligatoire");
        }

        if (dto.getPrixParJour() == null || dto.getPrixParJour() <= 0) {
            throw new BusinessException("Le prix par jour doit être supérieur à 0");
        }

        // Vérifier si le véhicule est loué
        if ("LOUE".equals(vehicule.getStatut())) {
            // Si le véhicule est loué, interdire certaines modifications
            if (!vehicule.getPrixParJour().equals(dto.getPrixParJour())) {
                throw new BusinessException(
                        "Impossible de modifier le prix : le véhicule est actuellement loué"
                );
            }

            // Si on essaye de changer le statut autre qu'en LOUE
            if (dto.getStatut() != null && !dto.getStatut().isBlank() &&
                    !"LOUE".equals(dto.getStatut().toUpperCase())) {
                throw new BusinessException(
                        "Impossible de changer le statut : le véhicule est actuellement loué"
                );
            }
        }

        // Vérifier l'immatriculation unique
        if (!vehicule.getImmatriculation().equals(dto.getImmatriculation())) {
            if (dto.getImmatriculation() == null || dto.getImmatriculation().isBlank()) {
                throw new BusinessException("L'immatriculation est obligatoire");
            }

            if (vehiculeRepository.existsByImmatriculation(dto.getImmatriculation())) {
                throw new BusinessException("Immatriculation déjà utilisée");
            }

            vehicule.setImmatriculation(dto.getImmatriculation());
        }

        // METTRE À JOUR LE STATUT
        if (dto.getStatut() != null && !dto.getStatut().isBlank()) {
            String statut = dto.getStatut().toUpperCase().trim();

            if (!statut.equals("DISPONIBLE") && !statut.equals("LOUE") && !statut.equals("EN_MAINTENANCE")) {
                throw new BusinessException("Statut invalide. Valeurs autorisées: DISPONIBLE, LOUE, EN_MAINTENANCE");
            }

            // Vérifier les transitions de statut
            if ("LOUE".equals(vehicule.getStatut()) && !"LOUE".equals(statut)) {
                throw new BusinessException("Un véhicule loué ne peut pas changer de statut tant qu'il n'est pas retourné");
            }

            vehicule.setStatut(statut);
        }

        vehicule.setMarque(dto.getMarque());
        vehicule.setModele(dto.getModele());
        vehicule.setPrixParJour(dto.getPrixParJour());

        return toDTO(vehiculeRepository.save(vehicule));
    }

    // =========================
    //  SUPPRIMER (ADMIN) - CORRIGÉ
    // =========================
    @Transactional
    public void delete(Long id) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Véhicule introuvable"));

        // Vérifier si le véhicule est disponible
        if (!vehicule.isDisponible()) {
            throw new BusinessException(
                    "Impossible de supprimer un véhicule qui n'est pas disponible (statut: " + vehicule.getStatut() + ")"
            );
        }

        vehiculeRepository.delete(vehicule);
    }

    // =========================
    //  RECHERCHE (CORRIGÉE)
    // =========================
    public List<VehiculeDTO> search(
            String marque,
            String modele,
            String immatriculation,
            Boolean disponible
    ) {
        // S'assurer que les statuts des locations sont à jour
        locationService.updateLocationsStatus();

        return vehiculeRepository.search(
                        normalize(marque),
                        normalize(modele),
                        normalize(immatriculation),
                        disponible
                ).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    //  MÉTHODE POUR METTRE UN VÉHICULE EN MAINTENANCE
    // =========================
    @Transactional
    public VehiculeDTO mettreEnMaintenance(Long id) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Véhicule introuvable"));

        if ("LOUE".equals(vehicule.getStatut())) {
            throw new BusinessException("Impossible de mettre en maintenance un véhicule loué");
        }

        vehicule.setStatut("EN_MAINTENANCE");
        return toDTO(vehiculeRepository.save(vehicule));
    }

    // =========================
    //  MÉTHODE POUR SORTIR DE MAINTENANCE
    // =========================
    @Transactional
    public VehiculeDTO sortirDeMaintenance(Long id) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Véhicule introuvable"));

        if (!"EN_MAINTENANCE".equals(vehicule.getStatut())) {
            throw new BusinessException("Le véhicule n'est pas en maintenance");
        }

        vehicule.setStatut("DISPONIBLE");
        return toDTO(vehiculeRepository.save(vehicule));
    }

    // =========================
    // UTILITAIRE
    // =========================
    private String normalize(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    // =========================
    // ENTITY → DTO - CORRIGÉ
    // =========================
    private VehiculeDTO toDTO(Vehicule vehicule) {
        VehiculeDTO dto = new VehiculeDTO();
        dto.setId(vehicule.getId());
        dto.setMarque(vehicule.getMarque());
        dto.setModele(vehicule.getModele());
        dto.setImmatriculation(vehicule.getImmatriculation());
        dto.setPrixParJour(vehicule.getPrixParJour());
        dto.setDisponible(vehicule.isDisponible());
        dto.setStatut(vehicule.getStatut());
        return dto;
    }
}