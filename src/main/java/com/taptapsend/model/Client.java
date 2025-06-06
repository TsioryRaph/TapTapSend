package com.taptapsend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "client")
public class Client {
    @Id
    @Column(name = "numtel", length = 20)
    private String numtel;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "sexe", length = 10)
    private String sexe;

    @Column(name = "pays", length = 50)
    private String pays;

    @Column(name = "solde")
    private int solde;

    @Column(name = "mail", length = 100)
    private String mail;

    @OneToMany(mappedBy = "envoyeur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Envoyer> envoisEnvoyes = new ArrayList<>();

    @OneToMany(mappedBy = "recepteur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Envoyer> envoisRecus = new ArrayList<>();

    // Constructeurs, getters et setters

    public Client() {
    }

    public Client(String numtel, String nom, String sexe, String pays, int solde, String mail) {
        this.numtel = numtel;
        this.nom = nom;
        this.sexe = sexe;
        this.pays = pays;
        this.solde = solde;
        this.mail = mail;
    }

    // Getters et Setters
    public String getNumtel() {
        return numtel;
    }

    public void setNumtel(String numtel) {
        this.numtel = numtel;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public int getSolde() {
        return solde;
    }

    public void setSolde(int solde) {
        this.solde = solde;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public List<Envoyer> getEnvoisEnvoyes() {
        return envoisEnvoyes;
    }

    public void setEnvoisEnvoyes(List<Envoyer> envoisEnvoyes) {
        this.envoisEnvoyes = envoisEnvoyes;
    }

    public List<Envoyer> getEnvoisRecus() {
        return envoisRecus;
    }

    public void setEnvoisRecus(List<Envoyer> envoisRecus) {
        this.envoisRecus = envoisRecus;
    }

    public String getDevise() {
        // Logique pour déterminer la devise en fonction du pays.
        // On suppose que "Madagascar" est le pays local pour l'Ariary (MGA),
        // tout autre pays implique un client international utilisant l'Euro (EUR).
        if (this.pays != null && this.pays.equalsIgnoreCase("Madagascar")) {
            return "MGA"; // Ariary malgache
        } else {
            return "EUR"; // Euro pour les clients internationaux ou si le pays n'est pas spécifié
        }
    }
}