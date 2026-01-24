// reports.js - Gestion des rapports et statistiques

document.addEventListener('DOMContentLoaded', function() {
    if (!localStorage.getItem('locagest_token')) {
        window.location.href = 'index.html';
        return;
    }
    loadStatistics();
    loadVehicleFilter();
});

async function loadStatistics() {
    try {
        showLoading();
        const period = document.getElementById('periodFilter').value;

        // Charger les statistiques
        const [summary, vehicleStats, contracts] = await Promise.all([
            fetchSummary(period),
            fetchVehicleStatistics(period),
            fetchContracts() // Pour compter les locations
        ]);

        // Calculer le nombre de locations par véhicule
        const vehicleStatsWithCount = calculateRentalCounts(vehicleStats, contracts);

        // Mettre à jour l'interface
        updateSummaryCards(summary);
        updateVehicleStatsTable(vehicleStatsWithCount);
        updateTopVehiclesList(vehicleStatsWithCount);

    } catch (error) {
        console.error('Erreur chargement statistiques:', error);
        showError('Impossible de charger les statistiques');
    }
}

async function loadVehicleFilter() {
    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch('http://localhost:8080/vehicules', {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const vehicles = await response.json();
            const select = document.getElementById('vehicleFilter');

            vehicles.forEach(vehicle => {
                const option = document.createElement('option');
                option.value = vehicle.id;
                option.textContent = `${vehicle.marque} ${vehicle.modele} (${vehicle.immatriculation})`;
                select.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Erreur chargement véhicules:', error);
    }
}

async function fetchSummary(period) {
    const token = localStorage.getItem('locagest_token');
    const response = await fetch('http://localhost:8080/api/statistics/summary', {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });

    if (!response.ok) {
        throw new Error(`Erreur API: ${response.status}`);
    }

    return await response.json();
}

async function fetchVehicleStatistics(period) {
    const token = localStorage.getItem('locagest_token');
    const response = await fetch('http://localhost:8080/api/statistics/vehicles', {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });

    if (!response.ok) {
        throw new Error(`Erreur API: ${response.status}`);
    }

    return await response.json();
}

async function fetchContracts() {
    const token = localStorage.getItem('locagest_token');
    const response = await fetch('http://localhost:8080/contrats', {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });

    if (!response.ok) {
        throw new Error(`Erreur API: ${response.status}`);
    }

    return await response.json();
}

function calculateRentalCounts(vehicleStats, contracts) {
    if (!contracts || !vehicleStats) return vehicleStats;

    // Créer un dictionnaire pour compter les locations par véhicule
    const rentalCounts = {};

    contracts.forEach(contract => {
        if (contract.location && contract.location.vehicule) {
            const vehicleId = contract.location.vehicule.id;
            rentalCounts[vehicleId] = (rentalCounts[vehicleId] || 0) + 1;
        }
    });

    // Mettre à jour les statistiques des véhicules avec le nombre de locations
    return vehicleStats.map(vehicle => {
        const rentalCount = rentalCounts[vehicle.id] || 0;
        return {
            ...vehicle,
            rentalCount: rentalCount
        };
    });
}

function updateSummaryCards(summary) {
    // Total véhicules
    document.getElementById('totalVehicles').textContent = summary.totalVehicles || 0;

    // Revenus totaux
    const revenue = summary.totalRevenue || 0;
    document.getElementById('totalRevenue').textContent = formatCurrency(revenue);

    // Taux d'occupation
    const occupationRate = summary.occupationRate || 0;
    document.getElementById('occupationRate').textContent = occupationRate.toFixed(1) + '%';

    // Clients actifs
    document.getElementById('activeClients').textContent = summary.activeClients || 0;
}

function updateVehicleStatsTable(vehicleStats) {
    const tbody = document.getElementById('vehicleStatsTable');

    if (!vehicleStats || vehicleStats.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" style="padding: 30px; text-align: center; color: #95a5a6;">
                    <i class="fas fa-info-circle"></i> Aucune donnée disponible
                </td>
            </tr>
        `;
        return;
    }

    // Trier par revenu décroissant
    vehicleStats.sort((a, b) => b.revenue - a.revenue);

    tbody.innerHTML = vehicleStats.map(vehicle => {
        // Formater le taux d'occupation
        const occupationRate = vehicle.occupationRate || 0;
        const formattedOccupation = occupationRate.toFixed(1);

        // Déterminer la couleur selon le taux d'occupation
        const performanceClass = getPerformanceClass(occupationRate);
        const performanceLabel = getPerformanceLabel(occupationRate);

        return `
        <tr style="border-bottom: 1px solid #34495e;">
            <td style="padding: 15px;">
                <div style="display: flex; align-items: center; gap: 10px;">
                    <div style="width: 40px; height: 40px; background-color: #3498db; border-radius: 5px; display: flex; align-items: center; justify-content: center;">
                        <i class="fas fa-car" style="color: white;"></i>
                    </div>
                    <div>
                        <strong style="color: #ecf0f1; font-size: 1rem;">${vehicle.marque || ''} ${vehicle.modele || ''}</strong><br>
                        <small style="color: #95a5a6; font-size: 0.85rem;">${vehicle.immatriculation || 'Non spécifié'}</small>
                    </div>
                </div>
            </td>
            <td style="padding: 15px; text-align: center; font-weight: bold; color: #ecf0f1; font-size: 1.1rem;">
                ${vehicle.rentalCount || 0}
            </td>
            <td style="padding: 15px; text-align: right; font-weight: bold; color: #2ecc71; font-size: 1.1rem;">
                ${formatCurrency(vehicle.revenue || 0)}
            </td>
            <td style="padding: 15px; text-align: center;">
                <div style="display: flex; flex-direction: column; align-items: center; gap: 5px;">
                    <div style="font-size: 1.1rem; font-weight: bold; color: #ecf0f1;">
                        ${formattedOccupation}%
                    </div>
                    <span class="performance-badge ${performanceClass}">
                        ${performanceLabel}
                    </span>
                </div>
            </td>
        </tr>
        `;
    }).join('');
}

function updateTopVehiclesList(vehicleStats) {
    const container = document.getElementById('topVehiclesList');

    if (!vehicleStats || vehicleStats.length === 0) {
        container.innerHTML = `
            <div class="loading-item">
                <i class="fas fa-info-circle"></i> Aucune donnée disponible
            </div>
        `;
        return;
    }

    // Prendre les 5 premiers (déjà triés par revenu)
    const topVehicles = vehicleStats.slice(0, 5);

    container.innerHTML = topVehicles.map((vehicle, index) => {
        const occupationRate = vehicle.occupationRate || 0;
        const formattedOccupation = occupationRate.toFixed(1);
        const performanceClass = getPerformanceClass(occupationRate);
        const performanceLabel = getPerformanceLabel(occupationRate);

        return `
        <div class="top-vehicle-item">
            <div class="top-vehicle-rank">
                <span class="rank-number">${index + 1}</span>
            </div>
            <div class="top-vehicle-info">
                <div class="top-vehicle-name">
                    <strong>${vehicle.marque || ''} ${vehicle.modele || ''}</strong>
                    <small>${vehicle.immatriculation || ''}</small>
                </div>
                <div class="top-vehicle-stats">
                    <span class="revenue-stat">
                        <i class="fas fa-money-bill-wave"></i> ${formatCurrency(vehicle.revenue || 0)}
                    </span>
                    <span class="occupation-stat">
                        <i class="fas fa-chart-line"></i> ${formattedOccupation}%
                    </span>
                    <span class="performance-badge ${performanceClass}" style="margin-top: 0; font-size: 0.75rem; padding: 3px 8px;">
                        ${performanceLabel}
                    </span>
                </div>
            </div>
        </div>
        `;
    }).join('');
}

function getPerformanceClass(occupationRate) {
    // Basé sur votre logique backend
    if (occupationRate >= 50) return "performance-excellent";
    if (occupationRate >= 30) return "performance-good";
    if (occupationRate >= 15) return "performance-medium";
    return "performance-low";
}

function getPerformanceLabel(occupationRate) {
    // Basé sur votre logique backend
    if (occupationRate >= 50) return "Excellent";
    if (occupationRate >= 30) return "Bon";
    if (occupationRate >= 15) return "Moyen";
    return "Faible";
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('fr-FR', {
        style: 'currency',
        currency: 'XOF',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
    }).format(amount);
}

function showLoading() {
    const tbody = document.getElementById('vehicleStatsTable');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" style="padding: 30px; text-align: center; color: #95a5a6;">
                    <i class="fas fa-spinner fa-spin"></i> Chargement des statistiques...
                </td>
            </tr>
        `;
    }

    const topList = document.getElementById('topVehiclesList');
    if (topList) {
        topList.innerHTML = `
            <div class="loading-item">
                <i class="fas fa-spinner fa-spin"></i> Chargement...
            </div>
        `;
    }
}

function showError(message) {
    const tbody = document.getElementById('vehicleStatsTable');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" style="padding: 30px; text-align: center; color: #f44336;">
                    <i class="fas fa-exclamation-circle"></i> ${message}
                </td>
            </tr>
        `;
    }

    const topList = document.getElementById('topVehiclesList');
    if (topList) {
        topList.innerHTML = `
            <div class="loading-item" style="color: #f44336;">
                <i class="fas fa-exclamation-circle"></i> ${message}
            </div>
        `;
    }
}

// Fonctions d'export (restent les mêmes que précédemment)
function generateExport() {
    const format = document.getElementById('exportFormat').value;
    const period = document.getElementById('exportPeriod').value;

    // Récupérer les données du tableau
    const tableData = getTableData();

    if (tableData.length === 0) {
        alert("Aucune donnée à exporter !");
        closeExportModal();
        return;
    }

    // Générer l'export selon le format
    switch(format) {
        case 'csv':
            exportToCSV(tableData, period);
            break;
        case 'excel':
            exportToExcel(tableData, period);
            break;
        case 'pdf':
            exportToPDF(tableData, period);
            break;
    }

    closeExportModal();
}

function getTableData() {
    const rows = document.querySelectorAll('#vehicleStatsTable tr');
    const data = [];

    rows.forEach(row => {
        const cells = row.querySelectorAll('td');
        if (cells.length >= 4) {
            // Extraire le nom du véhicule
            const vehicleNameDiv = cells[0].querySelector('strong');
            const vehicleModelDiv = cells[0].querySelector('small');
            const vehicleName = vehicleNameDiv ? vehicleNameDiv.textContent.trim() : '';
            const vehicleModel = vehicleModelDiv ? vehicleModelDiv.textContent.trim() : '';
            const fullVehicleName = vehicleModel ? `${vehicleName} (${vehicleModel})` : vehicleName;

            // Extraire les autres données
            const rentalCount = cells[1].textContent.trim();
            const revenue = cells[2].textContent.trim();
            const occupationRate = cells[3].querySelector('div:first-child')?.textContent.trim() || '0%';

            data.push({
                vehicule: fullVehicleName,
                locations: rentalCount,
                revenu: revenue,
                taux: occupationRate
            });
        }
    });

    return data;
}

function exportToCSV(tableData, period) {
    let csvContent = "Véhicule;Nombre de locations;Revenu généré;Taux d'occupation\n";

    tableData.forEach(row => {
        // Nettoyer les données
        const vehicle = row.vehicule.replace(/;/g, ' ').replace(/\n/g, ' ');
        const revenue = row.revenu.replace(/[^\d]/g, '');

        csvContent += `${vehicle};${row.locations};${revenue};${row.taux}\n`;
    });

    // Ajouter les KPIs
    const kpiData = getKPIData();
    csvContent += `\n\nStatistiques Globales\n`;
    csvContent += `Total véhicules;${kpiData.totalVehicles}\n`;
    csvContent += `Revenus totaux;${kpiData.totalRevenue.replace(/[^\d]/g, '')}\n`;
    csvContent += `Taux d'occupation moyen;${kpiData.occupationRate}\n`;
    csvContent += `Clients actifs;${kpiData.activeClients}\n`;

    // Créer et télécharger le fichier
    downloadFile(csvContent, `rapport_vehicules_${period}_${getCurrentDate()}.csv`, 'text/csv;charset=utf-8;');

    showNotification('CSV exporté avec succès !', 'success');
}

function exportToExcel(tableData, period) {
    // Pour Excel, on peut utiliser un CSV avec extension .xls
    let csvContent = "Véhicule\tNombre de locations\tRevenu généré\tTaux d'occupation\n";

    tableData.forEach(row => {
        // Nettoyer les données
        const vehicle = row.vehicule.replace(/\t/g, ' ').replace(/\n/g, ' ');
        const revenue = row.revenu.replace(/[^\d]/g, '');
        const taux = row.taux.replace('%', '');

        csvContent += `${vehicle}\t${row.locations}\t${revenue}\t${taux}\n`;
    });

    // Ajouter les KPIs
    const kpiData = getKPIData();
    csvContent += `\n\nStatistiques Globales\n`;
    csvContent += `Total véhicules\t${kpiData.totalVehicles}\n`;
    csvContent += `Revenus totaux\t${kpiData.totalRevenue.replace(/[^\d]/g, '')}\n`;
    csvContent += `Taux d'occupation moyen\t${kpiData.occupationRate.replace('%', '')}\n`;
    csvContent += `Clients actifs\t${kpiData.activeClients}\n`;

    // Créer et télécharger le fichier
    downloadFile(csvContent, `rapport_vehicules_${period}_${getCurrentDate()}.xls`, 'application/vnd.ms-excel;charset=utf-8');

    showNotification('Excel exporté avec succès !', 'success');
}

function exportToPDF(tableData, period) {
    // Créer un HTML pour le PDF
    let htmlContent = `
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Rapport LocalGest - ${period}</title>
            <style>
                body { font-family: Arial, sans-serif; padding: 20px; }
                h1 { color: #3498db; border-bottom: 2px solid #3498db; padding-bottom: 10px; }
                h2 { color: #2c3e50; margin-top: 30px; }
                table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                th { background-color: #3498db; color: white; padding: 12px; text-align: left; }
                td { padding: 10px; border-bottom: 1px solid #ddd; }
                .kpi-box { background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin-top: 20px; }
                .kpi-box h3 { margin-top: 0; color: #2c3e50; }
                .kpi-item { display: flex; justify-content: space-between; margin: 10px 0; }
                .kpi-label { font-weight: bold; color: #555; }
                .kpi-value { color: #27ae60; font-weight: bold; }
                .footer { margin-top: 30px; padding-top: 10px; border-top: 1px solid #ddd; color: #777; font-size: 12px; text-align: center; }
            </style>
        </head>
        <body>
            <h1>Rapport LocalGest</h1>
            <p><strong>Période:</strong> ${period === 'current' ? 'Période actuelle' : 'Toute la période'}</p>
            <p><strong>Date d'export:</strong> ${new Date().toLocaleDateString('fr-FR')}</p>
            
            <h2>Statistiques par véhicule</h2>
            <table>
                <thead>
                    <tr>
                        <th>Véhicule</th>
                        <th style="text-align: center;">Nombre de locations</th>
                        <th style="text-align: right;">Revenu généré</th>
                        <th style="text-align: center;">Taux d'occupation</th>
                    </tr>
                </thead>
                <tbody>
    `;

    tableData.forEach(row => {
        htmlContent += `
            <tr>
                <td>${row.vehicule}</td>
                <td style="text-align: center;">${row.locations}</td>
                <td style="text-align: right;">${row.revenu}</td>
                <td style="text-align: center;">${row.taux}</td>
            </tr>
        `;
    });

    htmlContent += `
                </tbody>
            </table>
            
            <div class="kpi-box">
                <h3>Statistiques Globales</h3>
    `;

    const kpiData = getKPIData();
    htmlContent += `
                <div class="kpi-item">
                    <span class="kpi-label">Total véhicules:</span>
                    <span class="kpi-value">${kpiData.totalVehicles}</span>
                </div>
                <div class="kpi-item">
                    <span class="kpi-label">Revenus totaux:</span>
                    <span class="kpi-value">${kpiData.totalRevenue}</span>
                </div>
                <div class="kpi-item">
                    <span class="kpi-label">Taux d'occupation moyen:</span>
                    <span class="kpi-value">${kpiData.occupationRate}</span>
                </div>
                <div class="kpi-item">
                    <span class="kpi-label">Clients actifs:</span>
                    <span class="kpi-value">${kpiData.activeClients}</span>
                </div>
            </div>
            
            <div class="footer">
                Généré par LocalGest - ${new Date().toLocaleString('fr-FR')}
            </div>
        </body>
        </html>
    `;

    // Créer et télécharger le fichier HTML
    downloadFile(htmlContent, `rapport_vehicules_${period}_${getCurrentDate()}.html`, 'text/html;charset=utf-8');

    showNotification('PDF/HTML exporté avec succès !', 'success');
}

function getKPIData() {
    return {
        totalVehicles: document.getElementById('totalVehicles').textContent,
        totalRevenue: document.getElementById('totalRevenue').textContent,
        occupationRate: document.getElementById('occupationRate').textContent,
        activeClients: document.getElementById('activeClients').textContent
    };
}

function downloadFile(content, filename, type) {
    const blob = new Blob([content], { type: type });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');

    link.href = url;
    link.download = filename;
    link.style.display = 'none';

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
}

function getCurrentDate() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

function showNotification(message, type) {
    // Créer une notification
    const notification = document.createElement('div');
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        background-color: ${type === 'success' ? '#27ae60' : '#e74c3c'};
        color: white;
        border-radius: 5px;
        z-index: 10000;
        box-shadow: 0 4px 12px rgba(0,0,0,0.3);
        display: flex;
        align-items: center;
        gap: 10px;
        animation: slideIn 0.3s ease;
    `;

    notification.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
        <span>${message}</span>
    `;

    document.body.appendChild(notification);

    // Ajouter l'animation CSS
    if (!document.querySelector('#notification-style')) {
        const style = document.createElement('style');
        style.id = 'notification-style';
        style.textContent = `
            @keyframes slideIn {
                from {
                    transform: translateX(100%);
                    opacity: 0;
                }
                to {
                    transform: translateX(0);
                    opacity: 1;
                }
            }
        `;
        document.head.appendChild(style);
    }

    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease forwards';
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 3000);
}

// Ajouter l'animation de sortie
if (!document.querySelector('#notification-out-style')) {
    const style = document.createElement('style');
    style.id = 'notification-out-style';
    style.textContent = `
        @keyframes slideOut {
            from {
                transform: translateX(0);
                opacity: 1;
            }
            to {
                transform: translateX(100%);
                opacity: 0;
            }
        }
    `;
    document.head.appendChild(style);
}