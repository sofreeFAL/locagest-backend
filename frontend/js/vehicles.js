// vehicles.js - Gestion des véhicules

document.addEventListener('DOMContentLoaded', function() {
    // Vérifier l'authentification
    if (!localStorage.getItem('locagest_token')) {
        window.location.href = 'index.html';
        return;
    }

    // Charger les véhicules
    loadVehicles();
});

// Charger les véhicules
async function loadVehicles() {
    try {
        showLoading();

        const vehicles = await fetchVehicles();
        displayVehicles(vehicles);

    } catch (error) {
        console.error('Erreur:', error);
        showError('Impossible de charger les véhicules');
    }
}

// Récupérer les véhicules depuis l'API
async function fetchVehicles() {
    try {
        if (window.api && window.api.getVehicules) {
            return await window.api.getVehicules();
        }

        // Fallback direct
        const token = localStorage.getItem('locagest_token');
        const response = await fetch('http://localhost:8080/vehicules', {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) throw new Error('Erreur API');
        return await response.json();

    } catch (error) {
        console.warn('Erreur récupération véhicules:', error);
        return [];
    }
}

// Afficher les véhicules dans le tableau
function displayVehicles(vehicles) {
    const tbody = document.getElementById('vehiclesTableBody');

    if (!vehicles || vehicles.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="loading-cell">
                    <i class="fas fa-info-circle"></i> Aucun véhicule trouvé
                </td>
            </tr>
        `;
        return;
    }

    // Générer le HTML
    tbody.innerHTML = vehicles.map(vehicle => `
        <tr>
            <td><strong>V${vehicle.id}</strong></td>
            <td>${vehicle.marque || 'N/A'}</td>
            <td>${vehicle.modele || 'N/A'}</td>
            <td><code>${vehicle.immatriculation || 'N/A'}</code></td>
            <td>${getStatusBadge(vehicle.disponible, vehicle.statut)}</td>
            <td class="price-cell">${formatPrice(vehicle.prixParJour || vehicle.prixLocation)} FCFA</td>
            <td>
                <div class="action-buttons">
                    <button class="btn-edit" onclick="editVehicle(${vehicle.id})" title="Modifier">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn-delete" onclick="deleteVehicle(${vehicle.id})" title="Supprimer">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

// Obtenir le badge de statut - CORRIGÉ POUR VOTRE API
function getStatusBadge(disponible, statut) {
    console.log('Statut reçu:', statut, 'Disponible:', disponible); // Pour déboguer

    let badgeClass = '';
    let badgeText = 'Inconnu';

    // Nettoyer et normaliser le statut
    const cleanStatut = statut ? statut.toString().trim().toUpperCase() : '';

    // Priorité au champ "statut" si présent
    if (cleanStatut) {
        switch(cleanStatut) {
            case 'DISPONIBLE':
            case 'AVAILABLE':
            case 'TRUE': // Pour compatibilité
                badgeClass = 'status-disponible';
                badgeText = 'Disponible';
                break;
            case 'LOUE':
            case 'RENTED':
            case 'LOUÉ':
                badgeClass = 'status-loue';
                badgeText = 'Loué';
                break;
            case 'MAINTENANCE':
            case 'EN_MAINTENANCE':
            case 'EN MAINTENANCE':
            case 'MAINTENANCE_EN_COURS':
                badgeClass = 'status-maintenance';
                badgeText = 'En maintenance';
                break;
            default:
                // Si le statut contient le mot "maintenance"
                if (cleanStatut.includes('MAINTENANCE')) {
                    badgeClass = 'status-maintenance';
                    badgeText = 'En maintenance';
                } else {
                    badgeClass = 'status-disponible';
                    badgeText = statut || 'Inconnu';
                }
        }
    }
    // Sinon utiliser le champ "disponible" (boolean)
    else if (disponible !== undefined) {
        if (disponible === true || disponible === 'true') {
            badgeClass = 'status-disponible';
            badgeText = 'Disponible';
        } else {
            badgeClass = 'status-loue';
            badgeText = 'Loué';
        }
    }

    console.log('Badge généré:', badgeClass, badgeText); // Pour déboguer
    return `<span class="status-badge ${badgeClass}">${badgeText}</span>`;
}

// Formater le prix - CORRIGÉ POUR VOTRE API
function formatPrice(price) {
    if (!price) return '0';
    return new Intl.NumberFormat('fr-FR').format(price);
}

// Rechercher des véhicules
function searchVehicles() {
    const searchTerm = document.getElementById('searchVehicle').value.toLowerCase();
    const rows = document.querySelectorAll('#vehiclesTableBody tr');

    rows.forEach(row => {
        const text = row.textContent.toLowerCase();
        row.style.display = text.includes(searchTerm) ? '' : 'none';
    });
}

// Gestion du modal
function openAddVehicleModal() {
    document.getElementById('addVehicleModal').style.display = 'flex';
}

function closeAddVehicleModal() {
    document.getElementById('addVehicleModal').style.display = 'none';
    document.getElementById('addVehicleForm').reset();
}

// Sauvegarder un véhicule - MODIFIÉ POUR ENVOYER LE STATUT
async function saveVehicle() {
    const vehicleData = {
        marque: document.getElementById('vehicleBrand').value,
        modele: document.getElementById('vehicleModel').value,
        immatriculation: document.getElementById('vehiclePlate').value,
        prixParJour: parseInt(document.getElementById('vehiclePrice').value)
    };

    // Récupérer le statut depuis le select
    const statusSelect = document.getElementById('vehicleStatus');
    if (statusSelect && statusSelect.value) {
        // Convertir la valeur du select en statut backend
        if (statusSelect.value === 'DISPONIBLE') {
            vehicleData.statut = 'DISPONIBLE';
        } else if (statusSelect.value === 'LOUE') {
            vehicleData.statut = 'LOUE';
        } else if (statusSelect.value === 'MAINTENANCE') {
            vehicleData.statut = 'EN_MAINTENANCE';
        }
    }

    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch('http://localhost:8080/vehicules', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(vehicleData)
        });

        if (!response.ok) throw new Error('Erreur API');

        closeAddVehicleModal();
        loadVehicles();
        showNotification('Véhicule ajouté avec succès', 'success');

    } catch (error) {
        console.error('Erreur sauvegarde véhicule:', error);
        showNotification('Erreur lors de l\'ajout du véhicule', 'error');
    }
}

// Modifier un véhicule
// Modifier un véhicule - AVEC MODAL
let currentEditId = null;

function editVehicle(vehicleId) {
    currentEditId = vehicleId;

    // Charger les données du véhicule
    loadVehicleData(vehicleId);

    // Afficher le modal
    document.getElementById('editVehicleModal').style.display = 'flex';
}

async function loadVehicleData(vehicleId) {
    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080/vehicules/${vehicleId}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) throw new Error('Erreur API');
        const vehicle = await response.json();

        // Remplir le formulaire
        document.getElementById('editBrand').value = vehicle.marque || '';
        document.getElementById('editModel').value = vehicle.modele || '';
        document.getElementById('editPlate').value = vehicle.immatriculation || '';
        document.getElementById('editPrice').value = vehicle.prixParJour || '';

        // Sélectionner le bon statut
        const statusSelect = document.getElementById('editStatus');
        if (vehicle.statut) {
            statusSelect.value = vehicle.statut.toUpperCase();
        } else {
            statusSelect.value = vehicle.disponible ? 'DISPONIBLE' : 'LOUE';
        }

    } catch (error) {
        console.error('Erreur chargement véhicule:', error);
        showNotification('Impossible de charger les données du véhicule', 'error');
    }
}

function closeEditModal() {
    document.getElementById('editVehicleModal').style.display = 'none';
    document.getElementById('editVehicleForm').reset();
    currentEditId = null;
}

async function updateVehicle() {
    if (!currentEditId) return;

    const vehicleData = {
        marque: document.getElementById('editBrand').value,
        modele: document.getElementById('editModel').value,
        immatriculation: document.getElementById('editPlate').value,
        prixParJour: parseInt(document.getElementById('editPrice').value)
    };

    // Gérer le statut
    const statusSelect = document.getElementById('editStatus');
    if (statusSelect && statusSelect.value) {
        vehicleData.statut = statusSelect.value;
    }

    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080/vehicules/${currentEditId}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(vehicleData)
        });

        if (!response.ok) throw new Error('Erreur API');

        closeEditModal();
        loadVehicles(); // Recharger la liste
        showNotification('Véhicule modifié avec succès', 'success');

    } catch (error) {
        console.error('Erreur modification véhicule:', error);
        showNotification('Erreur lors de la modification du véhicule', 'error');
    }
}

// Supprimer un véhicule
async function deleteVehicle(vehicleId) {
    if (!confirm('Êtes-vous sûr de vouloir supprimer ce véhicule ?')) {
        return;
    }

    try {
        if (window.api && window.api.deleteVehicule) {
            await window.api.deleteVehicule(vehicleId);
        } else {
            const token = localStorage.getItem('locagest_token');
            const response = await fetch(`http://localhost:8080/vehicules/${vehicleId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) throw new Error('Erreur API');
        }

        loadVehicles(); // Recharger la liste
        showNotification('Véhicule supprimé avec succès', 'success');

    } catch (error) {
        console.error('Erreur suppression véhicule:', error);
        showNotification('Erreur lors de la suppression du véhicule', 'error');
    }
}

// Afficher l'état de chargement
function showLoading() {
    const tbody = document.getElementById('vehiclesTableBody');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="loading-cell">
                    <i class="fas fa-spinner fa-spin"></i> Chargement des véhicules...
                </td>
            </tr>
        `;
    }
}

// Afficher une erreur
function showError(message) {
    const tbody = document.getElementById('vehiclesTableBody');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="loading-cell" style="color: var(--rouge);">
                    <i class="fas fa-exclamation-circle"></i> ${message}
                </td>
            </tr>
        `;
    }
}

// Notification
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
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
        animation: slideIn 0.3s ease-out;
    `;

    if (type === 'success') {
        notification.style.backgroundColor = 'var(--vert)';
    } else {
        notification.style.backgroundColor = 'var(--rouge)';
    }

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease-out';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Déconnexion
function logout() {
    localStorage.removeItem('locagest_token');
    localStorage.removeItem('locagest_user');
    window.location.href = 'index.html';
}

// Afficher les véhicules dans le tableau
function displayVehicles(vehicles) {
    const tbody = document.getElementById('vehiclesTableBody');

    if (!vehicles || vehicles.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="loading-cell">
                    <i class="fas fa-info-circle"></i> Aucun véhicule trouvé
                </td>
            </tr>
        `;
        return;
    }

    // DEBUG: Afficher les données reçues
    console.log('Véhicules reçus:', vehicles);

    // Générer le HTML
    tbody.innerHTML = vehicles.map(vehicle => `
        <tr>
            <td><strong>V${vehicle.id}</strong></td>
            <td>${vehicle.marque || 'N/A'}</td>
            <td>${vehicle.modele || 'N/A'}</td>
            <td><code>${vehicle.immatriculation || 'N/A'}</code></td>
            <td>${getStatusBadge(vehicle.disponible, vehicle.statut)}</td>
            <td class="price-cell">${formatPrice(vehicle.prixParJour || vehicle.prixLocation)} FCFA</td>
            <td>
                <div class="action-buttons">
                    <button class="btn-edit" onclick="editVehicle(${vehicle.id})" title="Modifier">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn-delete" onclick="deleteVehicle(${vehicle.id})" title="Supprimer">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

// Ajouter l'animation CSS pour les notifications
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    @keyframes slideOut {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(100%); opacity: 0; }
    }
`;
document.head.appendChild(style);