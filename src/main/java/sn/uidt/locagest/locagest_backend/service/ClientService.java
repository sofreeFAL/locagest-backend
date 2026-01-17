package sn.uidt.locagest.locagest_backend.service;

import org.springframework.stereotype.Service;
import sn.uidt.locagest.locagest_backend.exception.BusinessException;
import sn.uidt.locagest.locagest_backend.model.Client;
import sn.uidt.locagest.locagest_backend.repository.ClientRepository;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    // =====================================================
    // 🛠 UTILITAIRE : NORMALISER LES CHAMPS DE RECHERCHE
    // =====================================================
    private String normalize(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }

    // =====================================================
    //  CRÉER UN CLIENT
    // =====================================================
    public Client create(Client client) {

        if (client.getNom() == null || client.getNom().isBlank()) {
            throw new BusinessException("Nom obligatoire");
        }
        if (client.getPrenom() == null || client.getPrenom().isBlank()) {
            throw new BusinessException("Prénom obligatoire");
        }
        if (client.getTelephone() == null || client.getTelephone().isBlank()) {
            throw new BusinessException("Téléphone obligatoire");
        }
        if (client.getEmail() == null || client.getEmail().isBlank()) {
            throw new BusinessException("Email obligatoire");
        }
        if (client.getNumeroCni() == null || client.getNumeroCni().isBlank()) {
            throw new BusinessException("Numéro de CNI obligatoire");
        }

        // ADRESSE : optionnelle - pas de validation nécessaire
        // Si adresse est vide, on la met à null pour la base de données
        if (client.getAdresse() != null && client.getAdresse().isBlank()) {
            client.setAdresse(null);
        }

        if (clientRepository.existsByEmail(client.getEmail())) {
            throw new BusinessException("Email déjà utilisé");
        }
        if (clientRepository.existsByTelephone(client.getTelephone())) {
            throw new BusinessException("Numéro de téléphone déjà utilisé");
        }
        if (clientRepository.existsByNumeroCni(client.getNumeroCni())) {
            throw new BusinessException("Numéro de CNI déjà utilisé");
        }

        return clientRepository.save(client);
    }

    // =====================================================
    //  LISTER TOUS LES CLIENTS
    // =====================================================
    public List<Client> getAll() {
        return clientRepository.findAll();
    }

    // =====================================================
    //  CLIENT PAR ID
    // =====================================================
    public Client getById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Client introuvable"));
    }

    // =====================================================
    //  MODIFIER CLIENT (USER + ADMIN)
    // =====================================================
    public Client update(Long id, Client data) {

        Client client = getById(id);

        if (data.getNom() == null || data.getNom().isBlank()) {
            throw new BusinessException("Nom obligatoire");
        }
        if (data.getPrenom() == null || data.getPrenom().isBlank()) {
            throw new BusinessException("Prénom obligatoire");
        }
        if (data.getTelephone() == null || data.getTelephone().isBlank()) {
            throw new BusinessException("Téléphone obligatoire");
        }
        if (data.getEmail() == null || data.getEmail().isBlank()) {
            throw new BusinessException("Email obligatoire");
        }
        if (data.getNumeroCni() == null || data.getNumeroCni().isBlank()) {
            throw new BusinessException("Numéro de CNI obligatoire");
        }

        // Gérer l'adresse - optionnelle
        if (data.getAdresse() != null && data.getAdresse().isBlank()) {
            data.setAdresse(null);
        }

        if (!client.getEmail().equals(data.getEmail())
                && clientRepository.existsByEmail(data.getEmail())) {
            throw new BusinessException("Email déjà utilisé");
        }

        if (!client.getTelephone().equals(data.getTelephone())
                && clientRepository.existsByTelephone(data.getTelephone())) {
            throw new BusinessException("Numéro de téléphone déjà utilisé");
        }

        if (!client.getNumeroCni().equals(data.getNumeroCni())
                && clientRepository.existsByNumeroCni(data.getNumeroCni())) {
            throw new BusinessException("Numéro de CNI déjà utilisé");
        }

        // Mettre à jour tous les champs
        client.setNom(data.getNom());
        client.setPrenom(data.getPrenom());
        client.setTelephone(data.getTelephone());
        client.setEmail(data.getEmail());
        client.setNumeroCni(data.getNumeroCni());
        client.setAdresse(data.getAdresse()); // ← CORRECTION IMPORTANTE ICI

        return clientRepository.save(client);
    }

    // =====================================================
    //  RECHERCHE AVANCÉE (CORRIGÉE)
    // =====================================================
    public List<Client> search(
            String nom,
            String prenom,
            String telephone,
            String email,
            String numeroCni
    ) {
        return clientRepository.search(
                normalize(nom),
                normalize(prenom),
                normalize(telephone),
                normalize(email),
                normalize(numeroCni)
        );
    }

    // =====================================================
    // 🗑 SUPPRIMER CLIENT (ADMIN)
    // =====================================================
    public void delete(Long id) {

        Client client = getById(id);

        try {
            clientRepository.delete(client);
        } catch (Exception e) {
            throw new BusinessException(
                    "Impossible de supprimer ce client : il est lié à une location"
            );
        }
    }
}