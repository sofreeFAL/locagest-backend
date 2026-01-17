// contracts.js - Gestion des contrats

document.addEventListener('DOMContentLoaded', function() {
    if (!localStorage.getItem('locagest_token')) {
        window.location.href = 'index.html';
        return;
    }
    loadContracts();
    loadLocationsForContract();
});

let currentEditContractId = null;

async function loadContracts() {
    try {
        showLoadingContracts();
        const contracts = await fetchContracts();
        console.log('Contrats chargés:', contracts);
        displayContracts(contracts);
    } catch (error) {
        console.error('Erreur:', error);
        showErrorContracts('Impossible de charger les contrats');
    }
}

async function fetchContracts() {
    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch('http://localhost:8080/contrats', {
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
        console.error('Erreur récupération contrats:', error);
        throw error;
    }
}

function displayContracts(contracts) {
    const tbody = document.getElementById('contractsTableBody');

    if (!contracts || contracts.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="9" class="loading-cell">
                    <i class="fas fa-info-circle"></i> Aucun contrat trouvé
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = contracts.map(contract => {
        const location = contract.location || {};
        const client = location.client || {};
        const vehicle = location.vehicule || {};

        // DEBUG: Afficher la structure de l'objet
        console.log('Contract object:', contract);
        console.log('Location object:', location);
        console.log('Location montantTotalLocation:', location.montantTotalLocation);
        console.log('Location montantTotal:', location.montantTotal);

        // Format des dates
        const formatDate = (dateString) => {
            if (!dateString) return 'N/A';
            const date = new Date(dateString);
            return date.toLocaleDateString('fr-FR');
        };

        // Calcul de la durée
        let duration = 'N/A';
        let montant = '0 €';

        if (location.dateDebut && location.dateFin) {
            const startDate = new Date(location.dateDebut);
            const endDate = new Date(location.dateFin);
            const diffTime = Math.abs(endDate - startDate);
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            duration = `${diffDays} jours`;
        }

        // Récupérer le montant - essayer différentes propriétés
        if (location.montantTotalLocation !== undefined && location.montantTotalLocation !== null) {
            montant = `${location.montantTotalLocation} FCFA`;
        } else if (location.montantTotal !== undefined && location.montantTotal !== null) {
            montant = `${location.montantTotal} FCFA`;
        } else if (location.montant !== undefined && location.montant !== null) {
            montant = `${location.montant} FCFA`;
        } else if (contract.montantTotal !== undefined && contract.montantTotal !== null) {
            montant = `${contract.montantTotal} FCFA`;
        }

        // Badge de statut avec le même style que l'historique
        let statusBadge = '';
        if (contract.statut === 'ACTIF') {
            statusBadge = `
        <span class="history-badge" style="background-color: rgba(255, 140, 0, 0.2); color: #ff8c00;">
            <i class="fas fa-clock" style="margin-right: 5px;"></i>
            En cours
        </span>
    `;
        } else if (contract.statut === 'TERMINE') {
            statusBadge = `
        <span class="history-badge" style="background-color: rgba(52, 152, 219, 0.2); color: #3498DB;">
            <i class="fas fa-check-circle" style="margin-right: 5px;"></i>
            Terminé
        </span>
    `;
        } else {
            statusBadge = `
        <span class="history-badge" style="background-color: rgba(108, 117, 125, 0.2); color: #6c757d;">
            <i class="fas fa-question-circle" style="margin-right: 5px;"></i>
            ${contract.statut}
        </span>
    `;
        }

        // Actions selon le statut
        let actions = '';
        if (contract.statut === 'ACTIF') {
            actions = `
                <button onclick="editContract(${contract.id})" class="btn-icon" title="Modifier">
                    <i class="fas fa-edit" style="color: #FF8C00;"></i>
                </button>
                <button onclick="closeContract(${contract.id})" class="btn-action btn-blue" title="Clôturer">
                    Clôturer
                </button>
            `;
        } else {
            actions = `
                <button onclick="viewContract(${contract.id})" class="btn-action btn-blue" title="Voir">
                    <i class="fas fa-eye"></i> Voir
                </button>
            `;
        }

        return `
        <tr>
            <td><strong>${contract.numeroContrat || 'C' + contract.id}</strong></td>
            <td>${client.prenom || ''} ${client.nom || ''}</td>
            <td>${vehicle.marque || ''} ${vehicle.modele || ''}</td>
            <td>${formatDate(location.dateDebut)}</td>
            <td>${formatDate(location.dateFin)}</td>
            <td>${duration}</td>
            <td style="font-weight: bold; color: #27ae60;">${montant}</td>
            <td>${statusBadge}</td>
            <td>
                <div style="display: flex; gap: 10px; align-items: center;">
                    ${actions}
                    <button onclick="downloadContractPdf(${contract.location?.id || contract.id})" 
                            class="btn-icon" 
                            title="Télécharger PDF">
                        <i class="fas fa-download" style="color: #3498DB;"></i>
                    </button>
                </div>
            </td>
        </tr>
        `;
    }).join('');
}

async function loadLocationsForContract() {
    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch('http://localhost:8080/locations', {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const locations = await response.json();
            const select = document.getElementById('contractLocation');
            select.innerHTML = '<option value="">Sélectionner une location</option>';
            locations.forEach(location => {
                const client = location.client || {};
                const vehicle = location.vehicule || {};
                const option = document.createElement('option');
                option.value = location.id;
                option.textContent = `${client.prenom || ''} ${client.nom || ''} - ${vehicle.marque || ''} ${vehicle.modele || ''}`;
                option.setAttribute('data-datedebut', location.dateDebut || '');
                option.setAttribute('data-datefin', location.dateFin || '');
                option.setAttribute('data-montant', location.montantTotal || 0);
                select.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Erreur chargement locations:', error);
    }
}

async function loadAvailableLocations() {
    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch('http://localhost:8080/locations/sans-contrat', {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const locations = await response.json();
            const select = document.getElementById('contractLocation');
            select.innerHTML = '<option value="">Sélectionner une location sans contrat</option>';

            if (locations.length === 0) {
                const option = document.createElement('option');
                option.value = "";
                option.textContent = "Aucune location disponible sans contrat";
                option.disabled = true;
                select.appendChild(option);
            } else {
                // Pour chaque location, récupérer les détails du client et du véhicule
                for (const location of locations) {
                    // Récupérer les détails du client
                    const clientResponse = await fetch(`http://localhost:8080/clients/${location.clientId}`, {
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'Content-Type': 'application/json'
                        }
                    });

                    // Récupérer les détails du véhicule
                    const vehicleResponse = await fetch(`http://localhost:8080/vehicles/${location.vehiculeId}`, {
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'Content-Type': 'application/json'
                        }
                    });

                    let clientName = 'Client inconnu';
                    let vehicleInfo = 'Véhicule inconnu';

                    if (clientResponse.ok) {
                        const client = await clientResponse.json();
                        clientName = `${client.prenom} ${client.nom}`;
                    }

                    if (vehicleResponse.ok) {
                        const vehicle = await vehicleResponse.json();
                        vehicleInfo = `${vehicle.marque} ${vehicle.modele}`;
                    }

                    // Formater la date
                    const formatDate = (dateString) => {
                        const date = new Date(dateString);
                        return date.toLocaleDateString('fr-FR');
                    };

                    const option = document.createElement('option');
                    option.value = location.id;
                    option.textContent = `Loc#${location.id}: ${clientName} - ${vehicleInfo} (${formatDate(location.dateDebut)} au ${formatDate(location.dateFin)}) - ${location.montantTotalLocation} €`;
                    select.appendChild(option);
                }
            }
        }
    } catch (error) {
        console.error('Erreur chargement locations disponibles:', error);
        const select = document.getElementById('contractLocation');
        select.innerHTML = '<option value="">Erreur de chargement des locations</option>';
    }
}

function openAddContractModal() {
    document.getElementById('addContractModal').style.display = 'flex';
    loadAvailableLocations();
}

function closeAddContractModal() {
    document.getElementById('addContractModal').style.display = 'none';
    document.getElementById('addContractForm').reset();
}

async function saveContract() {
    const locationId = document.getElementById('contractLocation').value;

    if (!locationId) {
        showNotification('Veuillez sélectionner une location', 'error');
        return;
    }

    // Générer un numéro de contrat
    const contractNumber = 'CTR-' + new Date().getTime() + '-' + Math.floor(Math.random() * 1000);

    const contractData = {
        numeroContrat: contractNumber,
        locationId: parseInt(locationId),
        dateCreation: new Date().toISOString().split('T')[0],
        statut: 'ACTIF'
    };

    console.log('Création contrat:', contractData);

    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch('http://localhost:8080/contrats', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(contractData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erreur API ${response.status}: ${errorText}`);
        }

        closeAddContractModal();
        loadContracts();
        showNotification('Contrat créé avec succès', 'success');
    } catch (error) {
        console.error('Erreur création contrat:', error);
        showNotification(error.message || 'Erreur lors de la création du contrat', 'error');
    }
}

async function viewContract(contractId) {
    try {
        const contract = await fetchContract(contractId);

        const location = contract.location || {};
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

        // Récupérer le montant - essayer différentes propriétés
        let montant = '0 €';
        if (location.montantTotalLocation !== undefined && location.montantTotalLocation !== null) {
            montant = `${location.montantTotalLocation} €`;
        } else if (location.montantTotal !== undefined && location.montantTotal !== null) {
            montant = `${location.montantTotal} €`;
        } else if (location.montant !== undefined && location.montant !== null) {
            montant = `${location.montant} €`;
        }

        const details = `
            <div class="contract-details">
                <div class="detail-row">
                    <span class="detail-label">N° Contrat:</span>
                    <span class="detail-value">${contract.numeroContrat || 'C' + contract.id}</span>
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
                    <span class="detail-value" style="color: #27ae60; font-weight: bold;">${montant}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Date création:</span>
                    <span class="detail-value">${formatDate(contract.dateCreation)}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Statut:</span>
                    <span class="detail-value ${contract.statut === 'ACTIF' ? 'status-en-cours' : 'status-termine'}">
                        ${contract.statut === 'ACTIF' ? 'En cours' : 'Terminé'}
                    </span>
                </div>
            </div>
        `;

        document.getElementById('contractDetails').innerHTML = details;
        document.getElementById('viewContractModal').style.display = 'flex';

    } catch (error) {
        console.error('Erreur chargement contrat:', error);
        showNotification('Impossible de charger les détails du contrat', 'error');
    }
}

async function fetchContract(contractId) {
    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080/contrats/${contractId}`, {
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
        console.error('Erreur récupération contrat:', error);
        throw error;
    }
}

async function closeContract(contractId) {
    if (!confirm('Êtes-vous sûr de vouloir clôturer ce contrat ?')) return;

    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080/contrats/${contractId}/terminer`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erreur API ${response.status}: ${errorText}`);
        }

        loadContracts();
        showNotification('Contrat clôturé avec succès', 'success');
    } catch (error) {
        console.error('Erreur clôture contrat:', error);
        showNotification(error.message || 'Erreur lors de la clôture du contrat', 'error');
    }
}

async function downloadContractPdf(locationId) {
    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080/contrats/locations/${locationId}/pdf`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Erreur lors du téléchargement du PDF');
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `contrat-location-${locationId}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);

        showNotification('PDF téléchargé avec succès', 'success');
    } catch (error) {
        console.error('Erreur téléchargement PDF:', error);
        showNotification('Erreur lors du téléchargement du PDF', 'error');
    }
}

function closeViewContractModal() {
    document.getElementById('viewContractModal').style.display = 'none';
}

function searchContracts() {
    const searchTerm = document.getElementById('searchContract').value.toLowerCase();
    const rows = document.querySelectorAll('#contractsTableBody tr');
    rows.forEach(row => {
        const text = row.textContent.toLowerCase();
        row.style.display = text.includes(searchTerm) ? '' : 'none';
    });
}

function showLoadingContracts() {
    const tbody = document.getElementById('contractsTableBody');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="9" class="loading-cell">
                    <i class="fas fa-spinner fa-spin"></i> Chargement des contrats...
                </td>
            </tr>
        `;
    }
}

function showErrorContracts(message) {
    const tbody = document.getElementById('contractsTableBody');
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

async function updateLocationDetails() {
    const locationId = document.getElementById('contractLocation').value;
    const detailsDiv = document.getElementById('locationDetails');

    if (!locationId) {
        detailsDiv.style.display = 'none';
        return;
    }

    try {
        const token = localStorage.getItem('locagest_token');

        // 1. Récupérer la location
        const locationResponse = await fetch(`http://localhost:8080/locations/${locationId}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!locationResponse.ok) {
            throw new Error('Erreur chargement location');
        }

        const location = await locationResponse.json();

        // 2. Récupérer le client
        const clientResponse = await fetch(`http://localhost:8080/clients/${location.clientId}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        // 3. Récupérer le véhicule
        const vehicleResponse = await fetch(`http://localhost:8080/vehicles/${location.vehiculeId}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        let clientName = 'Client inconnu';
        let vehicleInfo = 'Véhicule inconnu';

        if (clientResponse.ok) {
            const client = await clientResponse.json();
            clientName = `${client.prenom} ${client.nom}`;
        }

        if (vehicleResponse.ok) {
            const vehicle = await vehicleResponse.json();
            vehicleInfo = `${vehicle.marque} ${vehicle.modele} (${vehicle.immatriculation})`;
        }

        // 4. Calculer la durée
        const startDate = new Date(location.dateDebut);
        const endDate = new Date(location.dateFin);
        const duration = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24));

        // 5. Formater les dates
        const formatDate = (dateString) => {
            const date = new Date(dateString);
            return date.toLocaleDateString('fr-FR');
        };

        // 6. Afficher les détails
        document.getElementById('detailClient').textContent = clientName;
        document.getElementById('detailVehicle').textContent = vehicleInfo;
        document.getElementById('detailDateDebut').textContent = formatDate(location.dateDebut);
        document.getElementById('detailDateFin').textContent = formatDate(location.dateFin);
        document.getElementById('detailDuree').textContent = `${duration} jours`;
        document.getElementById('detailMontant').textContent = `${location.montantTotalLocation || 0} €`;

        // 7. Afficher la section détails
        detailsDiv.style.display = 'block';

    } catch (error) {
        console.error('Erreur chargement détails location:', error);
        detailsDiv.style.display = 'none';
    }
}

async function debugContract(contractId) {
    try {
        const contract = await fetchContract(contractId);
        console.log('=== DEBUG CONTRACT ===');
        console.log('Contrat complet:', contract);
        console.log('Location:', contract.location);
        console.log('Toutes les clés de location:', Object.keys(contract.location || {}));
        console.log('montantTotalLocation:', contract.location?.montantTotalLocation);
        console.log('montantTotal:', contract.location?.montantTotal);
        console.log('montant:', contract.location?.montant);

        // Afficher toutes les propriétés de la location
        if (contract.location) {
            for (let key in contract.location) {
                console.log(`location.${key}:`, contract.location[key]);
            }
        }
    } catch (error) {
        console.error('Erreur debug:', error);
    }
}

// Appelez cette fonction pour debugger
// debugContract(1); // Remplacez 1 par l'ID du contrat