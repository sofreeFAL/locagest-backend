package sn.uidt.locagest.locagest_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import sn.uidt.locagest.locagest_backend.dto.VehiculeDTO;
import sn.uidt.locagest.locagest_backend.exception.BusinessException;
import sn.uidt.locagest.locagest_backend.model.Location;
import sn.uidt.locagest.locagest_backend.model.StatutLocation;
import sn.uidt.locagest.locagest_backend.model.Vehicule;
import sn.uidt.locagest.locagest_backend.repository.LocationRepository;
import sn.uidt.locagest.locagest_backend.repository.VehiculeRepository;
import sn.uidt.locagest.locagest_backend.service.VehiculeService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/vehicules")
public class VehiculeController {

    private final VehiculeService vehiculeService;
    private final VehiculeRepository vehiculeRepository;
    private final LocationRepository locationRepository;

    public VehiculeController(
            VehiculeService vehiculeService,
            VehiculeRepository vehiculeRepository,
            LocationRepository locationRepository
    ) {
        this.vehiculeService = vehiculeService;
        this.vehiculeRepository = vehiculeRepository;
        this.locationRepository = locationRepository;
    }

    // =========================
    // CRÉER UN VÉHICULE
    // ADMIN SEULEMENT
    // =========================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public VehiculeDTO create(@RequestBody VehiculeDTO dto) {
        return vehiculeService.create(dto);
    }

    // =========================
    //  LISTER TOUS
    // USER + ADMIN
    // =========================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public List<VehiculeDTO> getAll() {
        return vehiculeService.getAll();
    }

    // =========================
    //  VÉHICULES DISPONIBLES
    // USER + ADMIN
    // =========================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/disponibles")
    public List<VehiculeDTO> getDisponibles() {
        return vehiculeService.getDisponibles();
    }

    // =========================
    //  MODIFIER (PRIX, INFOS)
    // ADMIN SEULEMENT
    // =========================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public VehiculeDTO update(
            @PathVariable Long id,
            @RequestBody VehiculeDTO dto
    ) {
        return vehiculeService.update(id, dto);
    }

    // =====================================================
    //  CORRIGER LES STATUTS DES VÉHICULES
    // =====================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/corriger-statut")
    public ResponseEntity<?> corrigerStatut(@PathVariable Long id) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Véhicule introuvable"));

        // Vérifier s'il y a des locations en cours pour ce véhicule
        List<Location> locationsEnCours = locationRepository.findAll().stream()
                .filter(l -> l.getVehicule().getId().equals(id))
                .filter(l -> l.getStatut() == StatutLocation.EN_COURS)
                .collect(Collectors.toList());

        if (locationsEnCours.isEmpty()) {
            // Pas de location en cours → le véhicule doit être disponible
            if (!"DISPONIBLE".equals(vehicule.getStatut()) && !"EN_MAINTENANCE".equals(vehicule.getStatut())) {
                String ancienStatut = vehicule.getStatut();
                vehicule.setStatut("DISPONIBLE");
                vehiculeRepository.save(vehicule);
                return ResponseEntity.ok(Map.of(
                        "message", "Statut corrigé : véhicule marqué comme DISPONIBLE",
                        "ancienStatut", ancienStatut,
                        "nouveauStatut", "DISPONIBLE"
                ));
            }
        } else {
            // Il y a une location en cours → le véhicule doit être loué
            String ancienStatut = vehicule.getStatut();
            vehicule.setStatut("LOUE");
            vehiculeRepository.save(vehicule);
            return ResponseEntity.ok(Map.of(
                    "message", "Statut corrigé : véhicule marqué comme LOUE (location en cours trouvée)",
                    "ancienStatut", ancienStatut,
                    "nouveauStatut", "LOUE",
                    "locationsEnCours", locationsEnCours.size()
            ));
        }

        return ResponseEntity.ok(Map.of("message", "Statut déjà correct", "statut", vehicule.getStatut()));
    }

    // =====================================================
    //  METTRE EN MAINTENANCE
    // =====================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/maintenance")
    public ResponseEntity<?> mettreEnMaintenance(@PathVariable Long id) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Véhicule introuvable"));

        // Vérifier si le véhicule est loué
        if ("LOUE".equals(vehicule.getStatut())) {
            throw new BusinessException("Impossible de mettre en maintenance un véhicule loué");
        }

        String ancienStatut = vehicule.getStatut();
        vehicule.setStatut("EN_MAINTENANCE");
        vehiculeRepository.save(vehicule);

        return ResponseEntity.ok(Map.of(
                "message", "Véhicule mis en maintenance",
                "ancienStatut", ancienStatut,
                "nouveauStatut", "EN_MAINTENANCE"
        ));
    }

    // =====================================================
    //  SORTIR DE MAINTENANCE
    // =====================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/sortir-maintenance")
    public ResponseEntity<?> sortirDeMaintenance(@PathVariable Long id) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Véhicule introuvable"));

        if (!"EN_MAINTENANCE".equals(vehicule.getStatut())) {
            throw new BusinessException("Le véhicule n'est pas en maintenance");
        }

        String ancienStatut = vehicule.getStatut();
        vehicule.setStatut("DISPONIBLE");
        vehiculeRepository.save(vehicule);

        return ResponseEntity.ok(Map.of(
                "message", "Véhicule sorti de maintenance",
                "ancienStatut", ancienStatut,
                "nouveauStatut", "DISPONIBLE"
        ));
    }

    // =====================================================
    //  RÉPARATION GLOBALE DES STATUTS
    // =====================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reparer-statuts")
    public ResponseEntity<?> reparerTousLesStatuts() {
        List<Vehicule> vehicules = vehiculeRepository.findAll();
        int corrections = 0;
        int enMaintenance = 0;

        for (Vehicule vehicule : vehicules) {
            // Ne pas modifier les véhicules en maintenance
            if ("EN_MAINTENANCE".equals(vehicule.getStatut())) {
                enMaintenance++;
                continue;
            }

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
                // Véhicule devrait être DISPONIBLE
                if (!"DISPONIBLE".equals(vehicule.getStatut())) {
                    vehicule.setStatut("DISPONIBLE");
                    vehiculeRepository.save(vehicule);
                    corrections++;
                }
            }
        }

        return ResponseEntity.ok(Map.of(
                "message", "Réparation des statuts terminée",
                "totalVehicules", vehicules.size(),
                "correctionsEffectuees", corrections,
                "enMaintenanceNonModifies", enMaintenance,
                "details", corrections + " véhicules corrigés sur " + vehicules.size()
        ));
    }

    // =========================
    //  SUPPRIMER
    // ADMIN SEULEMENT
    // =========================
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        vehiculeService.delete(id);
    }

    // =========================
    // RECHERCHE
    // USER + ADMIN
    // =========================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/search")
    public List<VehiculeDTO> search(
            @RequestParam(required = false) String marque,
            @RequestParam(required = false) String modele,
            @RequestParam(required = false) String immatriculation,
            @RequestParam(required = false) Boolean disponible
    ) {
        return vehiculeService.search(
                marque,
                modele,
                immatriculation,
                disponible
        );
    }

    // =========================
    //  VOIR PAR ID
    // USER + ADMIN
    // =========================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{id}")
    public VehiculeDTO getById(@PathVariable Long id) {
        return vehiculeService.getById(id);
    }
}