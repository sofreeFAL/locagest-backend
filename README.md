# 🚗 LocaGest – Backend
Système de gestion de location de véhicules  
Backend développé avec **Spring Boot**, **JWT**, **PostgreSQL**

---

##  Description
**LocaGest** est une application backend REST permettant de gérer :
- Les utilisateurs (authentification JWT)
- Les véhicules
- Les clients
- Les locations de véhicules
- Les paiements
- Les rôles (ADMIN / USER)

Ce backend est conçu pour être consommé par une application frontend (à venir).

---

## 🛠️ Technologies utilisées
- Java 17
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Maven
- Hibernate

---

## 📂 Architecture du projet
locagest-backend
├── config → Configuration sécurité (JWT, rôles)
├── controller → API REST
├── dto → DTO (éviter les valeurs null)
├── mapper → Conversion Entity ↔ DTO
├── model → Entités JPA
├── repository → JPA Repositories
├── security → JWT Filter & Service
├── service → Logique métier
└── resources
└── application.properties

---

##  Authentification
L’authentification se fait avec **JWT**.

### Endpoints :
- `POST /auth/register` → créer un utilisateur
- `POST /auth/login` → obtenir un token JWT

### Exemple Header Authorization :


---

## 🚘 Fonctionnalités principales

### 👤 Utilisateurs
- Inscription
- Connexion
- Rôles : `ROLE_USER`, `ROLE_ADMIN`

### 🚗 Véhicules
- Ajouter un véhicule
- Lister les véhicules
- Disponibilité automatique

### 👥 Clients
- Ajouter un client
- Lister les clients

###  Locations
- Créer une location
- Retour de véhicule
- Statut : EN_COURS / TERMINEE

###  Paiements
- Enregistrer un paiement lié à une location

---

##  Tests avec Postman
1. Se connecter (`/auth/login`)
2. Copier le token JWT
3. L’ajouter dans le header :
4. Tester les endpoints sécurisés

---

## 🗄 Base de données
Configurer PostgreSQL dans `application.properties` :

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/locagest_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update

Lancer le projet
mvn spring-boot:run

Le serveur démarre sur :
http://localhost:8080
