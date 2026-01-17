// clients.js - Gestion des clients (Version API réelle)

document.addEventListener('DOMContentLoaded', function() {
    if (!localStorage.getItem('locagest_token')) {
        window.location.href = 'index.html';
        return;
    }
    loadClients();
});

let currentEditClientId = null;

async function loadClients() {
    try {
        showLoading();
        const clients = await fetchClients();
        console.log('Clients chargés:', clients); // DEBUG
        displayClients(clients);
    } catch (error) {
        console.error('Erreur:', error);
        showError('Impossible de charger les clients');
    }
}

async function fetchClients() {
    try {
        const token = localStorage.getItem('locagest_token');
        console.log('Fetching clients with token:', token ? 'Present' : 'Missing');

        const response = await fetch('http://localhost:8080/clients', {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        console.log('Response status:', response.status);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Error response:', errorText);
            throw new Error(`Erreur API ${response.status}: ${errorText}`);
        }

        const clients = await response.json();
        console.log('Parsed clients:', clients);
        return clients;

    } catch (error) {
        console.error('Erreur récupération clients:', error);
        throw error;
    }
}

async function displayClients(clients) {
    const tbody = document.getElementById('clientsTableBody');
    tbody.innerHTML = '<tr><td colspan="8" class="loading-cell"><i class="fas fa-spinner fa-spin"></i> Chargement des clients...</td></tr>';

    console.log('Nombre total de clients:', clients.length); // Debug

    // Charger les historiques un par un pour debug
    const clientsWithHistory = [];

    for (const client of clients) {
        try {
            console.log(`Chargement historique pour client ${client.id} (${client.prenom} ${client.nom})`); // Debug

            // ESSAYER LES DEUX ENDPOINTS POSSIBLES
            let locationCount = 0;

            // Méthode 1: Essayer d'abord l'endpoint historique
            try {
                const token = localStorage.getItem('locagest_token');
                const response = await fetch(`http://localhost:8080/locations/historique/client/${client.id}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });

                if (response.ok) {
                    const locations = await response.json();
                    locationCount = locations.length || 0;
                    console.log(`  Méthode 1 (historique): ${locationCount} locations`);
                }
            } catch (error1) {
                console.log(`  Méthode 1 échouée: ${error1.message}`);
            }

            // Si méthode 1 donne 0, essayer méthode 2
            if (locationCount === 0) {
                try {
                    const token = localStorage.getItem('locagest_token');
                    const response = await fetch(`http://localhost:8080/locations/client/${client.id}/all`, {
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'Content-Type': 'application/json'
                        }
                    });

                    if (response.ok) {
                        const locations = await response.json();
                        locationCount = locations.length || 0;
                        console.log(`  Méthode 2 (/all): ${locationCount} locations`);
                    }
                } catch (error2) {
                    console.log(`  Méthode 2 échouée: ${error2.message}`);
                }
            }

            clientsWithHistory.push({
                ...client,
                locationCount
            });

        } catch (error) {
            console.error(`Erreur pour client ${client.id}:`, error);
            clientsWithHistory.push({ ...client, locationCount: 0 });
        }
    }

    console.log('Résultats finaux:', clientsWithHistory); // Debug

    // Afficher les clients
    if (clientsWithHistory.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="loading-cell">
                    <i class="fas fa-info-circle"></i> Aucun client trouvé
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = clientsWithHistory.map(client => {
        const historyClass = client.locationCount > 0 ? 'has-history' : 'no-history';
        const historyText = client.locationCount === 0 ?
            'Aucune location' :
            `${client.locationCount} location${client.locationCount !== 1 ? 's' : ''}`;

        return `
        <tr>
            <td><strong>C${client.id}</strong></td>
            <td>${client.prenom} ${client.nom}</td>
            <td>${client.telephone}</td>
            <td>${client.email}</td>
            <td>${client.numeroCni || client.numero_cni || ''}</td>
            <td>${client.adresse || 'N/A'}</td>
            <td>
                <span class="history-badge ${historyClass}">
                    <i class="fas fa-history" style="margin-right: 5px;"></i>
                    ${historyText}
                </span>
            </td>
            <td>
                <div class="action-buttons">
                    <button class="btn-edit" onclick="editClient(${client.id})" title="Modifier">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn-delete" onclick="deleteClient(${client.id})" title="Supprimer">
                        <i class="fas fa-trash"></i>
                    </button>
                    
                </div>
            </td>
        </tr>
        `;
    }).join('');
}

// =====================================================
//  COMPTER TOUTES LES LOCATIONS D'UN CLIENT (TOUS STATUTS) - CORRIGÉ
// =====================================================
async function getLocationCount(clientId) {
    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080/locations/historique/client/${clientId}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const locations = await response.json();
            console.log(`Client ${clientId} a ${locations.length} locations:`, locations); // Debug
            return locations.length || 0;
        }

        console.warn(`Erreur pour client ${clientId}: ${response.status}`);
        return 0;

    } catch (error) {
        console.error(`Erreur chargement locations pour client ${clientId}:`, error);
        return 0;
    }
}

// =====================================================
//  COMPTER LES LOCATIONS ACTIVES (EN_COURS + A_VENIR)
// =====================================================
async function getActiveLocationCount(clientId) {
    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080/locations/client/${clientId}/all`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const locations = await response.json();
            // Compter seulement les locations actives
            return locations.filter(location => {
                const statut = (location.statut || '').toUpperCase();
                return statut === 'EN_COURS' || statut === 'A_VENIR';
            }).length || 0;
        }
        return 0;
    } catch (error) {
        console.error(`Erreur chargement locations actives pour client ${clientId}:`, error);
        return 0;
    }
}
// Fonctions utilitaires pour les statuts (à ajouter)
function getLocationStatusClass(location) {
    if (!location.statut) return 'status-pending';
    const statut = location.statut.toUpperCase();
    if (statut === 'EN_COURS') return 'status-active';
    if (statut === 'TERMINEE') return 'status-completed';
    if (statut === 'A_VENIR') return 'status-upcoming';
    if (statut === 'ANNULEE') return 'status-cancelled';
    return 'status-pending';
}

function getLocationStatusText(status) {
    if (!status) return 'Inconnu';
    switch(status.toUpperCase()) {
        case 'EN_COURS': return 'EN COURS';
        case 'TERMINEE': return 'TERMINÉE';
        case 'A_VENIR': return 'À VENIR';
        case 'ANNULEE': return 'ANNULÉE';
        default: return status;
    }
}
async function editClient(clientId) {
    currentEditClientId = clientId;
    try {
        const client = await loadClientData(clientId);
        document.getElementById('editClientLastName').value = client.nom || '';
        document.getElementById('editClientFirstName').value = client.prenom || '';
        document.getElementById('editClientPhone').value = client.telephone || '';
        document.getElementById('editClientEmail').value = client.email || '';
        document.getElementById('editClientCni').value = client.numero_cni || '';
        document.getElementById('editClientAddress').value = client.adresse || '';
        document.getElementById('editClientId').value = clientId;
        document.getElementById('editClientModal').style.display = 'flex';
    } catch (error) {
        console.error('Erreur chargement client:', error);
        showNotification('Impossible de charger les données du client', 'error');
    }
}

async function loadClientData(clientId) {
    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080/clients/${clientId}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error('Erreur API ' + response.status + ': ' + errorText);
        }
        return await response.json();
    } catch (error) {
        console.error('Erreur récupération client:', error);
        throw error;
    }
}

async function updateClient() {
    if (!currentEditClientId) return;

    const clientData = {
        nom: document.getElementById('editClientLastName').value,
        prenom: document.getElementById('editClientFirstName').value,
        telephone: document.getElementById('editClientPhone').value,
        email: document.getElementById('editClientEmail').value,
        numero_cni: document.getElementById('editClientCni').value,
        adresse: document.getElementById('editClientAddress').value || null
    };

    console.log('Mise à jour client:', clientData);

    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080/clients/${currentEditClientId}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(clientData)
        });

        if (!response.ok) throw new Error('Erreur API ' + response.status);

        closeEditClientModal();
        loadClients();
        showNotification('Client modifié avec succès', 'success');
    } catch (error) {
        console.error('Erreur modification client:', error);
        showNotification('Erreur lors de la modification du client', 'error');
    }
}

function openAddClientModal() {
    document.getElementById('addClientModal').style.display = 'flex';
}

function closeAddClientModal() {
    document.getElementById('addClientModal').style.display = 'none';
    document.getElementById('addClientForm').reset();
}

function closeEditClientModal() {
    document.getElementById('editClientModal').style.display = 'none';
    document.getElementById('editClientForm').reset();
    currentEditClientId = null;
}

async function saveClient() {
    const clientData = {
        nom: document.getElementById('clientLastName').value,
        prenom: document.getElementById('clientFirstName').value,
        telephone: document.getElementById('clientPhone').value,
        email: document.getElementById('clientEmail').value,
        numero_cni: document.getElementById('clientCni').value,
        adresse: document.getElementById('clientAddress').value || null
    };

    console.log('Création client:', clientData);

    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch('http://localhost:8080/clients', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(clientData)
        });

        if (!response.ok) throw new Error('Erreur API ' + response.status);

        closeAddClientModal();
        loadClients();
        showNotification('Client ajouté avec succès', 'success');
    } catch (error) {
        console.error('Erreur sauvegarde client:', error);
        showNotification('Erreur lors de l\'ajout du client', 'error');
    }
}
// =====================================================
//  VOIR L'HISTORIQUE D'UN CLIENT
// =====================================================
async function viewClientHistory(clientId) {
    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080/locations/historique/client/${clientId}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) throw new Error('Erreur API');

        const locations = await response.json();

        let historyHTML = `
            <div class="history-modal">
                <h3><i class="fas fa-history"></i> Historique des locations</h3>
                <div class="history-stats">
                    <div class="stat-item">
                        <span class="stat-label">Total locations:</span>
                        <span class="stat-value">${locations.length}</span>
                    </div>
                </div>
        `;

        if (locations.length === 0) {
            historyHTML += `<p class="no-history">Aucune location trouvée pour ce client.</p>`;
        } else {
            historyHTML += `
                <div class="history-table">
                    <table>
                        <thead>
                            <tr>
                                <th>ID Location</th>
                                <th>Véhicule</th>
                                <th>Date début</th>
                                <th>Date fin</th>
                                <th>Montant</th>
                                <th>Statut</th>
                            </tr>
                        </thead>
                        <tbody>
            `;

            locations.forEach(location => {
                const vehicleInfo = `${location.vehiculeMarque || ''} ${location.vehiculeModele || ''}`;
                const formatDate = (date) => date ? new Date(date).toLocaleDateString('fr-FR') : 'N/A';

                historyHTML += `
                    <tr>
                        <td><strong>L${location.id}</strong></td>
                        <td>${vehicleInfo}</td>
                        <td>${formatDate(location.dateDebut)}</td>
                        <td>${formatDate(location.dateFin)}</td>
                        <td style="font-weight: bold; color: #27ae60;">
                            ${location.montantTotalLocation || 0} €
                        </td>
                        <td>
                            <span class="status-badge ${getLocationStatusClass(location)}">
                                ${getLocationStatusText(location.statut)}
                            </span>
                        </td>
                    </tr>
                `;
            });

            historyHTML += `
                        </tbody>
                    </table>
                </div>
            `;
        }

        historyHTML += `</div>`;

        // Créer un modal pour afficher l'historique
        const modal = document.createElement('div');
        modal.className = 'history-modal-overlay';
        modal.innerHTML = `
            <div class="history-modal-content">
                <button class="close-history-btn" onclick="this.parentElement.parentElement.remove()">&times;</button>
                ${historyHTML}
            </div>
        `;

        document.body.appendChild(modal);

    } catch (error) {
        console.error('Erreur chargement historique:', error);
        showNotification('Impossible de charger l\'historique du client', 'error');
    }
}
async function deleteClient(clientId) {
    if (!confirm('Êtes-vous sûr de vouloir supprimer ce client ?')) return;

    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080/clients/${clientId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error('Erreur API ' + response.status + ': ' + errorText);
        }

        loadClients();
        showNotification('Client supprimé avec succès', 'success');
    } catch (error) {
        console.error('Erreur suppression client:', error);
        showNotification('Erreur lors de la suppression du client', 'error');
    }
}

function searchClients() {
    const searchTerm = document.getElementById('searchClient').value.toLowerCase();
    const rows = document.querySelectorAll('#clientsTableBody tr');
    rows.forEach(row => {
        const text = row.textContent.toLowerCase();
        row.style.display = text.includes(searchTerm) ? '' : 'none';
    });
}

function showLoading() {
    const tbody = document.getElementById('clientsTableBody');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="loading-cell">
                    <i class="fas fa-spinner fa-spin"></i> Chargement des clients...
                </td>
            </tr>
        `;
    }
}

function showError(message) {
    const tbody = document.getElementById('clientsTableBody');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="loading-cell" style="color: #f44336;">
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

// Ajouter les styles CSS
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
`;
document.head.appendChild(style);