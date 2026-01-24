package sn.uidt.locagest.locagest_backend.service;

import org.springframework.stereotype.Service;
import sn.uidt.locagest.locagest_backend.model.*;
import sn.uidt.locagest.locagest_backend.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final VehiculeRepository vehiculeRepository;
    private final LocationRepository locationRepository;
    private final ClientRepository clientRepository;
    private final ContratLocationRepository contratRepository;
    private final PaiementRepository paiementRepository;

    public StatisticsService(
            VehiculeRepository vehiculeRepository,
            LocationRepository locationRepository,
            ClientRepository clientRepository,
            ContratLocationRepository contratRepository,
            PaiementRepository paiementRepository
    ) {
        this.vehiculeRepository = vehiculeRepository;
        this.locationRepository = locationRepository;
        this.clientRepository = clientRepository;
        this.contratRepository = contratRepository;
        this.paiementRepository = paiementRepository;
    }

    // =====================================================
    //  RÉSUMÉ DES STATISTIQUES
    // =====================================================
    public Map<String, Object> getSummary(String period, LocalDate startDate, LocalDate endDate) {
        // Déterminer la période
        DateRange dateRange = getDateRange(period, startDate, endDate);

        Map<String, Object> summary = new HashMap<>();

        // 1. Total véhicules
        long totalVehicles = vehiculeRepository.count();
        summary.put("totalVehicles", totalVehicles);

        // 2. Revenus totaux
        double totalRevenue = calculateTotalRevenue(dateRange.startDate, dateRange.endDate);
        summary.put("totalRevenue", totalRevenue);

        // 3. Taux d'occupation
        double occupationRate = calculateOccupationRate(dateRange.startDate, dateRange.endDate);
        summary.put("occupationRate", Math.round(occupationRate * 100.0) / 100.0);

        // 4. Clients actifs
        long activeClients = calculateActiveClients(dateRange.startDate, dateRange.endDate);
        summary.put("activeClients", activeClients);

        // Informations de période
        summary.put("period", period != null ? period : "custom");
        summary.put("startDate", dateRange.startDate);
        summary.put("endDate", dateRange.endDate);

        return summary;
    }

    // =====================================================
    //  STATISTIQUES PAR VÉHICULE
    // =====================================================
    public List<Map<String, Object>> getVehicleStatistics(String period, LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = getDateRange(period, startDate, endDate);
        List<Vehicule> vehicles = vehiculeRepository.findAll();
        List<Location> allLocations = locationRepository.findAll();

        return vehicles.stream().map(vehicle -> {
            // Filtrer les locations pour ce véhicule dans la période
            List<Location> vehicleLocations = allLocations.stream()
                    .filter(location -> location.getVehicule().getId().equals(vehicle.getId()))
                    .filter(location -> isDateInRange(location.getDateDebut(), dateRange.startDate, dateRange.endDate))
                    .collect(Collectors.toList());

            // Nombre de locations
            int locationCount = vehicleLocations.size();

            // Revenu généré
            double revenue = vehicleLocations.stream()
                    .mapToDouble(Location::getMontantTotalLocation)
                    .sum();

            // Taux d'occupation (jours loués / jours totaux dans la période)
            long totalDaysInPeriod = java.time.temporal.ChronoUnit.DAYS.between(
                    dateRange.startDate, dateRange.endDate.plusDays(1)
            );

            long rentedDays = vehicleLocations.stream()
                    .mapToLong(location -> java.time.temporal.ChronoUnit.DAYS.between(
                            location.getDateDebut(), location.getDateFin().plusDays(1)
                    ))
                    .sum();

            double occupationRate = totalDaysInPeriod > 0 ?
                    (double) rentedDays / totalDaysInPeriod * 100 : 0;

            // Déterminer la performance
            String performance = getPerformanceLevel(occupationRate);

            Map<String, Object> stats = new HashMap<>();
            stats.put("id", vehicle.getId());
            stats.put("marque", vehicle.getMarque());
            stats.put("modele", vehicle.getModele());
            stats.put("immatriculation", vehicle.getImmatriculation());
            stats.put("locationCount", locationCount);
            stats.put("revenue", revenue);
            stats.put("occupationRate", Math.round(occupationRate * 100.0) / 100.0);
            stats.put("performance", performance);
            stats.put("status", vehicle.getStatut());

            return stats;
        }).collect(Collectors.toList());
    }

    // =====================================================
    //  TOP 5 VÉHICULES
    // =====================================================
    public List<Map<String, Object>> getTopVehicles(String period, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> vehicleStats = getVehicleStatistics(period, startDate, endDate);

        return vehicleStats.stream()
                .sorted((a, b) -> Double.compare(
                        (Double) b.get("revenue"),
                        (Double) a.get("revenue")
                ))
                .limit(5)
                .collect(Collectors.toList());
    }

    // =====================================================
    //  DONNÉES POUR GRAPHIQUE DES REVENUS
    // =====================================================
    public Map<String, Object> getRevenueChartData(String period, LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = getDateRange(period, startDate, endDate);

        // Pour "month" : données par jour
        // Pour "quarter" : données par semaine
        // Pour "year" : données par mois
        // Pour "all" ou "custom" : données par mois

        List<Map<String, Object>> chartData = new ArrayList<>();

        if ("month".equals(period)) {
            // Données par jour du mois
            LocalDate current = dateRange.startDate;
            while (!current.isAfter(dateRange.endDate)) {
                double dailyRevenue = calculateDailyRevenue(current);

                Map<String, Object> dayData = new HashMap<>();
                dayData.put("label", current.getDayOfMonth() + "/" + current.getMonthValue());
                dayData.put("revenue", dailyRevenue);
                dayData.put("date", current.toString());
                chartData.add(dayData);

                current = current.plusDays(1);
            }
        } else {
            // Données par mois (pour les autres périodes)
            YearMonth startMonth = YearMonth.from(dateRange.startDate);
            YearMonth endMonth = YearMonth.from(dateRange.endDate);

            YearMonth currentMonth = startMonth;
            while (!currentMonth.isAfter(endMonth)) {
                double monthlyRevenue = calculateMonthlyRevenue(currentMonth);

                Map<String, Object> monthData = new HashMap<>();
                monthData.put("label", currentMonth.getMonth().toString().substring(0, 3) + " " + currentMonth.getYear());
                monthData.put("revenue", monthlyRevenue);
                monthData.put("year", currentMonth.getYear());
                monthData.put("month", currentMonth.getMonthValue());
                chartData.add(monthData);

                currentMonth = currentMonth.plusMonths(1);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("period", period);
        result.put("data", chartData);
        result.put("total", calculateTotalRevenue(dateRange.startDate, dateRange.endDate));

        return result;
    }

    // =====================================================
    //  RÉPARTITION PAR STATUT DES LOCATIONS
    // =====================================================
    public Map<String, Long> getLocationsByStatus(String period, LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = getDateRange(period, startDate, endDate);
        List<Location> locations = locationRepository.findAll();

        Map<String, Long> statusCount = new HashMap<>();

        // Initialiser tous les statuts à 0
        for (StatutLocation statut : StatutLocation.values()) {
            statusCount.put(statut.name(), 0L);
        }

        // Compter les locations par statut dans la période
        locations.stream()
                .filter(location -> isDateInRange(location.getDateDebut(), dateRange.startDate, dateRange.endDate))
                .forEach(location -> {
                    String statut = location.getStatut().name();
                    statusCount.put(statut, statusCount.get(statut) + 1);
                });

        return statusCount;
    }

    // =====================================================
    //  DERNIÈRES LOCATIONS
    // =====================================================
    public List<Map<String, Object>> getRecentLocations(int limit) {
        return locationRepository.findAll().stream()
                .sorted((a, b) -> b.getDateDebut().compareTo(a.getDateDebut()))
                .limit(limit)
                .map(location -> {
                    Map<String, Object> recent = new HashMap<>();
                    recent.put("id", location.getId());
                    recent.put("clientName", location.getClient().getPrenom() + " " + location.getClient().getNom());
                    recent.put("vehicleInfo", location.getVehicule().getMarque() + " " + location.getVehicule().getModele());
                    recent.put("dateDebut", location.getDateDebut());
                    recent.put("dateFin", location.getDateFin());
                    recent.put("montant", location.getMontantTotalLocation());
                    recent.put("statut", location.getStatut().name());
                    return recent;
                })
                .collect(Collectors.toList());
    }

    // =====================================================
    //  STATISTIQUES CLIENTS
    // =====================================================
    public Map<String, Object> getClientStatistics(String period, LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = getDateRange(period, startDate, endDate);
        List<Client> clients = clientRepository.findAll();
        List<Location> locations = locationRepository.findAll();

        Map<String, Object> stats = new HashMap<>();

        // Total clients
        stats.put("totalClients", clients.size());

        // Clients actifs (avec au moins une location dans la période)
        long activeClients = clients.stream()
                .filter(client -> locations.stream()
                        .anyMatch(location ->
                                location.getClient().getId().equals(client.getId()) &&
                                        isDateInRange(location.getDateDebut(), dateRange.startDate, dateRange.endDate)
                        ))
                .count();
        stats.put("activeClients", activeClients);

        // Top clients par revenu généré
        List<Map<String, Object>> topClients = clients.stream()
                .map(client -> {
                    double clientRevenue = locations.stream()
                            .filter(location -> location.getClient().getId().equals(client.getId()))
                            .filter(location -> isDateInRange(location.getDateDebut(), dateRange.startDate, dateRange.endDate))
                            .mapToDouble(Location::getMontantTotalLocation)
                            .sum();

                    Map<String, Object> clientStats = new HashMap<>();
                    clientStats.put("id", client.getId());
                    clientStats.put("name", client.getPrenom() + " " + client.getNom());
                    clientStats.put("revenue", clientRevenue);
                    clientStats.put("locationCount", locations.stream()
                            .filter(location -> location.getClient().getId().equals(client.getId()))
                            .filter(location -> isDateInRange(location.getDateDebut(), dateRange.startDate, dateRange.endDate))
                            .count());
                    return clientStats;
                })
                .sorted((a, b) -> Double.compare((Double) b.get("revenue"), (Double) a.get("revenue")))
                .limit(5)
                .collect(Collectors.toList());

        stats.put("topClients", topClients);

        return stats;
    }

    // =====================================================
    //  MÉTHODES UTILITAIRES
    // =====================================================

    private double calculateTotalRevenue(LocalDate startDate, LocalDate endDate) {
        return locationRepository.findAll().stream()
                .filter(location -> isDateInRange(location.getDateDebut(), startDate, endDate))
                .mapToDouble(Location::getMontantTotalLocation)
                .sum();
    }

    private double calculateDailyRevenue(LocalDate date) {
        return locationRepository.findAll().stream()
                .filter(location -> location.getDateDebut().equals(date))
                .mapToDouble(Location::getMontantTotalLocation)
                .sum();
    }

    private double calculateMonthlyRevenue(YearMonth yearMonth) {
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        return locationRepository.findAll().stream()
                .filter(location -> !location.getDateDebut().isAfter(endOfMonth) &&
                        !location.getDateFin().isBefore(startOfMonth))
                .mapToDouble(location -> {
                    // Calculer la portion du mois
                    LocalDate start = location.getDateDebut().isBefore(startOfMonth) ? startOfMonth : location.getDateDebut();
                    LocalDate end = location.getDateFin().isAfter(endOfMonth) ? endOfMonth : location.getDateFin();

                    long daysInMonth = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
                    long totalDays = java.time.temporal.ChronoUnit.DAYS.between(location.getDateDebut(), location.getDateFin()) + 1;

                    return location.getMontantTotalLocation() * daysInMonth / totalDays;
                })
                .sum();
    }

    private double calculateOccupationRate(LocalDate startDate, LocalDate endDate) {
        List<Vehicule> vehicles = vehiculeRepository.findAll();
        if (vehicles.isEmpty()) return 0;

        long totalDaysInPeriod = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        long totalPossibleRentalDays = vehicles.size() * totalDaysInPeriod;

        if (totalPossibleRentalDays == 0) return 0;

        long actualRentalDays = locationRepository.findAll().stream()
                .filter(location -> !location.getDateDebut().isAfter(endDate) &&
                        !location.getDateFin().isBefore(startDate))
                .mapToLong(location -> {
                    LocalDate start = location.getDateDebut().isBefore(startDate) ? startDate : location.getDateDebut();
                    LocalDate end = location.getDateFin().isAfter(endDate) ? endDate : location.getDateFin();
                    return java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
                })
                .sum();

        return (double) actualRentalDays / totalPossibleRentalDays * 100;
    }

    private long calculateActiveClients(LocalDate startDate, LocalDate endDate) {
        return clientRepository.findAll().stream()
                .filter(client -> locationRepository.findAll().stream()
                        .anyMatch(location ->
                                location.getClient().getId().equals(client.getId()) &&
                                        isDateInRange(location.getDateDebut(), startDate, endDate)
                        ))
                .count();
    }

    private boolean isDateInRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        return (date.isEqual(startDate) || date.isAfter(startDate)) &&
                (date.isEqual(endDate) || date.isBefore(endDate));
    }

    private String getPerformanceLevel(double occupationRate) {
        if (occupationRate >= 50) return "Excellent";
        if (occupationRate >= 30) return "Bon";
        if (occupationRate >= 15) return "Moyen";
        return "Faible";
    }

    private DateRange getDateRange(String period, LocalDate customStart, LocalDate customEnd) {
        LocalDate startDate;
        LocalDate endDate;
        LocalDate now = LocalDate.now();

        if (period == null || period.equals("all")) {
            // Toute la période : depuis la première location
            Optional<Location> firstLocation = locationRepository.findAll().stream()
                    .min(Comparator.comparing(Location::getDateDebut));
            startDate = firstLocation.map(Location::getDateDebut).orElse(now.minusYears(1));
            endDate = now;
        } else if (period.equals("month")) {
            startDate = now.with(TemporalAdjusters.firstDayOfMonth());
            endDate = now.with(TemporalAdjusters.lastDayOfMonth());
        } else if (period.equals("quarter")) {
            int month = now.getMonthValue();
            int quarterStartMonth = ((month - 1) / 3) * 3 + 1;
            startDate = LocalDate.of(now.getYear(), quarterStartMonth, 1);
            endDate = startDate.plusMonths(3).minusDays(1);
        } else if (period.equals("year")) {
            startDate = LocalDate.of(now.getYear(), 1, 1);
            endDate = LocalDate.of(now.getYear(), 12, 31);
        } else {
            // Période personnalisée
            startDate = customStart != null ? customStart : now.minusMonths(1);
            endDate = customEnd != null ? customEnd : now;
        }

        return new DateRange(startDate, endDate);
    }

    // Classe interne pour gérer les plages de dates
    private static class DateRange {
        LocalDate startDate;
        LocalDate endDate;

        DateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}