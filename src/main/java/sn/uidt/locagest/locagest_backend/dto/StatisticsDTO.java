package sn.uidt.locagest.locagest_backend.dto;

import java.util.Map;
import java.util.List;

public class StatisticsDTO {

    public static class SummaryDTO {
        private Long totalVehicles;
        private Double totalRevenue;
        private Double occupationRate;
        private Long activeClients;
        private String period;
        // Getters/Setters
    }

    public static class VehicleStatsDTO {
        private Long id;
        private String marque;
        private String modele;
        private String immatriculation;
        private Integer locationCount;
        private Double revenue;
        private Double occupationRate;
        private String performance;
        // Getters/Setters
    }

    public static class RevenueChartDTO {
        private String period;
        private List<Map<String, Object>> data;
        private Double total;
        // Getters/Setters
    }
}