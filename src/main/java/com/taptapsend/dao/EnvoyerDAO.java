package com.taptapsend.dao;

import com.taptapsend.model.Envoyer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction; // Import nécessaire pour les transactions
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery; // Meilleure pratique pour les requêtes typées
import java.time.LocalDateTime;
import java.util.List;

// Assurez-vous que votre GenericDAO fournit getEntityManager(), findById, findAll, persist, merge, remove
// Si votre GenericDAO ne gère pas les transactions, alors les méthodes deleteEnvoi et updateEnvoi
// devront gérer leur propre EntityTransaction comme montré ci-dessous.
public class EnvoyerDAO extends GenericDAO<Envoyer> {

    public EnvoyerDAO() {
        super(Envoyer.class);
    }

    // --- Méthodes CRUD appelées par EnvoyerService ---
    // Elles délèguent à l'implémentation de GenericDAO pour la plupart des opérations.

    /**
     * Récupère un envoi par son identifiant unique.
     * @param id L'identifiant de l'envoi.
     * @return L'objet Envoyer correspondant, ou null si non trouvé.
     */
    // L'annotation @Override a été supprimée car la classe parente GenericDAO
    // ne semble pas déclarer de méthode findById(String id).
    // Si GenericDAO possède un findById(Object id), cette méthode dans EnvoyerDAO est une nouvelle méthode.
    public Envoyer findById(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Envoyer.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Récupère toutes les opérations d'envoi.
     * @return Une liste de toutes les opérations d'envoi.
     */
    // L'annotation @Override a été supprimée car la classe parente GenericDAO
    // ne semble pas déclarer de méthode findAll().
    public List<Envoyer> findAll() {
        EntityManager em = getEntityManager();
        try {
            // Ajout d'un ORDER BY DESC sur la date pour que les envois les plus récents apparaissent en premier
            return em.createQuery("SELECT e FROM Envoyer e ORDER BY e.date DESC", Envoyer.class).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Supprime une opération d'envoi par son identifiant.
     * Cette méthode gère sa propre transaction.
     * @param id L'identifiant de l'envoi à supprimer.
     * @throws RuntimeException En cas d'erreur lors de la suppression.
     */
    public void deleteEnvoi(String id) {
        EntityManager em = getEntityManager();
        EntityTransaction et = null;
        try {
            et = em.getTransaction();
            et.begin();
            Envoyer envoi = em.find(Envoyer.class, id); // Récupère l'entité gérée
            if (envoi != null) {
                em.remove(envoi); // Supprime l'entité gérée
            } else {
                throw new IllegalArgumentException("Envoi introuvable avec ID: " + id);
            }
            et.commit();
        } catch (Exception e) {
            if (et != null && et.isActive()) {
                et.rollback(); // Annule la transaction en cas d'erreur
            }
            throw new RuntimeException("Erreur lors de la suppression de l'envoi avec ID " + id, e); // Relance une RuntimeException
        } finally {
            em.close();
        }
    }

    /**
     * Met à jour une opération d'envoi existante.
     * Cette méthode gère sa propre transaction.
     * @param envoi L'objet Envoyer avec les données mises à jour.
     * @return L'entité Envoyer mise à jour et gérée.
     * @throws RuntimeException En cas d'erreur lors de la mise à jour.
     */
    public Envoyer updateEnvoi(Envoyer envoi) {
        EntityManager em = getEntityManager();
        EntityTransaction et = null;
        try {
            et = em.getTransaction();
            et.begin();
            Envoyer mergedEnvoi = em.merge(envoi); // Fusionne l'entité détachée (envoi) dans le contexte persistant
            et.commit();
            return mergedEnvoi; // Retourne l'entité gérée
        } catch (Exception e) {
            if (et != null && et.isActive()) {
                et.rollback(); // Annule la transaction en cas d'erreur
            }
            throw new RuntimeException("Erreur lors de la mise à jour de l'envoi avec ID " + envoi.getIdEnv(), e); // Relance une RuntimeException
        } finally {
            em.close();
        }
    }

    // --- Méthodes de Requête Spécifiques ---

    /**
     * Recherche les envois par date.
     * @param date La date à rechercher (seule la partie date est utilisée).
     * @return Une liste des envois effectués à cette date.
     */
    public List<Envoyer> findByDate(LocalDateTime date) {
        EntityManager em = getEntityManager();
        try {
            // Utilisation de FUNCTION('DATE', ...) pour comparer seulement la partie date
            // ORDER BY e.date DESC pour les résultats les plus récents en premier
            TypedQuery<Envoyer> query = em.createQuery(
                    "SELECT e FROM Envoyer e WHERE FUNCTION('DATE', e.date) = FUNCTION('DATE', :date) ORDER BY e.date DESC", Envoyer.class);
            query.setParameter("date", date);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Recherche les envois par numéro de téléphone de l'envoyeur, mois et année.
     * @param numtel Le numéro de téléphone de l'envoyeur.
     * @param month Le mois (1-12).
     * @param year L'année.
     * @return Une liste des envois correspondants.
     */
    public List<Envoyer> findByEnvoyeurAndMonth(String numtel, int month, int year) {
        EntityManager em = getEntityManager();
        try {
            // Utilisation de FUNCTION('MONTH', ...) et FUNCTION('YEAR', ...) pour filtrer par mois et année
            // ORDER BY e.date DESC pour les résultats les plus récents en premier
            TypedQuery<Envoyer> query = em.createQuery(
                    "SELECT e FROM Envoyer e WHERE e.envoyeur.numtel = :numtel " +
                            "AND FUNCTION('MONTH', e.date) = :month AND FUNCTION('YEAR', e.date) = :year ORDER BY e.date DESC", Envoyer.class);
            query.setParameter("numtel", numtel);
            query.setParameter("month", month);
            query.setParameter("year", year);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Calcule le total des frais d'envoi.
     * Cette requête est sur la table FraisEnvoi, pas Envoyer.
     * @return La somme totale des frais définis dans la table FraisEnvoi.
     */
    public double getTotalFrais() {
        EntityManager em = getEntityManager();
        try {
            // Requête pour sommer la colonne 'frais' de la table FraisEnvoi
            Query query = em.createQuery("SELECT SUM(f.frais) FROM FraisEnvoi f");
            Object result = query.getSingleResult();

            // Gère le cas où SUM() retourne null (par exemple, si la table est vide)
            if (result == null) {
                return 0.0;
            }

            // Convertit le résultat (qui peut être Long ou BigDecimal selon la DB et le type de colonne) en double
            return ((Number) result).doubleValue();
        } finally {
            em.close();
        }
    }
}