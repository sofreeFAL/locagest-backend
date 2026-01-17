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

    // Carte Locations (DÉPLACEZ CE BLOC HORS DE clientCard)
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

    // Carte Contrats (CHANGER L'INDEX À 4)
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
// Charger toutes les données
async function loadDashboardData() {
    try {
        showLoading();

        // Charger les données depuis l'API
        const [vehicles, clients, contracts, locations] = await Promise.all([
            fetchData('/vehicules'),
            fetchData('/clients'),
            fetchData('/contrats'),
            fetchData('/locations') // AJOUTER fetchData pour locations
        ]);

        // Mettre à jour les statistiques (CORRIGÉ)
        updateVehicleStats(vehicles);
        updateClientStats(clients, contracts);
        updateContractStats(contracts);
        updateLocationStats(locations); // UTILISER locations

        // Afficher les contrats récents
        displayRecentContracts(contracts);

        // Afficher les locations récentes (optionnel)
        displayRecentLocations(locations);

    } catch (error) {
        console.error('Erreur:', error);
        showError('Impossible de charger les données');
    }
}

// Mettre à jour les statistiques des locations
function updateLocationStats(locations) {
    if (!locations || !Array.isArray(locations)) {
        console.warn('Données locations invalides:', locations);
        return;
    }

    const today = new Date();

    // Initialiser les compteurs
    let ongoing = 0;
    let completed = 0;
    let upcoming = 0;
    let totalRevenue = 0;

    console.log('Locations reçues pour stats:', locations.length); // Debug

    locations.forEach(location => {
        // Calculer le revenu total
        const revenue = location.montantTotalLocation || location.montant || 0;
        totalRevenue += revenue;

        // Déterminer le statut
        if (location.statut) {
            const statutUpper = (location.statut || '').toUpperCase().trim();

            if (statutUpper === 'EN_COURS' || statutUpper === 'ACTIVE') {
                ongoing++;
            } else if (statutUpper === 'TERMINEE' || statutUpper === 'TERMINE' || statutUpper === 'COMPLETED') {
                completed++;
            } else if (statutUpper === 'A_VENIR' || statutUpper === 'UPCOMING') {
                upcoming++;
            } else {
                // Si statut inconnu, déterminer par dates
                determineStatusByDates(location, today);
            }
        } else {
            // Si pas de statut, déterminer par dates
            determineStatusByDates(location, today);
        }

        function determineStatusByDates(loc, currentDate) {
            if (loc.dateDebut && loc.dateFin) {
                const startDate = new Date(loc.dateDebut);
                const endDate = new Date(loc.dateFin);

                if (currentDate < startDate) {
                    upcoming++;
                } else if (currentDate > endDate) {
                    completed++;
                } else {
                    ongoing++;
                }
            } else {
                // Dates manquantes, considérer comme terminé par défaut
                completed++;
            }
        }
    });

    console.log('Résultats locations:', { ongoing, completed, upcoming, totalRevenue }); // Debug

    // Mettre à jour l'interface
    updateCounter('ongoingLocations', ongoing);
    updateCounter('completedLocations', completed);
    updateCounter('upcomingLocations', upcoming);
    updateCounter('totalLocationRevenue', totalRevenue.toFixed(0) + ' €');
}

// Afficher les locations récentes
function displayRecentLocations(locations) {
    // Créez d'abord cette section dans votre HTML si elle n'existe pas
    const locationsTable = document.getElementById('recentLocationsTableBody');
    if (!locationsTable) return; // Si la table n'existe pas, on ne fait rien

    if (!locations || locations.length === 0) {
        locationsTable.innerHTML = `
            <tr>
                <td colspan="7" class="loading-cell">
                    <i class="fas fa-info-circle"></i> Aucune location récente
                </td>
            </tr>
        `;
        return;
    }

    // Trier par date (les plus récents en premier)
    const recent = [...locations]
        .sort((a, b) => {
            const dateA = new Date(a.dateDebut || a.createdAt || 0);
            const dateB = new Date(b.dateDebut || b.createdAt || 0);
            return dateB - dateA;
        })
        .slice(0, 5); // 5 locations maximum

    // Générer le HTML
    locationsTable.innerHTML = recent.map(location => `
        <tr>
            <td><strong>L${location.id || 'N/A'}</strong></td>
            <td>${getClientName(location.client)}</td>
            <td>${getVehicleInfo(location.vehicule)}</td>
            <td>${formatDate(location.dateDebut)}</td>
            <td>${formatDate(location.dateFin)}</td>
            <td style="font-weight: bold; color: #27ae60;">
                ${location.montantTotalLocation || location.montant || 0} €
            </td>
            <td><span class="status-badge ${getLocationStatusClass(location)}">
                ${getLocationStatusText(location.statut)}
            </span></td>
        </tr>
    `).join('');
}

// Obtenir la classe CSS pour le statut de location
function getLocationStatusClass(location) {
    if (!location.statut) return 'status-pending';

    const statutUpper = location.statut.toUpperCase();
    if (statutUpper === 'EN_COURS' || statutUpper === 'ACTIVE') {
        return 'status-active';
    } else if (statutUpper === 'TERMINEE' || statutUpper === 'TERMINE' || statutUpper === 'COMPLETED') {
        return 'status-completed';
    } else if (statutUpper === 'A_VENIR' || statutUpper === 'UPCOMING') {
        return 'status-upcoming';
    }
    return 'status-pending';
}

// Obtenir le texte du statut pour les locations
function getLocationStatusText(status) {
    if (!status) return 'EN ATTENTE';

    switch(status.toUpperCase()) {
        case 'EN_COURS':
            return 'EN COURS';
        case 'TERMINEE':
        case 'TERMINE':
            return 'TERMINÉE';
        case 'A_VENIR':
            return 'À VENIR';
        default:
            return status;
    }
}
async function fetchData(endpoint) {
    try {
        // Essayer d'utiliser l'API existante
        if (window.api) {
            switch(endpoint) {
                case '/vehicules': return await window.api.getVehicules();
                case '/clients': return await window.api.getClients();
                case '/contrats': return await window.api.getContrats();
                case '/locations': return await window.api.getLocations(); // AJOUTER CE CAS
            }
        }

        // Fallback direct
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080${endpoint}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) throw new Error('Erreur API');
        return await response.json();

    } catch (error) {
        console.warn(`Erreur ${endpoint}:`, error);
        return [];
    }
}

// Mettre à jour les statistiques des véhicules - CORRIGÉ POUR VOTRE API
function updateVehicleStats(vehicles) {
    const total = vehicles.length || 0;

    // Variables pour les compteurs
    let available = 0;
    let rented = 0;
    let maintenance = 0;

    console.log('Véhicules reçus pour stats:', vehicles); // Pour déboguer

    vehicles.forEach(vehicle => {
        console.log('Véhicule:', vehicle.id, 'Statut:', vehicle.statut, 'Disponible:', vehicle.disponible); // Debug

        // Si le véhicule a un champ "statut" (PRIORITÉ)
        if (vehicle.statut) {
            const statutUpper = vehicle.statut.toUpperCase().trim();
            console.log('Statut en majuscules:', statutUpper); // Debug

            if (statutUpper === 'DISPONIBLE') {
                available++;
                console.log('Compté comme DISPONIBLE');
            }
            else if (statutUpper === 'LOUE' || statutUpper === 'LOUÉ') {
                rented++;
                console.log('Compté comme LOUE');
            }
            else if (statutUpper.includes('MAINTENANCE')) {
                // Accepte "MAINTENANCE", "EN_MAINTENANCE", "EN MAINTENANCE"
                maintenance++;
                console.log('Compté comme EN MAINTENANCE');
            }
            else {
                // Pour les autres statuts inconnus, vérifier disponible
                if (vehicle.disponible === true) {
                    available++;
                    console.log('Statut inconnu mais disponible=true');
                } else {
                    rented++; // Par défaut loué si pas disponible
                    console.log('Statut inconnu mais disponible=false');
                }
            }
        }
        // Sinon utiliser le champ "disponible" (boolean) - ancienne logique
        else if (vehicle.disponible !== undefined) {
            if (vehicle.disponible === true) {
                available++;
                console.log('Disponible=true (ancienne logique)');
            } else {
                rented++;
                console.log('Disponible=false (ancienne logique)');
            }
        }
        // Si aucun champ, considérer comme disponible
        else {
            available++;
            console.log('Aucun champ statut/disponible');
        }
    });

    console.log('Résultats finaux:', { total, available, rented, maintenance }); // Debug

    updateCounter('totalVehicles', total);
    updateCounter('availableVehicles', available);
    updateCounter('rentedVehicles', rented);
    updateCounter('maintenanceVehicles', maintenance);
}

// Mettre à jour les statistiques des clients
function updateClientStats(clients, contracts) {
    const total = clients.length || 0;

    // Calculer les clients actifs (avec contrats en cours)
    let active = 0;
    if (contracts && Array.isArray(contracts)) {
        const ongoingContractClientIds = contracts
            .filter(c => {
                // Vérifier si le contrat est en cours
                return c.statut === 'EN_COURS' ||
                    c.enCours === true ||
                    (c.dateFin && new Date(c.dateFin) > new Date());
            })
            .map(c => c.clientId || (c.client && c.client.id))
            .filter(id => id);

        active = new Set(ongoingContractClientIds).size;
    }

    const pending = clients.filter(c => c.enAttente === true).length || 0;

    updateCounter('totalClients', total);
    updateCounter('activeClients', active);
    updateCounter('pendingClients', pending);
}

// Mettre à jour les statistiques des contrats
function updateContractStats(contracts) {
    const ongoing = contracts.filter(c => {
        // Contrat en cours
        return c.statut === 'EN_COURS' ||
            c.enCours === true ||
            (c.dateDebut && c.dateFin &&
                new Date() >= new Date(c.dateDebut) &&
                new Date() <= new Date(c.dateFin));
    }).length || 0;

    const completed = contracts.filter(c => {
        // Contrat terminé
        return c.statut === 'TERMINE' ||
            c.termine === true ||
            (c.dateFin && new Date(c.dateFin) < new Date());
    }).length || 0;

    const upcoming = contracts.filter(c => {
        // Contrat à venir
        return c.statut === 'A_VENIR' ||
            (c.dateDebut && new Date(c.dateDebut) > new Date());
    }).length || 0;

    updateCounter('ongoingContracts', ongoing);
    updateCounter('completedContracts', completed);
    updateCounter('upcomingContracts', upcoming);
}

// Afficher les contrats récents
function displayRecentContracts(contracts) {
    const tbody = document.getElementById('contractsTableBody');

    if (!contracts || contracts.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="loading-cell">
                    <i class="fas fa-info-circle"></i> Aucun contrat trouvé
                </td>
            </tr>
        `;
        return;
    }

    // Trier par date (les plus récents en premier)
    const recent = [...contracts]
        .sort((a, b) => {
            const dateA = new Date(a.dateCreation || a.createdAt || a.dateDebut || 0);
            const dateB = new Date(b.dateCreation || b.createdAt || b.dateDebut || 0);
            return dateB - dateA;
        })
        .slice(0, 5); // 5 contrats maximum

    // Générer le HTML
    tbody.innerHTML = recent.map(contract => `
        <tr>
            <td><strong>${contract.numeroContrat || contract.id || 'N/A'}</strong></td>
            <td>${getClientName(contract.client)}</td>
            <td>${getVehicleInfo(contract.vehicule)}</td>
            <td>${formatDate(contract.dateDebut)}</td>
            <td><span class="status-badge status-active">${getStatusText(contract.statut)}</span></td>
            <td>
                <div class="action-buttons">
                    <button class="btn-action" onclick="viewContract(${contract.id})" title="Voir">
                        <i class="fas fa-eye"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

// Obtenir le nom du client
function getClientName(client) {
    if (!client) return 'Client inconnu';
    const firstName = client.prenom || '';
    const lastName = client.nom || '';
    return `${firstName} ${lastName}`.trim() || 'Client inconnu';
}

// Obtenir les infos du véhicule
function getVehicleInfo(vehicle) {
    if (!vehicle) return 'Marque inconnue';
    const brand = vehicle.marque || '';
    const model = vehicle.modele || '';
    return `${brand} ${model}`.trim() || 'Marque inconnue';
}

// Formater la date
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

// Obtenir le texte du statut
function getStatusText(status) {
    if (!status) return 'ACTIF';

    switch(status.toUpperCase()) {
        case 'EN_COURS':
        case 'EN_COURS':
            return 'EN COURS';
        case 'TERMINE':
        case 'TERMINE':
            return 'TERMINÉ';
        case 'A_VENIR':
            return 'À VENIR';
        default:
            return status;
    }
}

// Mettre à jour un compteur
function updateCounter(elementId, value) {
    const element = document.getElementById(elementId);
    if (element) {
        // Animation simple
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
                <td colspan="6" class="loading-cell" style="color: var(--rouge);">
                    <i class="fas fa-exclamation-circle"></i> ${message}
                </td>
            </tr>
        `;
    }
}

// Voir un contrat
function viewContract(contractId) {
    window.location.href = `contract-details.html?id=${contractId}`;
}

// Déconnexion
function logout() {
    localStorage.removeItem('locagest_token');
    localStorage.removeItem('locagest_user');
    window.location.href = 'index.html';
}

// Configurer le rafraîchissement automatique
setInterval(loadDashboardData, 30000);