package sn.uidt.locagest.locagest_backend.controller;

import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import sn.uidt.locagest.locagest_backend.dto.*;
import sn.uidt.locagest.locagest_backend.mapper.LocationMapper;
import sn.uidt.locagest.locagest_backend.model.Location;
import sn.uidt.locagest.locagest_backend.service.LocationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    // =========================
    //  CRÉER UNE LOCATION
    // USER + ADMIN
    // =========================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody LocationDTO dto) {

        Location saved = locationService.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "message", "Location enregistrée avec succès",
                        "location", LocationMapper.toDTO(saved)
                )
        );
    }

    // =========================
    //  LISTER LES LOCATIONS
    // (EN COURS + FUTURES)
    // USER + ADMIN
    // =========================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public List<LocationDTO> getAll() {
        return locationService.getAll()
                .stream()
                .map(LocationMapper::toDTO)
                .toList();
    }

    // =========================
    //  HISTORIQUE GLOBAL
    // LOCATIONS TERMINÉES
    // USER + ADMIN
    // =========================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/historique")
    public List<LocationDTO> historique() {
        return locationService.getHistorique()
                .stream()
                .map(LocationMapper::toDTO)
                .toList();
    }

    // =========================
    //  HISTORIQUE PAR CLIENT
    // =========================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/historique/client/{clientId}")
    public List<LocationDTO> historiqueParClient(@PathVariable Long clientId) {
        return locationService.getHistoriqueParClient(clientId)
                .stream()
                .map(LocationMapper::toDTO)
                .toList();
    }

    // =========================
    //  HISTORIQUE PAR VÉHICULE
    // =========================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/historique/vehicule/{vehiculeId}")
    public List<LocationDTO> historiqueParVehicule(@PathVariable Long vehiculeId) {
        return locationService.getHistoriqueParVehicule(vehiculeId)
                .stream()
                .map(LocationMapper::toDTO)
                .toList();
    }

    // =========================
    // RECHERCHE AVANCÉE
    // USER + ADMIN
    // =========================
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<LocationDTO> search(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long vehiculeId,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) LocalDate dateDebutMin,
            @RequestParam(required = false) LocalDate dateDebutMax,
            @RequestParam(required = false) LocalDate dateFinMin,
            @RequestParam(required = false) LocalDate dateFinMax,
            @RequestParam(required = false) Double montantMin,
            @RequestParam(required = false) Double montantMax
    ) {
        LocationSearchDTO dto = new LocationSearchDTO();
        dto.setClientId(clientId);
        dto.setVehiculeId(vehiculeId);
        dto.setStatut(statut);
        dto.setDateDebutMin(dateDebutMin);
        dto.setDateDebutMax(dateDebutMax);
        dto.setDateFinMin(dateFinMin);
        dto.setDateFinMax(dateFinMax);
        dto.setMontantMin(montantMin);
        dto.setMontantMax(montantMax);

        return locationService.searchAdvanced(dto)
                .stream()
                .map(LocationMapper::toDTO)
                .toList();
    }

    // =========================
    //  MODIFIER LE PRIX
    // ADMIN – AVANT DÉMARRAGE
    // =========================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/prix")
    public LocationDTO modifierPrix(
            @PathVariable Long id,
            @RequestBody ModifierPrixLocationDTO dto
    ) {
        return LocationMapper.toDTO(
                locationService.modifierPrixLocation(id, dto.getNouveauMontant())
        );
    }

    // =========================
    // PROLONGER
    // ADMIN
    // =========================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/prolonger")
    public LocationDTO prolonger(
            @PathVariable Long id,
            @RequestBody ProlongationLocationDTO dto
    ) {
        return LocationMapper.toDTO(
                locationService.prolongerLocation(id, dto.getNouvelleDateFin())
        );
    }

    // =========================
    //  RETOUR DU VÉHICULE
    // ADMIN
    // =========================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/retour")
    public LocationDTO retourVehicule(@PathVariable Long id) {
        return LocationMapper.toDTO(locationService.retourVehicule(id));
    }
}
