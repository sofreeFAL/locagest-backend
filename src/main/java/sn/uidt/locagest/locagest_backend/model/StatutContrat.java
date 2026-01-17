package sn.uidt.locagest.locagest_backend.model;

public enum StatutContrat {
    EN_ATTENTE, // Pour les locations À VENIR (nouveau)
    ACTIF,      // Pour les locations EN COURS
    TERMINE,    // Pour les locations TERMINÉES
    ANNULE      // Pour les locations ANNULÉES (nouveau)
}