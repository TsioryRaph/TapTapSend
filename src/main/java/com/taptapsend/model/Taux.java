package com.taptapsend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "taux")
public class Taux {
    @Id
    @Column(name = "idtaux", length = 20)
    private String idtaux;

    @Column(name = "montant1")
    private int montant1;

    @Column(name = "montant2")
    private int montant2;

    // Constructeurs, getters et setters

    public Taux() {
    }

    public Taux(String idtaux, int montant1, int montant2) {
        this.idtaux = idtaux;
        this.montant1 = montant1;
        this.montant2 = montant2;
    }

    public String getIdtaux() {
        return idtaux;
    }

    public void setIdtaux(String idtaux) {
        this.idtaux = idtaux;
    }

    public int getMontant1() {
        return montant1;
    }

    public void setMontant1(int montant1) {
        this.montant1 = montant1;
    }

    public int getMontant2() {
        return montant2;
    }

    public void setMontant2(int montant2) {
        this.montant2 = montant2;
    }
}