package sn.uidt.locagest.locagest_backend.controller;

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

    // CREATE CLIENT
    @PostMapping
    public Client create(@RequestBody Client client) {
        return clientService.create(client);
    }

    // GET ALL CLIENTS
    @GetMapping
    public List<Client> getAll() {
        return clientService.getAll();
    }
}
