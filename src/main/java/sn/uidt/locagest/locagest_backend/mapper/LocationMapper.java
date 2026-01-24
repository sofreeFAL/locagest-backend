package sn.uidt.locagest.locagest_backend.mapper;

import sn.uidt.locagest.locagest_backend.dto.ContratLocationDTO;
import sn.uidt.locagest.locagest_backend.dto.LocationDTO;
import sn.uidt.locagest.locagest_backend.model.ContratLocation;
import sn.uidt.locagest.locagest_backend.model.Location;

public class LocationMapper {

    // =========================
    // ENTITY → DTO (LECTURE)
    // =========================
    public static LocationDTO toDTO(Location location) {
        if (location == null) {
            return null;
        }

        LocationDTO dto = new LocationDTO();

        // Identifiant
        dto.setId(location.getId());

        // Dates
        dto.setDateDebut(location.getDateDebut());
        dto.setDateFin(location.getDateFin());

        // Client
        if (location.getClient() != null) {
            dto.setClientId(location.getClient().getId());
            dto.setClientNom(location.getClient().getNom());
            dto.setClientPrenom(location.getClient().getPrenom());
            dto.setClientTelephone(location.getClient().getTelephone());
        }

        // Véhicule
        if (location.getVehicule() != null) {
            dto.setVehiculeId(location.getVehicule().getId());
            dto.setVehiculeMarque(location.getVehicule().getMarque());
            dto.setVehiculeModele(location.getVehicule().getModele());
            dto.setVehiculeImmatriculation(location.getVehicule().getImmatriculation());
            dto.setVehiculePrixParJour(location.getVehicule().getPrixParJour());
        }

        // Montant figé de la location
        dto.setMontantTotalLocation(location.getMontantTotalLocation());

        // Statut
        if (location.getStatut() != null) {
            dto.setStatut(location.getStatut().name());
        }

        return dto;
    }

    // =========================
    // CONTRAT → DTO
    // =========================
    public static ContratLocationDTO contratToDTO(ContratLocation contrat) {
        if (contrat == null) {
            return null;
        }

        ContratLocationDTO dto = new ContratLocationDTO();
        dto.setId(contrat.getId());
        dto.setNumeroContrat(contrat.getNumeroContrat());
        dto.setDateCreation(contrat.getDateCreation());
        dto.setStatut(contrat.getStatut() != null ? contrat.getStatut().name() : null);

        // Informations de la location
        if (contrat.getLocation() != null) {
            Location location = contrat.getLocation();
            dto.setLocationId(location.getId());
            dto.setMontantTotalLocation(location.getMontantTotalLocation());
            dto.setDateDebut(location.getDateDebut());
            dto.setDateFin(location.getDateFin());

            if (location.getClient() != null) {
                dto.setClientNom(location.getClient().getNom());
                dto.setClientPrenom(location.getClient().getPrenom());
            }

            if (location.getVehicule() != null) {
                dto.setVehiculeMarque(location.getVehicule().getMarque());
                dto.setVehiculeModele(location.getVehicule().getModele());
                dto.setVehiculeImmatriculation(location.getVehicule().getImmatriculation());
            }
        }

        return dto;
    }
}