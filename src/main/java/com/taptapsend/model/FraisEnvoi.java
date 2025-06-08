package com.taptapsend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "frais_envoi")
public class FraisEnvoi {
    @Id
    @Column(name = "idfrais", length = 20)
    private String idfrais;

    @Column(name = "montant1")
    private double montant1; // CHANGÉ: int -> double

    @Column(name = "montant2")
    private double montant2; // CHANGÉ: int -> double

    @Column(name = "frais")
    private double frais; // CHANGÉ: int -> double

    // Constructeurs
    public FraisEnvoi() {
    }

    public FraisEnvoi(String idfrais, double montant1, double montant2, double frais) { // CHANGÉ: int -> double
        this.idfrais = idfrais;
        this.montant1 = montant1;
        this.montant2 = montant2;
        this.frais = frais;
    }

    // Getters et Setters
    public String getIdfrais() {
        return idfrais;
    }

    public void setIdfrais(String idfrais) {
        this.idfrais = idfrais;
    }

    public double getMontant1() { // CHANGÉ: int -> double
        return montant1;
    }

    public void setMontant1(double montant1) { // CHANGÉ: int -> double
        this.montant1 = montant1;
    }

    public double getMontant2() { // CHANGÉ: int -> double
        return montant2;
    }

    public void setMontant2(double montant2) { // CHANGÉ: int -> double
        this.montant2 = montant2;
    }

    public double getFrais() { // CHANGÉ: int -> double
        return frais;
    }

    public void setFrais(double frais) { // CHANGÉ: int -> double
        this.frais = frais;
    }
}