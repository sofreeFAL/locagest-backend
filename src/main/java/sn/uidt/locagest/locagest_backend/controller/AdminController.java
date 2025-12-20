package sn.uidt.locagest.locagest_backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.uidt.locagest.locagest_backend.model.*;
import sn.uidt.locagest.locagest_backend.repository.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final VehiculeRepository vehiculeRepository;
    private final ClientRepository clientRepository;
    private final LocationRepository locationRepository;
    private final PaiementRepository paiementRepository;

    public AdminController(UserRepository userRepository,
                           VehiculeRepository vehiculeRepository,
                           ClientRepository clientRepository,
                           LocationRepository locationRepository,
                           PaiementRepository paiementRepository) {
        this.userRepository = userRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.clientRepository = clientRepository;
        this.locationRepository = locationRepository;
        this.paiementRepository = paiementRepository;
    }

    @GetMapping("/users")
    public List<User> users() {
        return userRepository.findAll();
    }

    @GetMapping("/vehicules")
    public List<Vehicule> vehicules() {
        return vehiculeRepository.findAll();
    }

    @GetMapping("/clients")
    public List<Client> clients() {
        return clientRepository.findAll();
    }

    @GetMapping("/locations")
    public List<Location> locations() {
        return locationRepository.findAll();
    }

    @GetMapping("/paiements")
    public List<Paiement> paiements() {
        return paiementRepository.findAll();
    }
}
