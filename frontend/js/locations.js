// locations.js - Gestion des locations

document.addEventListener('DOMContentLoaded', function() {
    if (!localStorage.getItem('locagest_token')) {
        window.location.href = 'index.html';
        return;
    }
    loadLocations();
    loadClientsForLocation();
    loadVehiclesForLocation();

    // Initialiser la date d'aujourd'hui
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('locationStartDate').value = today;
    document.getElementById('locationStartDate').min = today;
});

// Variables globales pour stocker les données
let allClients = [];
let allVehicles = [];

async function loadLocations() {
    try {
        showLoadingLocations();
        const locations = await fetchLocations();
        console.log('Locations chargées:', locations);
        displayLocations(locations);
    } catch (error) {
        console.error('Erreur:', error);
        showErrorLocations('Impossible de charger les locations');
    }
}

async function fetchLocations() {
    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch('http://localhost:8080/locations', {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erreur API ${response.status}: ${errorText}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Erreur récupération locations:', error);
        throw error;
    }
}

function displayLocations(locations) {
    const tbody = document.getElementById('locationsTableBody');

    if (!locations || locations.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="9" class="loading-cell">
                    <i class="fas fa-info-circle"></i> Aucune location trouvée
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = locations.map(location => {
        // Utiliser les propriétés du DTO corrigé
        const clientNom = location.clientNom || '';
        const clientPrenom = location.clientPrenom || '';
        const vehiculeMarque = location.vehiculeMarque || '';
        const vehiculeModele = location.vehiculeModele || '';

        // Format des dates
        const formatDate = (dateString) => {
            if (!dateString) return 'N/A';
            const date = new Date(dateString);
            return date.toLocaleDateString('fr-FR');
        };

        // Calcul de la durée
        let duration = 'N/A';
        if (location.dateDebut && location.dateFin) {
            const startDate = new Date(location.dateDebut);
            const endDate = new Date(location.dateFin);
            const diffTime = Math.abs(endDate - startDate);
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            duration = `${diffDays} jours`;
        }

        // Badge de statut
        let statusBadge = '';
        let statusClass = '';

        if (location.statut === 'A_VENIR') {
            statusBadge = '<span class="badge badge-info">À venir</span>';
            statusClass = 'status-a-venir';
        }
        else if (location.statut === 'EN_COURS') {
            statusBadge = '<span class="badge badge-warning">En cours</span>';
            statusClass = 'status-en-cours';
        }
        else if (location.statut === 'TERMINEE') {
            statusBadge = '<span class="badge badge-success">Terminée</span>';
            statusClass = 'status-termine';
        }
        else if (location.statut === 'ANNULEE') {
            statusBadge = '<span class="badge badge-secondary">Annulée</span>';
            statusClass = 'status-annulee';
        }

        return `
        <tr>
            <td><strong>L${location.id}</strong></td>
            <td>${clientPrenom} ${clientNom}</td>
            <td>${vehiculeMarque} ${vehiculeModele}</td>
            <td>${formatDate(location.dateDebut)}</td>
            <td>${formatDate(location.dateFin)}</td>
            <td>${duration}</td>
            <td style="font-weight: bold; color: #27ae60;">
                ${formatCurrency(location.montantTotalLocation || 0)}
            </td>
            <td>${statusBadge}</td>
            <td>
                <div style="display: flex; gap: 8px;">
                    <button onclick="viewLocation(${location.id})" class="btn-icon" title="Voir">
                        <i class="fas fa-eye" style="color: #3498DB;"></i>
                    </button>
                    <button onclick="editLocation(${location.id})" class="btn-icon" title="Modifier">
                        <i class="fas fa-edit" style="color: #FF8C00;"></i>
                    </button>
                    <button onclick="deleteLocation(${location.id})" class="btn-icon" title="Supprimer">
                        <i class="fas fa-trash" style="color: #e74c3c;"></i>
                    </button>
                </div>
            </td>
        </tr>
        `;
    }).join('');
}

// Fonction pour formater la monnaie
function formatCurrency(amount) {
    return new Intl.NumberFormat('fr-FR', {
        style: 'currency',
        currency: 'XOF',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
    }).format(amount);
}

async function loadClientsForLocation() {
    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch('http://localhost:8080/clients', {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            allClients = await response.json();
            const select = document.getElementById('locationClient');
            select.innerHTML = '<option value="">Sélectionner un client</option>';
            allClients.forEach(client => {
                const option = document.createElement('option');
                option.value = client.id;
                option.textContent = `${client.prenom} ${client.nom} - ${client.telephone}`;
                select.appendChild(option);
            });

            // Mettre à jour le select d'édition également
            updateEditClientSelect();
        }
    } catch (error) {
        console.error('Erreur chargement clients:', error);
    }
}

async function loadVehiclesForLocation() {
    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch('http://localhost:8080/vehicules', {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            allVehicles = await response.json();
            const select = document.getElementById('locationVehicle');
            select.innerHTML = '<option value="">Sélectionner un véhicule</option>';
            allVehicles.forEach(vehicle => {
                const option = document.createElement('option');
                option.value = vehicle.id;
                option.textContent = `${vehicle.marque} ${vehicle.modele} - ${vehicle.immatriculation} (${formatCurrency(vehicle.prixParJour)}/jour)`;
                option.setAttribute('data-price', vehicle.prixParJour || 0);
                select.appendChild(option);
            });

            // Mettre à jour le select d'édition également
            updateEditVehicleSelect();
        } else {
            console.error('Erreur API véhicules:', response.status);
            showNotification('Impossible de charger les véhicules', 'error');
        }
    } catch (error) {
        console.error('Erreur chargement véhicules:', error);
        showNotification('Erreur de connexion au serveur', 'error');
    }
}

// Mettre à jour le select des clients dans le modal d'édition
function updateEditClientSelect() {
    const select = document.getElementById('editClient');
    if (!select) return;

    select.innerHTML = '<option value="">Sélectionner un client</option>';
    allClients.forEach(client => {
        const option = document.createElement('option');
        option.value = client.id;
        option.textContent = `${client.prenom} ${client.nom} - ${client.telephone}`;
        select.appendChild(option);
    });
}

// Mettre à jour le select des véhicules dans le modal d'édition
function updateEditVehicleSelect() {
    const select = document.getElementById('editVehicle');
    if (!select) return;

    select.innerHTML = '<option value="">Sélectionner un véhicule</option>';
    allVehicles.forEach(vehicle => {
        const option = document.createElement('option');
        option.value = vehicle.id;
        option.textContent = `${vehicle.marque} ${vehicle.modele} - ${vehicle.immatriculation} (${formatCurrency(vehicle.prixParJour)}/jour)`;
        select.appendChild(option);
    });
}

function calculateAmount() {
    const vehicleSelect = document.getElementById('locationVehicle');
    const startDateInput = document.getElementById('locationStartDate');
    const endDateInput = document.getElementById('locationEndDate');

    const selectedVehicle = vehicleSelect.options[vehicleSelect.selectedIndex];
    const dailyPrice = selectedVehicle ? parseFloat(selectedVehicle.getAttribute('data-price') || 0) : 0;

    document.getElementById('dailyPriceDisplay').textContent = formatCurrency(dailyPrice);

    if (startDateInput.value && endDateInput.value) {
        const startDate = new Date(startDateInput.value);
        const endDate = new Date(endDateInput.value);

        if (endDate < startDate) {
            document.getElementById('daysCountDisplay').textContent = 'Date invalide';
            document.getElementById('totalAmountDisplay').textContent = formatCurrency(0);
            document.getElementById('locationTotalAmount').value = 0;
            return;
        }

        const diffTime = Math.abs(endDate - startDate);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        const totalAmount = diffDays * dailyPrice;

        document.getElementById('daysCountDisplay').textContent = diffDays;
        document.getElementById('totalAmountDisplay').textContent = formatCurrency(totalAmount);
        document.getElementById('locationTotalAmount').value = totalAmount;
    } else {
        document.getElementById('daysCountDisplay').textContent = '0';
        document.getElementById('totalAmountDisplay').textContent = formatCurrency(0);
        document.getElementById('locationTotalAmount').value = 0;
    }
}

function openAddLocationModal() {
    document.getElementById('addLocationModal').style.display = 'flex';

    // Définir la date minimum pour la fin (demain)
    const startDate = document.getElementById('locationStartDate');
    const endDate = document.getElementById('locationEndDate');
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    endDate.min = tomorrow.toISOString().split('T')[0];

    // Réinitialiser
    calculateAmount();
}

function closeAddLocationModal() {
    document.getElementById('addLocationModal').style.display = 'none';
    document.getElementById('addLocationForm').reset();
    calculateAmount();
}

async function saveLocation() {
    const clientId = document.getElementById('locationClient').value;
    const vehicleId = document.getElementById('locationVehicle').value;
    const startDate = document.getElementById('locationStartDate').value;
    const endDate = document.getElementById('locationEndDate').value;
    const totalAmount = document.getElementById('locationTotalAmount').value;
    const notes = document.getElementById('locationNotes').value;

    // Validation
    if (!clientId || !vehicleId || !startDate || !endDate) {
        showNotification('Veuillez remplir tous les champs obligatoires', 'error');
        return;
    }

    if (new Date(endDate) < new Date(startDate)) {
        showNotification('La date de fin doit être après la date de début', 'error');
        return;
    }

    const locationData = {
        clientId: parseInt(clientId),
        vehiculeId: parseInt(vehicleId),
        dateDebut: startDate,
        dateFin: endDate,
        montantTotalLocation: parseFloat(totalAmount),
        notes: notes || null
    };

    console.log('Création location:', locationData);

    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch('http://localhost:8080/locations', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(locationData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erreur API ${response.status}: ${errorText}`);
        }

        const result = await response.json();
        closeAddLocationModal();
        loadLocations();
        showNotification('Location créée avec succès', 'success');

        // Ouvrir directement la page contrats pour créer le contrat
        setTimeout(() => {
            window.location.href = `contracts.html?location=${result.id}`;
        }, 1500);

    } catch (error) {
        console.error('Erreur création location:', error);
        showNotification(error.message || 'Erreur lors de la création de la location', 'error');
    }
}

async function viewLocation(locationId) {
    try {
        showNotification('Chargement des détails...', 'info');

        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080/locations/${locationId}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erreur ${response.status}: ${errorText}`);
        }

        const location = await response.json();

        // Utiliser les propriétés du DTO corrigé
        const clientNom = location.clientNom || '';
        const clientPrenom = location.clientPrenom || '';
        const clientTelephone = location.clientTelephone || '';
        const vehiculeMarque = location.vehiculeMarque || '';
        const vehiculeModele = location.vehiculeModele || '';
        const vehiculeImmatriculation = location.vehiculeImmatriculation || '';
        const vehiculePrixParJour = location.vehiculePrixParJour || 0;

        const formatDate = (dateString) => {
            if (!dateString) return 'N/A';
            const date = new Date(dateString);
            return date.toLocaleDateString('fr-FR');
        };

        let duration = 'N/A';
        if (location.dateDebut && location.dateFin) {
            const startDate = new Date(location.dateDebut);
            const endDate = new Date(location.dateFin);
            const diffTime = Math.abs(endDate - startDate);
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            duration = `${diffDays} jours`;
        }

        // Déterminer le texte du statut
        let statutText = location.statut || 'Inconnu';
        let statutClass = '';
        let actionsHTML = '';

        if (location.statut === 'A_VENIR') {
            statutText = 'À VENIR';
            statutClass = 'status-a-venir';
            actionsHTML = `
                <button class="btn-action" onclick="demarrerLocation(${location.id})" title="Démarrer">
                    <i class="fas fa-play"></i> Démarrer
                </button>
                <button class="btn-action" onclick="openEditLocationModal(${location.id})" title="Modifier">
                    <i class="fas fa-edit"></i> Modifier
                </button>
                <button class="btn-action btn-danger" onclick="annulerLocation(${location.id})" title="Annuler">
                    <i class="fas fa-ban"></i> Annuler
                </button>
            `;
        }
        else if (location.statut === 'EN_COURS') {
            statutText = 'EN COURS';
            statutClass = 'status-en-cours';
            actionsHTML = `
                <button class="btn-action" onclick="retourVehicule(${location.id})" title="Retour véhicule">
                    <i class="fas fa-car"></i> Retour
                </button>
                <button class="btn-action" onclick="prolongerLocation(${location.id})" title="Prolonger">
                    <i class="fas fa-calendar-plus"></i> Prolonger
                </button>
                <button class="btn-action btn-danger" onclick="annulerLocation(${location.id})" title="Annuler">
                    <i class="fas fa-ban"></i> Annuler
                </button>
            `;
        }
        else if (location.statut === 'TERMINEE') {
            statutText = 'TERMINÉE';
            statutClass = 'status-termine';
            actionsHTML = '<span class="text-muted">Aucune action disponible</span>';
        }
        else if (location.statut === 'ANNULEE') {
            statutText = 'ANNULÉE';
            statutClass = 'status-annulee';
            actionsHTML = '<span class="text-muted">Location annulée</span>';
        }

        const details = `
            <div class="location-details">
                <div class="detail-section">
                    <h4><i class="fas fa-info-circle"></i> Informations générales</h4>
                    <div class="detail-grid">
                        <div class="detail-item">
                            <span class="detail-label">ID Location:</span>
                            <span class="detail-value">L${location.id}</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">Statut:</span>
                            <span class="detail-value ${statutClass}" style="font-weight: bold;">
                                ${statutText}
                            </span>
                        </div>
                    </div>
                </div>

                <div class="detail-section">
                    <h4><i class="fas fa-user"></i> Client</h4>
                    <div class="detail-grid">
                        <div class="detail-item">
                            <span class="detail-label">Nom complet:</span>
                            <span class="detail-value">${clientPrenom} ${clientNom}</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">Téléphone:</span>
                            <span class="detail-value">${clientTelephone}</span>
                        </div>
                    </div>
                </div>

                <div class="detail-section">
                    <h4><i class="fas fa-car"></i> Véhicule</h4>
                    <div class="detail-grid">
                        <div class="detail-item">
                            <span class="detail-label">Véhicule:</span>
                            <span class="detail-value">${vehiculeMarque} ${vehiculeModele}</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">Immatriculation:</span>
                            <span class="detail-value">${vehiculeImmatriculation}</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">Prix journalier:</span>
                            <span class="detail-value">${formatCurrency(vehiculePrixParJour)}/jour</span>
                        </div>
                    </div>
                </div>

                <div class="detail-section">
                    <h4><i class="fas fa-calendar"></i> Période de location</h4>
                    <div class="detail-grid">
                        <div class="detail-item">
                            <span class="detail-label">Date début:</span>
                            <span class="detail-value">${formatDate(location.dateDebut)}</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">Date fin:</span>
                            <span class="detail-value">${formatDate(location.dateFin)}</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">Durée:</span>
                            <span class="detail-value">${duration}</span>
                        </div>
                    </div>
                </div>

                <div class="detail-section">
                    <h4><i class="fas fa-money-bill-wave"></i> Finance</h4>
                    <div class="detail-grid">
                        <div class="detail-item">
                            <span class="detail-label">Montant total:</span>
                            <span class="detail-value" style="color: #27ae60; font-weight: bold;">
                                ${formatCurrency(location.montantTotalLocation || 0)}
                            </span>
                        </div>
                    </div>
                </div>

                <div class="detail-section">
                    <h4><i class="fas fa-cogs"></i> Actions</h4>
                    <div class="actions-container">
                        ${actionsHTML}
                    </div>
                </div>
            </div>
        `;

        document.getElementById('locationDetails').innerHTML = details;
        document.getElementById('viewLocationModal').style.display = 'flex';

    } catch (error) {
        console.error('Erreur chargement location:', error);
        showNotification('Impossible de charger les détails de la location: ' + error.message, 'error');
    }
}

function createContractFromLocation() {
    // Récupérer l'ID de location depuis les détails affichés
    const details = document.getElementById('locationDetails').textContent;
    const match = details.match(/L(\d+)/);

    if (match) {
        const locationId = match[1];
        closeViewLocationModal();
        window.location.href = `contracts.html?location=${locationId}`;
    }
}

function closeViewLocationModal() {
    document.getElementById('viewLocationModal').style.display = 'none';
}

function searchLocations() {
    const searchTerm = document.getElementById('searchLocation').value.toLowerCase();
    const rows = document.querySelectorAll('#locationsTableBody tr');
    rows.forEach(row => {
        const text = row.textContent.toLowerCase();
        row.style.display = text.includes(searchTerm) ? '' : 'none';
    });
}

function showLoadingLocations() {
    const tbody = document.getElementById('locationsTableBody');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="9" class="loading-cell">
                    <i class="fas fa-spinner fa-spin"></i> Chargement des locations...
                </td>
            </tr>
        `;
    }
}

function showErrorLocations(message) {
    const tbody = document.getElementById('locationsTableBody');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="9" class="loading-cell" style="color: #f44336;">
                    <i class="fas fa-exclamation-circle"></i> ${message}
                </td>
            </tr>
        `;
    }
}

function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
        <span>${message}</span>
    `;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 5px;
        color: white;
        z-index: 3000;
        display: flex;
        align-items: center;
        gap: 10px;
        background-color: ${type === 'success' ? '#4caf50' : type === 'error' ? '#f44336' : '#3498db'};
        animation: slideIn 0.3s ease-out;
    `;
    document.body.appendChild(notification);
    setTimeout(() => {
        notification.remove();
    }, 3000);
}

function logout() {
    localStorage.removeItem('locagest_token');
    localStorage.removeItem('locagest_user');
    window.location.href = 'index.html';
}

// =====================================================
//  MODIFIER UNE LOCATION
// =====================================================
async function editLocation(locationId) {
    await openEditLocationModal(locationId);
}

async function openEditLocationModal(locationId) {
    try {
        // Afficher le modal
        document.getElementById('editLocationModal').style.display = 'flex';

        // Charger les données de la location
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080/locations/${locationId}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erreur ${response.status}: ${errorText}`);
        }

        const location = await response.json();
        console.log('Location à modifier:', location);

        // Remplir le formulaire d'édition
        document.getElementById('editLocationId').value = location.id;

        // Formater les dates pour l'input date (YYYY-MM-DD)
        const formatDateForInput = (dateString) => {
            if (!dateString) return '';
            const date = new Date(dateString);
            return date.toISOString().split('T')[0];
        };

        document.getElementById('editStartDate').value = formatDateForInput(location.dateDebut);
        document.getElementById('editEndDate').value = formatDateForInput(location.dateFin);
        document.getElementById('editTotalAmount').value = location.montantTotalLocation || 0;
        document.getElementById('editLocationStatus').value = location.statut || 'A_VENIR';
        document.getElementById('editNotes').value = location.notes || '';

        // Charger les listes déroulantes
        await loadEditSelects();

        // Définir les valeurs sélectionnées
        setTimeout(() => {
            document.getElementById('editClient').value = location.clientId || location.client?.id || '';
            document.getElementById('editVehicle').value = location.vehiculeId || location.vehicule?.id || '';
        }, 100);

    } catch (error) {
        console.error('Erreur:', error);
        showNotification('Impossible de charger la location pour modification', 'error');
        closeEditLocationModal();
    }
}

async function loadEditSelects() {
    // Charger les clients si pas déjà fait
    if (allClients.length === 0) {
        await loadClientsForLocation();
    }

    // Charger les véhicules si pas déjà fait
    if (allVehicles.length === 0) {
        await loadVehiclesForLocation();
    }

    // Mettre à jour les selects
    updateEditClientSelect();
    updateEditVehicleSelect();
}

async function saveEditedLocation() {
    const locationId = document.getElementById('editLocationId').value;
    const clientId = document.getElementById('editClient').value;
    const vehicleId = document.getElementById('editVehicle').value;
    const startDate = document.getElementById('editStartDate').value;
    const endDate = document.getElementById('editEndDate').value;
    const totalAmount = document.getElementById('editTotalAmount').value;
    const status = document.getElementById('editLocationStatus').value;
    const notes = document.getElementById('editNotes').value;

    // Validation
    if (!clientId || !vehicleId || !startDate || !endDate || !totalAmount) {
        showNotification('Veuillez remplir tous les champs obligatoires', 'error');
        return;
    }

    if (new Date(endDate) < new Date(startDate)) {
        showNotification('La date de fin doit être après la date de début', 'error');
        return;
    }

    const locationData = {
        clientId: parseInt(clientId),
        vehiculeId: parseInt(vehicleId),
        dateDebut: startDate,
        dateFin: endDate,
        montantTotalLocation: parseFloat(totalAmount),
        statut: status,
        notes: notes || null
    };

    console.log('Mise à jour location:', locationData);

    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080/locations/${locationId}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(locationData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erreur ${response.status}: ${errorText}`);
        }

        closeEditLocationModal();
        loadLocations();
        showNotification('Location modifiée avec succès', 'success');

    } catch (error) {
        console.error('Erreur modification location:', error);
        showNotification('Erreur lors de la modification: ' + error.message, 'error');
    }
}

// =====================================================
//  SUPPRIMER UNE LOCATION
// =====================================================
async function deleteLocation(locationId) {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cette location ? Cette action est irréversible.')) {
        return;
    }

    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080/locations/${locationId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erreur ${response.status}: ${errorText}`);
        }

        loadLocations();
        showNotification('Location supprimée avec succès', 'success');

    } catch (error) {
        console.error('Erreur suppression location:', error);
        showNotification('Erreur lors de la suppression: ' + error.message, 'error');
    }
}

// =====================================================
//  ANNULER UNE LOCATION
// =====================================================
async function annulerLocation(locationId) {
    if (!confirm('Êtes-vous sûr de vouloir annuler cette location ?')) {
        return;
    }

    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080/locations/${locationId}/annuler`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erreur ${response.status}: ${errorText}`);
        }

        loadLocations();
        showNotification('Location annulée avec succès', 'success');

    } catch (error) {
        console.error('Erreur annulation location:', error);
        showNotification('Erreur lors de l\'annulation: ' + error.message, 'error');
    }
}

// =====================================================
//  DÉMARRER UNE LOCATION (À VENIR → EN COURS)
// =====================================================
async function demarrerLocation(locationId) {
    if (!confirm('Démarrer cette location ? Le véhicule sera marqué comme indisponible.')) {
        return;
    }

    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080/locations/${locationId}/demarrer`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erreur ${response.status}: ${errorText}`);
        }

        closeViewLocationModal();
        loadLocations();
        showNotification('Location démarrée avec succès', 'success');

    } catch (error) {
        console.error('Erreur démarrage location:', error);
        showNotification('Erreur lors du démarrage: ' + error.message, 'error');
    }
}

// =====================================================
//  RETOUR VÉHICULE
// =====================================================
async function retourVehicule(locationId) {
    if (!confirm('Marquer le véhicule comme retourné ? La location sera terminée.')) {
        return;
    }

    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080/locations/${locationId}/retour`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erreur ${response.status}: ${errorText}`);
        }

        closeViewLocationModal();
        loadLocations();
        showNotification('Véhicule retourné avec succès', 'success');

    } catch (error) {
        console.error('Erreur retour véhicule:', error);
        showNotification('Erreur lors du retour: ' + error.message, 'error');
    }
}

// =====================================================
//  PROLONGER UNE LOCATION
// =====================================================
async function prolongerLocation(locationId) {
    const nouvelleDate = prompt('Entrez la nouvelle date de fin (format AAAA-MM-JJ):');
    if (!nouvelleDate) return;

    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080/locations/${locationId}/prolonger`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ nouvelleDateFin: nouvelleDate })
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erreur ${response.status}: ${errorText}`);
        }

        closeViewLocationModal();
        loadLocations();
        showNotification('Location prolongée avec succès', 'success');

    } catch (error) {
        console.error('Erreur prolongation location:', error);
        showNotification('Erreur lors de la prolongation: ' + error.message, 'error');
    }
}

// =====================================================
//  FERMER LE MODAL D'ÉDITION
// =====================================================
function closeEditLocationModal() {
    document.getElementById('editLocationModal').style.display = 'none';
    document.getElementById('editLocationForm').reset();
}