// Dashboard.js - Version CORRIGÉE

document.addEventListener('DOMContentLoaded', function() {
    // Vérifier l'authentification
    if (!localStorage.getItem('locagest_token')) {
        window.location.href = 'index.html';
        return;
    }

    // Rendre les cartes cliquables
    makeCardsClickable();

    // Charger les données
    loadDashboardData();
});

// Rendre les cartes cliquables
function makeCardsClickable() {
    // Carte Véhicules
    const vehicleCard = document.querySelector('.stat-card.ultra-compact:nth-child(1)');
    if (vehicleCard) {
        vehicleCard.style.cursor = 'pointer';
        vehicleCard.addEventListener('click', function() {
            window.location.href = 'vehicles.html';
        });

        vehicleCard.style.transition = 'all 0.3s';
        vehicleCard.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px)';
            this.style.boxShadow = '0 5px 15px rgba(255, 140, 0, 0.3)';
        });
        vehicleCard.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '0 3px 10px rgba(0,0,0,0.2)';
        });
    }

    // Carte Clients
    const clientCard = document.querySelector('.stat-card.ultra-compact:nth-child(2)');
    if (clientCard) {
        clientCard.style.cursor = 'pointer';
        clientCard.addEventListener('click', function() {
            window.location.href = 'clients.html';
        });

        clientCard.style.transition = 'all 0.3s';
        clientCard.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px)';
            this.style.boxShadow = '0 5px 15px rgba(52, 152, 219, 0.3)';
        });
        clientCard.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '0 3px 10px rgba(0,0,0,0.2)';
        });
    }

    // Carte Locations
    const locationCard = document.querySelector('.stat-card.ultra-compact:nth-child(3)');
    if (locationCard) {
        locationCard.style.cursor = 'pointer';
        locationCard.addEventListener('click', function() {
            window.location.href = 'locations.html';
        });

        locationCard.style.transition = 'all 0.3s';
        locationCard.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px)';
            this.style.boxShadow = '0 5px 15px rgba(155, 89, 182, 0.3)';
        });
        locationCard.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '0 3px 10px rgba(0,0,0,0.2)';
        });
    }

    // Carte Contrats
    const contractCard = document.querySelector('.stat-card.ultra-compact:nth-child(4)');
    if (contractCard) {
        contractCard.style.cursor = 'pointer';
        contractCard.addEventListener('click', function() {
            window.location.href = 'contracts.html';
        });

        contractCard.style.transition = 'all 0.3s';
        contractCard.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px)';
            this.style.boxShadow = '0 5px 15px rgba(39, 174, 96, 0.3)';
        });
        contractCard.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '0 3px 10px rgba(0,0,0,0.2)';
        });
    }
}

// =====================================================
//  CHARGER TOUTES LES DONNÉES - CORRIGÉ
// =====================================================
async function loadDashboardData() {
    try {
        showLoading();
        console.log('Début chargement dashboard...');

        // Charger TOUTES les données nécessaires
        const [vehicles, clients, locations, contrats] = await Promise.all([
            fetchData('/vehicules'),
            fetchData('/clients'),
            fetchData('/locations'),
            fetchData('/contrats') // AJOUTER LES VRAIS CONTRATS
        ]);

        console.log('Données récupérées:', {
            vehicles: vehicles.length,
            clients: clients.length,
            locations: locations.length,
            contrats: contrats.length
        });

        // Mettre à jour les statistiques
        updateVehicleStats(vehicles);
        updateClientStats(clients, locations, contrats);
        updateLocationStats(locations);
        updateContractStats(contrats);

        // Afficher les CONTRATS récents (pas les locations)
        displayRecentContracts(contrats);

    } catch (error) {
        console.error('Erreur chargement dashboard:', error);
        showError('Impossible de charger les données');
    }
}

// =====================================================
//  METTRE À JOUR LES STATISTIQUES DES VÉHICULES
// =====================================================
function updateVehicleStats(vehicles) {
    const total = vehicles.length || 0;
    let available = 0;
    let rented = 0;
    let maintenance = 0;

    console.log('Calcul stats véhicules:', total);

    vehicles.forEach(vehicle => {
        const statut = (vehicle.statut || '').toUpperCase();

        if (statut === 'DISPONIBLE') {
            available++;
        } else if (statut === 'LOUE' || statut === 'LOUÉ') {
            rented++;
        } else if (statut.includes('MAINTENANCE')) {
            maintenance++;
        } else {
            // Par défaut, vérifier le champ disponible
            if (vehicle.disponible === true) available++;
            else rented++;
        }
    });

    console.log('Résultats véhicules:', { total, available, rented, maintenance });

    updateCounter('totalVehicles', total);
    updateCounter('availableVehicles', available);
    updateCounter('rentedVehicles', rented);
    updateCounter('maintenanceVehicles', maintenance);
}

// =====================================================
//  METTRE À JOUR LES STATISTIQUES DES CLIENTS
// =====================================================
function updateClientStats(clients, locations, contrats) {
    const total = clients.length || 0;
    let active = 0;
    let pending = 0;

    console.log('Calcul stats clients:', total);

    // Compter les clients avec des locations actives
    clients.forEach(client => {
        // Vérifier si le client a des locations
        const hasLocations = locations.some(location =>
            location.clientId == client.id ||
            (location.client && location.client.id == client.id)
        );

        if (!hasLocations) {
            // Client sans aucune location
            pending++;
        } else {
            // Vérifier si le client a des locations actives
            const hasActiveLocations = locations.some(location => {
                if (location.clientId == client.id || (location.client && location.client.id == client.id)) {
                    const statut = (location.statut || '').toUpperCase();
                    return statut === 'EN_COURS' || statut === 'A_VENIR';
                }
                return false;
            });

            if (hasActiveLocations) {
                active++;
            }
        }
    });

    console.log('Résultats clients:', { total, active, pending });

    updateCounter('totalClients', total);
    updateCounter('activeClients', active);
    updateCounter('pendingClients', pending);
}

// =====================================================
//  METTRE À JOUR LES STATISTIQUES DES LOCATIONS
// =====================================================
function updateLocationStats(locations) {
    if (!locations || !Array.isArray(locations)) {
        console.log('Aucune location pour les stats');
        updateCounter('ongoingLocations', 0);
        updateCounter('completedLocations', 0);
        updateCounter('upcomingLocations', 0);
        updateCounter('totalLocationRevenue', '0 €');
        return;
    }

    let ongoing = 0;
    let completed = 0;
    let upcoming = 0;
    let totalRevenue = 0;

    locations.forEach(location => {
        const statut = (location.statut || '').toUpperCase();
        const montant = location.montantTotalLocation || 0;

        totalRevenue += montant;

        if (statut === 'EN_COURS') {
            ongoing++;
        } else if (statut === 'TERMINEE' || statut === 'TERMINE') {
            completed++;
        } else if (statut === 'A_VENIR') {
            upcoming++;
        }
    });

    console.log('Stats locations:', { ongoing, completed, upcoming, totalRevenue });

    updateCounter('ongoingLocations', ongoing);
    updateCounter('completedLocations', completed);
    updateCounter('upcomingLocations', upcoming);
    updateCounter('totalLocationRevenue', `${totalRevenue.toLocaleString('fr-FR')} €`);
}

// =====================================================
//  METTRE À JOUR LES STATISTIQUES DES CONTRATS
// =====================================================
function updateContractStats(contrats) {
    if (!contrats || !Array.isArray(contrats)) {
        console.log('Aucun contrat pour les stats');
        updateCounter('ongoingContracts', 0);
        updateCounter('completedContracts', 0);
        updateCounter('upcomingContracts', 0);
        return;
    }

    const ongoing = contrats.filter(c =>
        (c.statut || '').toUpperCase() === 'ACTIF'
    ).length || 0;

    const completed = contrats.filter(c => {
        const statut = (c.statut || '').toUpperCase();
        return statut === 'TERMINE' || statut === 'TERMINÉ';
    }).length || 0;

    const upcoming = contrats.filter(c =>
        (c.statut || '').toUpperCase() === 'A_VENIR'
    ).length || 0;

    console.log('Stats contrats:', { ongoing, completed, upcoming });

    updateCounter('ongoingContracts', ongoing);
    updateCounter('completedContracts', completed);
    updateCounter('upcomingContracts', upcoming);
}

// =====================================================
//  AFFICHER LES CONTRATS RÉCENTS
// =====================================================
function displayRecentContracts(contrats) {
    const tbody = document.getElementById('contractsTableBody');

    if (!contrats || contrats.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="loading-cell">
                    <i class="fas fa-info-circle"></i> Aucun contrat récent
                </td>
            </tr>
        `;
        return;
    }

    console.log('Affichage contrats récents:', contrats.length);

    // Trier par date de création (les plus récents en premier)
    const recent = [...contrats]
        .sort((a, b) => {
            const dateA = new Date(a.dateCreation || a.createdAt || 0);
            const dateB = new Date(b.dateCreation || b.createdAt || 0);
            return dateB - dateA;
        })
        .slice(0, 5); // 5 maximum

    // Générer le HTML
    tbody.innerHTML = recent.map(contrat => {
        // Numéro de contrat (format court)
        const contractNumber = contrat.numeroContrat ?
            `CTR-${contrat.numeroContrat.substring(4, 8)}` :
            `CTR-${contrat.id || 'N/A'}`;

        // Infos client depuis la location
        let clientName = 'Client inconnu';
        if (contrat.location && contrat.location.client) {
            const client = contrat.location.client;
            clientName = `${client.prenom || ''} ${client.nom || ''}`.trim() ||
                `Client ${client.id || 'Inconnu'}`;
        } else if (contrat.location && contrat.location.clientId) {
            clientName = `Client ${contrat.location.clientId}`;
        }

        // Infos véhicule depuis la location
        let vehicleInfo = 'Véhicule inconnu';
        if (contrat.location && contrat.location.vehicule) {
            const vehicule = contrat.location.vehicule;
            vehicleInfo = `${vehicule.marque || ''} ${vehicule.modele || ''}`.trim() ||
                `Véhicule ${vehicule.id || 'Inconnu'}`;
        } else if (contrat.location && contrat.location.vehiculeId) {
            vehicleInfo = `Véhicule ${contrat.location.vehiculeId}`;
        }

        // Date début depuis la location
        const startDate = contrat.location && contrat.location.dateDebut ?
            formatDate(contrat.location.dateDebut) : 'N/A';

        // Statut du contrat
        const statut = (contrat.statut || '').toUpperCase();
        let statusClass = 'status-pending';
        let statusText = 'EN ATTENTE';

        if (statut === 'ACTIF') {
            statusClass = 'status-active';
            statusText = 'ACTIF';
        } else if (statut === 'TERMINE' || statut === 'TERMINÉ') {
            statusClass = 'status-completed';
            statusText = 'TERMINÉ';
        } else if (statut === 'A_VENIR') {
            statusClass = 'status-upcoming';
            statusText = 'À VENIR';
        }

        return `
        <tr>
            <td><strong>${contractNumber}</strong></td>
            <td>${clientName}</td>
            <td>${vehicleInfo}</td>
            <td>${startDate}</td>
            <td><span class="status-badge ${statusClass}">${statusText}</span></td>
            <td>
                <div class="action-buttons">
                    <button class="btn-action" onclick="viewContract(${contrat.id})" title="Voir détails">
                        <i class="fas fa-eye"></i>
                    </button>
                </div>
            </td>
        </tr>
        `;
    }).join('');
}

// =====================================================
//  FONCTIONS UTILITAIRES
// =====================================================

// Récupérer les données depuis l'API
async function fetchData(endpoint) {
    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080${endpoint}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            console.warn(`Erreur ${endpoint}: ${response.status}`);
            return [];
        }

        const data = await response.json();
        console.log(`${endpoint} récupéré:`, data.length || 0, 'éléments');
        return data;

    } catch (error) {
        console.error(`Erreur ${endpoint}:`, error);
        return [];
    }
}

// Formater une date
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('fr-FR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        });
    } catch {
        return 'N/A';
    }
}

// Mettre à jour un compteur avec animation
function updateCounter(elementId, value) {
    const element = document.getElementById(elementId);
    if (element) {
        element.style.opacity = '0.5';
        element.style.transform = 'scale(0.9)';

        setTimeout(() => {
            element.textContent = value;
            element.style.opacity = '1';
            element.style.transform = 'scale(1)';
            element.style.transition = 'all 0.3s ease';
        }, 100);
    }
}

// Afficher l'état de chargement
function showLoading() {
    const tbody = document.getElementById('contractsTableBody');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="loading-cell">
                    <i class="fas fa-spinner fa-spin"></i> Chargement des données...
                </td>
            </tr>
        `;
    }
}

// Afficher une erreur
function showError(message) {
    const tbody = document.getElementById('contractsTableBody');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="loading-cell" style="color: #f44336;">
                    <i class="fas fa-exclamation-circle"></i> ${message}
                </td>
            </tr>
        `;
    }
}

// Voir les détails d'un contrat
function viewContract(contractId) {
    console.log('Voir contrat:', contractId);
    window.location.href = `contract-details.html?id=${contractId}`;
}

// Déconnexion
function logout() {
    localStorage.removeItem('locagest_token');
    localStorage.removeItem('locagest_user');
    window.location.href = 'index.html';
}

// Rafraîchissement automatique (toutes les 30 secondes)
setInterval(loadDashboardData, 30000);