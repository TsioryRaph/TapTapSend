package com.taptapsend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;   // <--- Assurez-vous que cet import est là
import java.util.Date;     // <--- Assurez-vous que cet import est là

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

    @Column(name = "date")
    private LocalDateTime date; // CE CHAMP RESTE LocalDateTime

    @Column(name = "raison", length = 255)
    private String raison;

    // Constructeurs
    public Envoyer() {
    }

    public Envoyer(String idEnv, Client envoyeur, Client recepteur, int montant, LocalDateTime date, String raison) {
        this.idEnv = idEnv;
        this.envoyeur = envoyeur;
        this.recepteur = recepteur;
        this.montant = montant;
        this.date = date;
        this.raison = raison;
    }

    // Getters et Setters existants
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

    // NOUVELLE MÉTHODE DE COMPATIBILITÉ POUR LE JSP
    /**
     * Retourne la date de l'envoi sous forme de java.util.Date pour la compatibilité avec JSTL fmt:formatDate.
     * Cette méthode est utile pour l'affichage dans les JSP sans modifier le type LocalDateTime du modèle.
     * @return La date de l'envoi convertie en java.util.Date, ou null si la date originale est null.
     */
    public Date getDisplayDate() {
        if (this.date == null) {
            return null;
        }
        // Convertit LocalDateTime (sans fuseau horaire) en un Instant (point dans le temps en UTC)
        // en utilisant le fuseau horaire par défaut du système pour la conversion.
        return Date.from(this.date.atZone(ZoneId.systemDefault()).toInstant());
    }
}