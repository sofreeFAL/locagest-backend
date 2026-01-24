// contracts.js - Gestion des contrats

document.addEventListener('DOMContentLoaded', function() {
    if (!localStorage.getItem('locagest_token')) {
        window.location.href = 'index.html';
        return;
    }
    loadContracts();
    setupEventListeners();
});

let currentEditContractId = null;

function setupEventListeners() {
    // Pré-remplir la date d'aujourd'hui dans le formulaire
    const today = new Date().toISOString().split('T')[0];
    const dateInput = document.getElementById('contractCreationDate');
    if (dateInput) {
        dateInput.value = today;
    }

    // Masquer le champ numéro de contrat (auto-généré)
    const contractNumberInput = document.getElementById('contractNumber');
    if (contractNumberInput) {
        contractNumberInput.value = "Auto-généré à la création";
        contractNumberInput.readOnly = true;
        contractNumberInput.style.backgroundColor = '#f5f5f5';
        contractNumberInput.style.color = '#666';
    }
}

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

        // Format des dates
        const formatDate = (dateString) => {
            if (!dateString) return 'N/A';
            const date = new Date(dateString);
            return date.toLocaleDateString('fr-FR');
        };

        // Calcul de la durée
        let duration = 'N/A';
        let montant = '0 FCFA';

        if (location.dateDebut && location.dateFin) {
            const startDate = new Date(location.dateDebut);
            const endDate = new Date(location.dateFin);
            const diffTime = Math.abs(endDate - startDate);
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            duration = `${diffDays} jours`;
        }

        // Récupérer le montant
        if (location.montantTotalLocation !== undefined && location.montantTotalLocation !== null) {
            montant = `${new Intl.NumberFormat('fr-FR').format(location.montantTotalLocation)} FCFA`;
        }

        // Badge de statut
        let statusBadge = '';
        if (contract.statut === 'ACTIF') {
            statusBadge = `
                <span class="history-badge" style="background-color: rgba(255, 140, 0, 0.2); color: #ff8c00;">
                    <i class="fas fa-clock" style="margin-right: 5px;"></i>
                    Actif
                </span>
            `;
        } else if (contract.statut === 'TERMINE') {
            statusBadge = `
                <span class="history-badge" style="background-color: rgba(52, 152, 219, 0.2); color: #3498DB;">
                    <i class="fas fa-check-circle" style="margin-right: 5px;"></i>
                    Terminé
                </span>
            `;
        } else if (contract.statut === 'EN_ATTENTE') {
            statusBadge = `
                <span class="history-badge" style="background-color: rgba(155, 89, 182, 0.2); color: #9b59b6;">
                    <i class="fas fa-hourglass-half" style="margin-right: 5px;"></i>
                    En attente
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
                <button onclick="viewContract(${contract.id})" class="btn-action btn-blue" title="Voir">
                    <i class="fas fa-eye"></i> Voir
                </button>
                <button onclick="closeContract(${contract.id})" class="btn-action btn-orange" title="Clôturer">
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

async function loadAvailableLocations() {
    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch('http://localhost:8080/contrats/locations/sans-contrat', {
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
                locations.forEach(location => {
                    const option = document.createElement('option');
                    option.value = location.id;
                    option.textContent = location.displayLabel || `Location #${location.id}`;
                    option.setAttribute('data-details', JSON.stringify(location));
                    select.appendChild(option);
                });
            }
        } else {
            console.error('Erreur API:', response.status);
            const select = document.getElementById('contractLocation');
            select.innerHTML = '<option value="">Erreur de chargement des locations</option>';
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
    // Réinitialiser les détails
    document.getElementById('locationDetails').style.display = 'none';
}

function closeAddContractModal() {
    document.getElementById('addContractModal').style.display = 'none';
    document.getElementById('addContractForm').reset();
    document.getElementById('locationDetails').style.display = 'none';
    const dateInput = document.getElementById('contractCreationDate');
    if (dateInput) {
        dateInput.value = new Date().toISOString().split('T')[0];
    }
}

function updateLocationDetails() {
    const locationId = document.getElementById('contractLocation').value;
    const detailsDiv = document.getElementById('locationDetails');

    if (!locationId) {
        detailsDiv.style.display = 'none';
        return;
    }

    try {
        const select = document.getElementById('contractLocation');
        const selectedOption = select.options[select.selectedIndex];
        const details = JSON.parse(selectedOption.getAttribute('data-details') || '{}');

        document.getElementById('detailClient').textContent =
            `${details.clientPrenom || ''} ${details.clientNom || ''}`;
        document.getElementById('detailVehicle').textContent =
            `${details.vehiculeMarque || ''} ${details.vehiculeModele || ''}`;
        document.getElementById('detailDateDebut').textContent = details.dateDebut || '-';
        document.getElementById('detailDateFin').textContent = details.dateFin || '-';
        document.getElementById('detailDuree').textContent =
            details.dateDebut && details.dateFin ?
                calculateDaysBetween(details.dateDebut, details.dateFin) + ' jours' : '-';
        document.getElementById('detailMontant').textContent =
            details.montantTotal ? new Intl.NumberFormat('fr-FR').format(details.montantTotal) + ' FCFA' : '-';

        detailsDiv.style.display = 'block';
    } catch (error) {
        console.error('Erreur affichage détails:', error);
        detailsDiv.style.display = 'none';
    }
}

function calculateDaysBetween(dateDebut, dateFin) {
    try {
        const [day1, month1, year1] = dateDebut.split('/');
        const [day2, month2, year2] = dateFin.split('/');

        const start = new Date(year1, month1 - 1, day1);
        const end = new Date(year2, month2 - 1, day2);

        const diffTime = Math.abs(end - start);
        return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    } catch (e) {
        return 'N/A';
    }
}

async function saveContract() {
    const locationId = document.getElementById('contractLocation').value;
    const status = document.getElementById('contractStatus').value;
    const creationDate = document.getElementById('contractCreationDate').value;

    if (!locationId) {
        showNotification('Veuillez sélectionner une location', 'error');
        return;
    }

    // Le numéro de contrat sera généré automatiquement côté serveur
    const contractData = {
        locationId: parseInt(locationId),
        dateCreation: creationDate,
        statut: status
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

        // Récupérer le montant
        let montant = '0 FCFA';
        if (location.montantTotalLocation !== undefined && location.montantTotalLocation !== null) {
            montant = `${new Intl.NumberFormat('fr-FR').format(location.montantTotalLocation)} FCFA`;
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
                        ${contract.statut === 'ACTIF' ? 'Actif' : contract.statut === 'TERMINE' ? 'Terminé' : contract.statut === 'EN_ATTENTE' ? 'En attente' : contract.statut}
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

async function editContract(contractId) {
    try {
        const contract = await fetchContract(contractId);
        currentEditContractId = contractId;

        document.getElementById('editContractNumber').value = contract.numeroContrat || '';
        document.getElementById('editContractStatus').value = contract.statut || 'ACTIF';

        // Formater la date pour l'input type="date"
        if (contract.dateCreation) {
            const date = new Date(contract.dateCreation);
            const formattedDate = date.toISOString().split('T')[0];
            document.getElementById('editContractCreationDate').value = formattedDate;
        }

        document.getElementById('editContractModal').style.display = 'flex';
    } catch (error) {
        console.error('Erreur chargement contrat pour édition:', error);
        showNotification('Impossible de charger le contrat pour modification', 'error');
    }
}

async function updateContract() {
    const contractId = currentEditContractId;
    if (!contractId) return;

    const numeroContrat = document.getElementById('editContractNumber').value;
    const statut = document.getElementById('editContractStatus').value;
    const dateCreation = document.getElementById('editContractCreationDate').value;

    if (!numeroContrat || !statut || !dateCreation) {
        showNotification('Veuillez remplir tous les champs obligatoires', 'error');
        return;
    }

    const updateData = {
        numeroContrat: numeroContrat,
        statut: statut,
        dateCreation: dateCreation
    };

    try {
        const token = localStorage.getItem('locagest_token');
        const response = await fetch(`http://localhost:8080/contrats/${contractId}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updateData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erreur API ${response.status}: ${errorText}`);
        }

        closeEditContractModal();
        loadContracts();
        showNotification('Contrat modifié avec succès', 'success');
    } catch (error) {
        console.error('Erreur modification contrat:', error);
        showNotification(error.message || 'Erreur lors de la modification du contrat', 'error');
    }
}

function closeEditContractModal() {
    document.getElementById('editContractModal').style.display = 'none';
    currentEditContractId = null;
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