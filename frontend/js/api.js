// Configuration de l'API LocalGest
const API_CONFIG = {
    BASE_URL: 'http://localhost:8080',
    ENDPOINTS: {
        LOGIN: '/auth/login',
        REGISTER: '/auth/register',
        VEHICULES: '/vehicules',
        CLIENTS: '/clients',
        LOCATIONS: '/locations',
        CONTRATS: '/contrats',
        PAIEMENTS: '/paiements'
    }
};

// Gestionnaire d'authentification
class AuthManager {
    constructor() {
        this.token = localStorage.getItem('locagest_token');
        this.user = JSON.parse(localStorage.getItem('locagest_user') || 'null');
    }

    setAuth(token, user) {
        this.token = token;
        this.user = user;
        localStorage.setItem('locagest_token', token);
        localStorage.setItem('locagest_user', JSON.stringify(user));
    }

    clearAuth() {
        this.token = null;
        this.user = null;
        localStorage.removeItem('locagest_token');
        localStorage.removeItem('locagest_user');
    }

    isAuthenticated() {
        return !!this.token;
    }

    isAdmin() {
        return this.user && this.user.role === 'ROLE_ADMIN';
    }

    getAuthHeader() {
        return this.token ? { 'Authorization': `Bearer ${this.token}` } : {};
    }
}

// Instance globale de AuthManager
const authManager = new AuthManager();

// Service API principal
class APIService {
    constructor() {
        this.baseURL = API_CONFIG.BASE_URL;
    }

    async request(endpoint, method = 'GET', data = null, requiresAuth = true) {
        const url = `${this.baseURL}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            ...(requiresAuth ? authManager.getAuthHeader() : {})
        };

        const options = {
            method,
            headers,
            mode: 'cors',
            credentials: 'include'
        };

        if (data && (method === 'POST' || method === 'PUT' || method === 'PATCH')) {
            options.body = JSON.stringify(data);
        }

        try {
            const response = await fetch(url, options);

            // Gestion des erreurs HTTP
            if (!response.ok) {
                const error = await this.handleError(response);
                throw error;
            }

            // Gestion des réponses sans contenu
            if (response.status === 204 || response.headers.get('content-length') === '0') {
                return null;
            }

            return await response.json();
        } catch (error) {
            console.error(`API Error [${method} ${endpoint}]:`, error);
            throw error;
        }
    }

    async handleError(response) {
        const status = response.status;
        let message = `Erreur ${status}`;

        try {
            const errorData = await response.json();
            message = errorData.message || errorData.error || message;
        } catch {
            // Si la réponse n'est pas du JSON
            message = response.statusText || message;
        }

        // Gestion spécifique des codes d'erreur
        switch (status) {
            case 401:
                authManager.clearAuth();
                message = 'Session expirée. Veuillez vous reconnecter.';
                window.location.href = 'index.html';
                break;
            case 403:
                message = 'Accès non autorisé. Vous n\'avez pas les permissions nécessaires.';
                break;
            case 404:
                message = 'Ressource non trouvée.';
                break;
            case 500:
                message = 'Erreur interne du serveur. Veuillez réessayer plus tard.';
                break;
        }

        return new Error(message);
    }

    // Méthodes d'authentification
    // REMPLACEZ SEULEMENT la méthode login dans votre APIService class
    async login(email, password) {
        try {
            console.log('Tentative de connexion avec:', { email, password });

            // Requête DIRECTE sans utiliser this.request() pour éviter CORS
            const response = await fetch(`${this.baseURL}${API_CONFIG.ENDPOINTS.LOGIN}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({ email, password })
            });

            console.log('Réponse status:', response.status);

            if (!response.ok) {
                const errorText = await response.text();
                console.error('Erreur login:', errorText);
                throw new Error('Identifiants incorrects');
            }

            const data = await response.json();
            console.log('Données reçues:', data);

            if (data && data.token) {
                authManager.setAuth(data.token, data.user || {
                    email: email,
                    role: 'ADMIN'
                });
                return { success: true, user: authManager.user };
            }

            throw new Error('Token non reçu');

        } catch (error) {
            console.error('Erreur login complète:', error);
            return {
                success: false,
                error: error.message || 'Erreur de connexion'
            };
        }
    }

    async logout() {
        authManager.clearAuth();
        return { success: true };
    }

    // Méthodes pour les véhicules
    async getVehicules() {
        return await this.request(API_CONFIG.ENDPOINTS.VEHICULES);
    }

    async createVehicule(vehiculeData) {
        return await this.request(API_CONFIG.ENDPOINTS.VEHICULES, 'POST', vehiculeData);
    }

    async updateVehicule(id, vehiculeData) {
        return await this.request(`${API_CONFIG.ENDPOINTS.VEHICULES}/${id}`, 'PUT', vehiculeData);
    }

    async deleteVehicule(id) {
        return await this.request(`${API_CONFIG.ENDPOINTS.VEHICULES}/${id}`, 'DELETE');
    }

    async searchVehicules(filters) {
        const queryParams = new URLSearchParams(filters).toString();
        return await this.request(`${API_CONFIG.ENDPOINTS.VEHICULES}/search?${queryParams}`);
    }

    // Méthodes pour les clients
    async getClients() {
        return await this.request(API_CONFIG.ENDPOINTS.CLIENTS);
    }

    async createClient(clientData) {
        return await this.request(API_CONFIG.ENDPOINTS.CLIENTS, 'POST', clientData);
    }

    async updateClient(id, clientData) {
        return await this.request(`${API_CONFIG.ENDPOINTS.CLIENTS}/${id}`, 'PUT', clientData);
    }

    async deleteClient(id) {
        return await this.request(`${API_CONFIG.ENDPOINTS.CLIENTS}/${id}`, 'DELETE');
    }

    // Méthodes pour les locations
    async getLocations() {
        return await this.request(API_CONFIG.ENDPOINTS.LOCATIONS);
    }

    async createLocation(locationData) {
        return await this.request(API_CONFIG.ENDPOINTS.LOCATIONS, 'POST', locationData);
    }

    // Méthodes pour les contrats
    async getContrats() {
        return await this.request(API_CONFIG.ENDPOINTS.CONTRATS);
    }

    async getContratPDF(id) {
        const url = `${this.baseURL}${API_CONFIG.ENDPOINTS.CONTRATS}/${id}/pdf`;
        window.open(url, '_blank');
    }

    // Méthodes pour les paiements
    async getPaiements() {
        return await this.request(API_CONFIG.ENDPOINTS.PAIEMENTS);
    }

    async createPaiement(paiementData) {
        return await this.request(API_CONFIG.ENDPOINTS.PAIEMENTS, 'POST', paiementData);
    }

    // Méthodes pour les locations
    async getLocations() {
        return await this.request(API_CONFIG.ENDPOINTS.LOCATIONS);
    }
}

// Création de l'instance globale
const apiService = new APIService();

// Interface globale pour la compatibilité
window.api = {
    // Authentification
    login: (email, password) => apiService.login(email, password),
    logout: () => {
        apiService.logout();
        window.location.href = 'index.html';
    },
    isAuthenticated: () => authManager.isAuthenticated(),
    isAdmin: () => authManager.isAdmin(),

    // Véhicules
    getVehicules: () => apiService.getVehicules(),
    createVehicule: (data) => apiService.createVehicule(data),
    updateVehicule: (id, data) => apiService.updateVehicule(id, data),
    deleteVehicule: (id) => apiService.deleteVehicule(id),
    searchVehicules: (filters) => apiService.searchVehicules(filters),

    // Clients
    getClients: () => apiService.getClients(),
    createClient: (data) => apiService.createClient(data),
    updateClient: (id, data) => apiService.updateClient(id, data),
    deleteClient: (id) => apiService.deleteClient(id),

    // Locations
    getLocations: () => apiService.getLocations(),
    createLocation: (data) => apiService.createLocation(data),

    // Contrats
    getContrats: () => apiService.getContrats(),
    getContratPDF: (id) => apiService.getContratPDF(id),

    // Paiements
    getPaiements: () => apiService.getPaiements(),
    createPaiement: (data) => apiService.createPaiement(data)
};

// Export pour les modules (si supporté)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { apiService, authManager };
}