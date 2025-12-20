package sn.uidt.locagest.locagest_backend.controller;

import org.springframework.web.bind.annotation.*;
import sn.uidt.locagest.locagest_backend.model.Vehicule;
import sn.uidt.locagest.locagest_backend.service.VehiculeService;

import java.util.List;

@RestController
@RequestMapping("/vehicules")
public class VehiculeController {

    private final VehiculeService vehiculeService;

    public VehiculeController(VehiculeService vehiculeService) {
        this.vehiculeService = vehiculeService;
    }

    @PostMapping
    public Vehicule create(@RequestBody Vehicule vehicule) {
        return vehiculeService.create(vehicule);
    }

    @GetMapping
    public List<Vehicule> getAll() {
        return vehiculeService.getAll();
    }

    @GetMapping("/disponibles")
    public List<Vehicule> getDisponibles() {
        return vehiculeService.getDisponibles();
    }

    @PutMapping("/{id}/disponible")
    public Vehicule changerDisponibilite(
            @PathVariable Long id,
            @RequestParam boolean disponible
    ) {
        return vehiculeService.changerDisponibilite(id, disponible);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        vehiculeService.delete(id);
    }
}
