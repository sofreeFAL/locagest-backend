package sn.uidt.locagest.locagest_backend.service;

import org.springframework.stereotype.Service;
import sn.uidt.locagest.locagest_backend.model.Client;
import sn.uidt.locagest.locagest_backend.repository.ClientRepository;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client create(Client client) {
        return clientRepository.save(client);
    }

    public List<Client> getAll() {
        return clientRepository.findAll();
    }
}
