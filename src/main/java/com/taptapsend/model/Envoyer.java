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
    private double montant; // CHANGÉ: int -> double

    @Column(name = "frais_appliques")
    private double fraisAppliques; // CHANGÉ: int -> double

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "raison", length = 255)
    private String raison;

    // Constructeurs
    public Envoyer() {
        this.fraisAppliques = 0.0; // CHANGÉ: 0 -> 0.0
    }

    public Envoyer(String idEnv, Client envoyeur, Client recepteur, double montant, LocalDateTime date, String raison) { // CHANGÉ: int -> double
        this.idEnv = idEnv;
        this.envoyeur = envoyeur;
        this.recepteur = recepteur;
        this.montant = montant;
        this.fraisAppliques = 0.0; // CHANGÉ: 0 -> 0.0
        this.date = date;
        this.raison = raison;
    }

    public Envoyer(String idEnv, Client envoyeur, Client recepteur, double montant, double fraisAppliques, LocalDateTime date, String raison) { // CHANGÉ: int -> double
        this.idEnv = idEnv;
        this.envoyeur = envoyeur;
        this.recepteur = recepteur;
        this.montant = montant;
        this.fraisAppliques = fraisAppliques;
        this.date = date;
        this.raison = raison;
    }

    // Getters et Setters
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

    public double getMontant() { // CHANGÉ: int -> double
        return montant;
    }

    public void setMontant(double montant) { // CHANGÉ: int -> double
        this.montant = montant;
    }

    public double getFraisAppliques() { // CHANGÉ: int -> double
        return fraisAppliques;
    }

    public void setFraisAppliques(double fraisAppliques) { // CHANGÉ: int -> double
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

    public Date getDisplayDate() {
        if (this.date == null) {
            return null;
        }
        return Date.from(this.date.atZone(ZoneId.systemDefault()).toInstant());
    }
}