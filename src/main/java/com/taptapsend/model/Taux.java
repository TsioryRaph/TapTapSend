package com.taptapsend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "taux")
public class Taux {
    @Id
    @Column(name = "idtaux", length = 20)
    private String idtaux;

    @Column(name = "montant1")
    private double montant1; // CHANGÉ: int -> double

    @Column(name = "montant2")
    private double montant2; // CHANGÉ: int -> double

    // Constructeurs
    public Taux() {
    }

    public Taux(String idtaux, double montant1, double montant2) { // CHANGÉ: int -> double
        this.idtaux = idtaux;
        this.montant1 = montant1;
        this.montant2 = montant2;
    }

    // Getters et Setters
    public String getIdtaux() {
        return idtaux;
    }

    public void setIdtaux(String idtaux) {
        this.idtaux = idtaux;
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
}