package sn.uidt.locagest.locagest_backend.mapper;

import sn.uidt.locagest.locagest_backend.dto.LocationDTO;
import sn.uidt.locagest.locagest_backend.model.*;

public class LocationMapper {

    // ENTITY → DTO
    public static LocationDTO toDTO(Location location) {
        LocationDTO dto = new LocationDTO();

        dto.setId(location.getId());
        dto.setDateDebut(location.getDateDebut());
        dto.setDateFin(location.getDateFin());
        dto.setStatut(location.getStatut().name());

        if (location.getClient() != null) {
            dto.setClientId(location.getClient().getId());
        }

        if (location.getVehicule() != null) {
            dto.setVehiculeId(location.getVehicule().getId());
        }

        return dto;
    }

    // DTO → ENTITY
    public static Location toEntity(LocationDTO dto) {
        Location location = new Location();

        location.setDateDebut(dto.getDateDebut());
        location.setDateFin(dto.getDateFin());
        location.setStatut(StatutLocation.valueOf(dto.getStatut()));

        Client client = new Client();
        client.setId(dto.getClientId());
        location.setClient(client);

        Vehicule vehicule = new Vehicule();
        vehicule.setId(dto.getVehiculeId());
        location.setVehicule(vehicule);

        return location;
    }
}
