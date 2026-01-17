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
        const client = location.client || {};
        const vehicle = location.vehicule || {};

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
        if (location.statut === 'EN_COURS') {
            statusBadge = '<span class="badge badge-warning">En cours</span>';
        } else if (location.statut === 'TERMINEE') {
            statusBadge = '<span class="badge badge-info">Terminée</span>';
        } else {
            statusBadge = `<span class="badge badge-secondary">${location.statut}</span>`;
        }

        return `
        <tr>
            <td><strong>L${location.id}</strong></td>
            <td>${client.prenom || ''} ${client.nom || ''}</td>
            <td>${vehicle.marque || ''} ${vehicle.modele || ''}</td>
            <td>${formatDate(location.dateDebut)}</td>
            <td>${formatDate(location.dateFin)}</td>
            <td>${duration}</td>
            <td style="font-weight: bold; color: #27ae60;">
                ${location.montantTotalLocation || 0} €
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
            const clients = await response.json();
            const select = document.getElementById('locationClient');
            select.innerHTML = '<option value="">Sélectionner un client</option>';
            clients.forEach(client => {
                const option = document.createElement('option');
                option.value = client.id;
                option.textContent = `${client.prenom} ${client.nom} - ${client.telephone}`;
                select.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Erreur chargement clients:', error);
    }
}

async function loadVehiclesForLocation() {
    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch('http://localhost:8080/vehicles/disponibles', {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const vehicles = await response.json();
            const select = document.getElementById('locationVehicle');
            select.innerHTML = '<option value="">Sélectionner un véhicule</option>';
            vehicles.forEach(vehicle => {
                const option = document.createElement('option');
                option.value = vehicle.id;
                option.textContent = `${vehicle.marque} ${vehicle.modele} - ${vehicle.immatriculation} (${vehicle.prixParJour} €/jour)`;
                option.setAttribute('data-price', vehicle.prixParJour || 0);
                select.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Erreur chargement véhicules:', error);
    }
}

async function updateVehicleList() {
    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch('http://localhost:8080/vehicles/disponibles', {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const vehicles = await response.json();
            const select = document.getElementById('locationVehicle');
            select.innerHTML = '<option value="">Sélectionner un véhicule disponible</option>';

            vehicles.forEach(vehicle => {
                const option = document.createElement('option');
                option.value = vehicle.id;
                option.textContent = `${vehicle.marque} ${vehicle.modele} (${vehicle.immatriculation}) - ${vehicle.prixParJour} €/jour`;
                option.setAttribute('data-price', vehicle.prixParJour || 0);
                select.appendChild(option);
            });

            // Réinitialiser le calcul
            calculateAmount();
        }
    } catch (error) {
        console.error('Erreur mise à jour véhicules:', error);
    }
}

function calculateAmount() {
    const vehicleSelect = document.getElementById('locationVehicle');
    const startDateInput = document.getElementById('locationStartDate');
    const endDateInput = document.getElementById('locationEndDate');

    const selectedVehicle = vehicleSelect.options[vehicleSelect.selectedIndex];
    const dailyPrice = selectedVehicle ? parseFloat(selectedVehicle.getAttribute('data-price') || 0) : 0;

    document.getElementById('dailyPriceDisplay').textContent = `${dailyPrice} €`;

    if (startDateInput.value && endDateInput.value) {
        const startDate = new Date(startDateInput.value);
        const endDate = new Date(endDateInput.value);

        if (endDate < startDate) {
            document.getElementById('daysCountDisplay').textContent = 'Date invalide';
            document.getElementById('totalAmountDisplay').textContent = '0 €';
            document.getElementById('locationTotalAmount').value = 0;
            return;
        }

        const diffTime = Math.abs(endDate - startDate);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        const totalAmount = diffDays * dailyPrice;

        document.getElementById('daysCountDisplay').textContent = diffDays;
        document.getElementById('totalAmountDisplay').textContent = `${totalAmount} €`;
        document.getElementById('locationTotalAmount').value = totalAmount;
    } else {
        document.getElementById('daysCountDisplay').textContent = '0';
        document.getElementById('totalAmountDisplay').textContent = '0 €';
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
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080/locations/${locationId}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Erreur chargement location');
        }

        const location = await response.json();
        const client = location.client || {};
        const vehicle = location.vehicule || {};

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

        const details = `
            <div class="contract-details">
                <div class="detail-row">
                    <span class="detail-label">ID Location:</span>
                    <span class="detail-value">L${location.id}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Client:</span>
                    <span class="detail-value">${client.prenom || ''} ${client.nom || ''}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Téléphone:</span>
                    <span class="detail-value">${client.telephone || 'N/A'}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Véhicule:</span>
                    <span class="detail-value">${vehicle.marque || ''} ${vehicle.modele || ''}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Immatriculation:</span>
                    <span class="detail-value">${vehicle.immatriculation || 'N/A'}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Date début:</span>
                    <span class="detail-value">${formatDate(location.dateDebut)}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Date fin:</span>
                    <span class="detail-value">${formatDate(location.dateFin)}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Durée:</span>
                    <span class="detail-value">${duration}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Montant total:</span>
                    <span class="detail-value" style="color: #27ae60; font-weight: bold;">
                        ${location.montantTotalLocation || 0} €
                    </span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Statut:</span>
                    <span class="detail-value ${location.statut === 'EN_COURS' ? 'status-en-cours' : 'status-termine'}">
                        ${location.statut === 'EN_COURS' ? 'En cours' : 'Terminée'}
                    </span>
                </div>
                ${location.notes ? `
                <div class="detail-row">
                    <span class="detail-label">Notes:</span>
                    <span class="detail-value">${location.notes}</span>
                </div>` : ''}
            </div>
        `;

        document.getElementById('locationDetails').innerHTML = details;
        document.getElementById('viewLocationModal').style.display = 'flex';

    } catch (error) {
        console.error('Erreur chargement location:', error);
        showNotification('Impossible de charger les détails de la location', 'error');
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
        background-color: ${type === 'success' ? '#4caf50' : '#f44336'};
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