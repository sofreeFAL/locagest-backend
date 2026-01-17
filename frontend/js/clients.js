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

function displayClients(clients) {
    const tbody = document.getElementById('clientsTableBody');
    tbody.innerHTML = ''; // Vider le tableau

    tbody.innerHTML = clients.map(client => `
        <tr>
            <!-- COLONNE 1: ID -->
            <td><strong>C${client.id}</strong></td>
            
            <!-- COLONNE 2: NOM COMPLET (Prénom + Nom) -->
            <td>${client.prenom} ${client.nom}</td>
            
            <!-- COLONNE 3: TÉLÉPHONE -->
            <td>${client.telephone}</td>
            
            <!-- COLONNE 4: EMAIL -->
            <td>${client.email}</td>
            
            <!-- COLONNE 5: CNI (numéroCni) -->
            <td>${client.numeroCni}</td>
            
            <!-- COLONNE 6: ADRESSE -->
            <td>${client.adresse || 'N/A'}</td>
            
            <!-- COLONNE 7: HISTORIQUE -->
            <td>
                <span class="history-badge">
                    <i class="fas fa-history" style="margin-right: 5px;"></i>
                    ${getLocationCount(client)} location${getLocationCount(client) !== 1 ? 's' : ''}
                </span>
            </td>
            
            <!-- COLONNE 8: ACTIONS -->
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
    `).join('');
}

function getLocationCount(client) {
    return 0; // Temporaire - à adapter plus tard
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