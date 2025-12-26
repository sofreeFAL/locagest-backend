package sn.uidt.locagest.locagest_backend.mapper;

import sn.uidt.locagest.locagest_backend.dto.LocationDTO;
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
        }

        // Véhicule
        if (location.getVehicule() != null) {
            dto.setVehiculeId(location.getVehicule().getId());
        }

        // Montant figé de la location
        dto.setMontantTotalLocation(location.getMontantTotalLocation());

        // Statut
        if (location.getStatut() != null) {
            dto.setStatut(location.getStatut().name());
        }

        return dto;
    }
}
