package sn.uidt.locagest.locagest_backend.dto;

public class ClientDTO {

    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private String numeroCni;

    public ClientDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNumeroCni() { return numeroCni; }
    public void setNumeroCni(String numeroCni) { this.numeroCni = numeroCni; }
}
