package com.taptapsend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Entity
@Table(name = "envoyer")
public class Envoyer {
    @Id
    @Column(name = "idEnv", length = 20)
    private String idEnv;

    @ManyToOne
    @JoinColumn(name = "numEnvoyeur", referencedColumnName = "numtel")
    private Client envoyeur;

    @ManyToOne
    @JoinColumn(name = "numRecepteur", referencedColumnName = "numtel")
    private Client recepteur;

    @Column(name = "montant")
    private int montant;

    @Column(name = "frais_appliques") // Ce champ reste bien sûr
    private int fraisAppliques;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "raison", length = 255)
    private String raison;

    // Constructeurs

    public Envoyer() {
        // Constructeur par défaut (nécessaire pour JPA)
        this.fraisAppliques = 0; // <--- CORRECTION : Initialiser fraisAppliques à 0
    }

    // CONSTRUCTEUR PRINCIPAL pour la création d'un Envoi depuis la couche web/présentation.
    // Ce constructeur prend 6 arguments, car 'fraisAppliques' est calculé par la couche service.
    public Envoyer(String idEnv, Client envoyeur, Client recepteur, int montant, LocalDateTime date, String raison) {
        this.idEnv = idEnv;
        this.envoyeur = envoyeur;
        this.recepteur = recepteur;
        this.montant = montant;
        this.fraisAppliques = 0; // <--- CORRECTION : Initialiser fraisAppliques à 0
        this.date = date;
        this.raison = raison;
    }

    // CONSTRUCTEUR AJOUTÉ pour une initialisation complète (peut être utilisé en interne ou pour des tests)
    // Ce constructeur prend 7 arguments, incluant 'fraisAppliques'.
    public Envoyer(String idEnv, Client envoyeur, Client recepteur, int montant, int fraisAppliques, LocalDateTime date, String raison) {
        this.idEnv = idEnv;
        this.envoyeur = envoyeur;
        this.recepteur = recepteur;
        this.montant = montant;
        this.fraisAppliques = fraisAppliques; // Ici, il est fourni en argument
        this.date = date;
        this.raison = raison;
    }


    // Getters et Setters (inchangés pour la plupart)
    public String getIdEnv() {
        return idEnv;
    }

    public void setIdEnv(String idEnv) {
        this.idEnv = idEnv;
    }

    public Client getEnvoyeur() {
        return envoyeur;
    }

    public void setEnvoyeur(Client envoyeur) {
        this.envoyeur = envoyeur;
    }

    public Client getRecepteur() {
        return recepteur;
    }

    public void setRecepteur(Client recepteur) {
        this.recepteur = recepteur;
    }

    public int getMontant() {
        return montant;
    }

    public void setMontant(int montant) {
        this.montant = montant;
    }

    // Getters et Setters pour fraisAppliques (ESSENTIELS)
    public int getFraisAppliques() {
        return fraisAppliques;
    }

    public void setFraisAppliques(int fraisAppliques) {
        this.fraisAppliques = fraisAppliques;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getRaison() {
        return raison;
    }

    public void setRaison(String raison) {
        this.raison = raison;
    }

    // MÉTHODE DE COMPATIBILITÉ POUR LE JSP (inchangée)
    public Date getDisplayDate() {
        if (this.date == null) {
            return null;
        }
        return Date.from(this.date.atZone(ZoneId.systemDefault()).toInstant());
    }
}