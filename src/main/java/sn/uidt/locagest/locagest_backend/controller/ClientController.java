package sn.uidt.locagest.locagest_backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.uidt.locagest.locagest_backend.model.Client;
import sn.uidt.locagest.locagest_backend.service.ClientService;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    // =========================
    // CRÉER UN CLIENT
    // USER + ADMIN
    // =========================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping
    public Client create(@RequestBody Client client) {
        return clientService.create(client);
    }

    // =========================
    //  LISTER TOUS LES CLIENTS
    // USER + ADMIN
    // =========================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public List<Client> getAll() {
        return clientService.getAll();
    }

    // =========================
    //  RECHERCHE MULTI-CRITÈRES (CORRIGÉE)
    // USER + ADMIN
    // =========================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/search")
    public List<Client> search(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) String telephone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String numeroCni
    ) {
        // 🔧 NORMALISATION DES PARAMÈTRES (CLÉ DU BUG)
        nom = (nom == null || nom.isBlank()) ? null : nom;
        prenom = (prenom == null || prenom.isBlank()) ? null : prenom;
        telephone = (telephone == null || telephone.isBlank()) ? null : telephone;
        email = (email == null || email.isBlank()) ? null : email;
        numeroCni = (numeroCni == null || numeroCni.isBlank()) ? null : numeroCni;

        return clientService.search(
                nom,
                prenom,
                telephone,
                email,
                numeroCni
        );
    }

    // =========================
    //  VOIR CLIENT PAR ID
    // USER + ADMIN
    // =========================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{id}")
    public Client getById(@PathVariable Long id) {
        return clientService.getById(id);
    }

    // =========================
    //  MODIFIER CLIENT
    // USER + ADMIN
    // =========================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/{id}")
    public Client update(
            @PathVariable Long id,
            @RequestBody Client client
    ) {
        return clientService.update(id, client);
    }

    // =========================
    //  SUPPRIMER CLIENT
    // ADMIN SEULEMENT
    // =========================
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        clientService.delete(id);
    }
}
