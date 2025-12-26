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

    public VehiculeService(VehiculeRepository vehiculeRepository) {
        this.vehiculeRepository = vehiculeRepository;
    }

    // =========================
    // CRÉER UN VÉHICULE (ADMIN)
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

        //  PRIX OBLIGATOIRE
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
        vehicule.setDisponible(true);

        return toDTO(vehiculeRepository.save(vehicule));
    }

    // =========================
    //  LISTER TOUS (USER + ADMIN)
    // =========================
    public List<VehiculeDTO> getAll() {
        return vehiculeRepository.findAllByOrderByIdAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    //  VÉHICULES DISPONIBLES
    // =========================
    public List<VehiculeDTO> getDisponibles() {
        return vehiculeRepository.findByDisponibleTrueOrderByIdAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    //  TROUVER PAR ID
    // =========================
    public VehiculeDTO getById(Long id) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Véhicule introuvable"));
        return toDTO(vehicule);
    }

    // =========================
    //  MODIFIER (ADMIN)
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

        // INTERDICTION DE MODIFIER LE PRIX SI LE VÉHICULE EST LOUÉ
        if (!vehicule.isDisponible()
                && !vehicule.getPrixParJour().equals(dto.getPrixParJour())) {
            throw new BusinessException(
                    "Impossible de modifier le prix : le véhicule est actuellement loué"
            );
        }

        //  IMMATRIICULATION UNIQUE
        if (!vehicule.getImmatriculation().equals(dto.getImmatriculation())) {

            if (dto.getImmatriculation() == null || dto.getImmatriculation().isBlank()) {
                throw new BusinessException("L'immatriculation est obligatoire");
            }

            if (vehiculeRepository.existsByImmatriculation(dto.getImmatriculation())) {
                throw new BusinessException("Immatriculation déjà utilisée");
            }

            vehicule.setImmatriculation(dto.getImmatriculation());
        }

        vehicule.setMarque(dto.getMarque());
        vehicule.setModele(dto.getModele());
        vehicule.setPrixParJour(dto.getPrixParJour());

        return toDTO(vehiculeRepository.save(vehicule));
    }

    // =========================
    //  SUPPRIMER (ADMIN)
    // =========================
    @Transactional
    public void delete(Long id) {

        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Véhicule introuvable"));

        //  INTERDICTION DE SUPPRIMER UN VÉHICULE LOUÉ
        if (!vehicule.isDisponible()) {
            throw new BusinessException(
                    "Impossible de supprimer un véhicule déjà loué"
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
    // UTILITAIRE
    // =========================
    private String normalize(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    // =========================
    // ENTITY → DTO
    // =========================
    private VehiculeDTO toDTO(Vehicule vehicule) {
        VehiculeDTO dto = new VehiculeDTO();
        dto.setId(vehicule.getId());
        dto.setMarque(vehicule.getMarque());
        dto.setModele(vehicule.getModele());
        dto.setImmatriculation(vehicule.getImmatriculation());
        dto.setPrixParJour(vehicule.getPrixParJour());
        dto.setDisponible(vehicule.isDisponible());
        return dto;
    }
}
