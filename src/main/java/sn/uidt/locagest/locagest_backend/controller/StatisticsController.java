package sn.uidt.locagest.locagest_backend.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.uidt.locagest.locagest_backend.service.StatisticsService;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    // =====================================================
    //  RÉSUMÉ DES STATISTIQUES (4 KPIs)
    // =====================================================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(statisticsService.getSummary(period, startDate, endDate));
    }

    // =====================================================
    //  STATISTIQUES PAR VÉHICULE
    // =====================================================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/vehicles")
    public ResponseEntity<?> getVehicleStatistics(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(statisticsService.getVehicleStatistics(period, startDate, endDate));
    }

    // =====================================================
    //  TOP 5 VÉHICULES
    // =====================================================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/vehicles/top")
    public ResponseEntity<?> getTopVehicles(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(statisticsService.getTopVehicles(period, startDate, endDate));
    }

    // =====================================================
    //  GRAPHIQUE DES REVENUS (Mensuel/Trimestriel/Annuel)
    // =====================================================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/revenue/chart")
    public ResponseEntity<?> getRevenueChart(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(statisticsService.getRevenueChartData(period, startDate, endDate));
    }

    // =====================================================
    //  RÉPARTITION PAR STATUT
    // =====================================================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/locations/status")
    public ResponseEntity<?> getLocationsByStatus(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(statisticsService.getLocationsByStatus(period, startDate, endDate));
    }

    // =====================================================
    //  DERNIÈRES LOCATIONS
    // =====================================================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/locations/recent")
    public ResponseEntity<?> getRecentLocations(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(statisticsService.getRecentLocations(limit));
    }

    // =====================================================
    //  STATISTIQUES CLIENTS
    // =====================================================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/clients")
    public ResponseEntity<?> getClientStatistics(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(statisticsService.getClientStatistics(period, startDate, endDate));
    }
}