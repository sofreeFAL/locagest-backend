package sn.uidt.locagest.locagest_backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import sn.uidt.locagest.locagest_backend.dto.VehiculeDTO;
import sn.uidt.locagest.locagest_backend.service.VehiculeService;

import java.util.List;

@RestController
@RequestMapping("/vehicules")
public class VehiculeController {

    private final VehiculeService vehiculeService;

    public VehiculeController(VehiculeService vehiculeService) {
        this.vehiculeService = vehiculeService;
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
