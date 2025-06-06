package com.taptapsend.dao;

import com.taptapsend.model.Envoyer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction; // Import nécessaire pour les transactions
import jakarta.persistence.NoResultException; // Ajout pour gérer le cas où SUM retourne aucun résultat
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery; // Meilleure pratique pour les requêtes typées
import java.time.LocalDateTime;
import java.util.List;

// Important : Ce DAO dépend fortement de l'implémentation de votre GenericDAO.
// Nous allons supposer que GenericDAO gère correctement la création et la fermeture de l'EntityManager,
// ainsi que la gestion des transactions pour les opérations persist, merge, remove.
// Si ce n'est pas le cas, vous devrez ajuster les méthodes de ce DAO en conséquence.

public class EnvoyerDAO extends GenericDAO<Envoyer> {

    // Constructeur : Appelle le constructeur de la classe parente (GenericDAO)
    public EnvoyerDAO() {
        super(Envoyer.class);
    }

    // --- Méthodes de Recherche Spécifiques (hors CRUD de base) ---

    /**
     * Récupère un service (envoi) par son identifiant unique.
     * Cette méthode est statique et pourrait être une méthode utilitaire si elle n'interagit pas avec l'état de l'instance DAO.
     * Cependant, il est plus idiomatique de la rendre non statique pour suivre le pattern DAO.
     * Pour l'instant, nous la gardons statique comme dans votre code original, mais soyez conscient de cette nuance.
     *
     * @param idEnv L'identifiant de l'envoi.
     * @return L'objet Envoyer correspondant, ou null si non trouvé.
     */
    public static Envoyer getServiceId(String idEnv) {
        // Dans une DAO, il est préférable d'obtenir l'EntityManager via une méthode non statique ou un gestionnaire.
        // Si getEntityManager() est statique dans GenericDAO, cela fonctionne.
        // Sinon, il faudrait passer par une instance de EnvoyerDAO.
        EntityManager em = getEntityManager(); // Supposons que getEntityManager() est statique ou accessible
        try {
            return em.find(Envoyer.class, idEnv);
        } finally {
            em.close();
        }
    }

    /**
     * Récupère un envoi par son identifiant unique.
     * Cette méthode redéfinit celle potentiellement héritée de GenericDAO si le type d'ID est Object.
     * Si GenericDAO a déjà un `findById(String id)`, cette méthode est redondante ou doit être annotée `@Override`.
     * Étant donné le commentaire original, nous conservons cette version distincte si `GenericDAO` utilise `Object id`.
     *
     * @param id L'identifiant de l'envoi.
     * @return L'objet Envoyer correspondant, ou null si non trouvé.
     */
    // Si GenericDAO implémente findById(ID id), où ID est le type de la clé primaire (ici String),
    // alors vous pouvez potentiellement supprimer cette méthode et utiliser celle du parent.
    // Pour l'instant, nous la gardons, car votre GenericDAO n'a peut-être pas une signature exacte.
    public Envoyer findById(String id) {
        // Appelons directement la méthode findById du GenericDAO si elle existe et gère la fermeture de l'EM.
        // Sinon, le code actuel est correct pour gérer l'EM ici.
        EntityManager em = getEntityManager();
        try {
            return em.find(Envoyer.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Récupère toutes les opérations d'envoi, triées par date décroissante.
     * @return Une liste de toutes les opérations d'envoi.
     */
    @Override // Cette annotation est ajoutée car il est courant que findAll soit une méthode de GenericDAO
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
     * Cette méthode gère sa propre transaction si GenericDAO ne le fait pas pour `remove`.
     * Il est préférable de déléguer à `GenericDAO.remove(entity)`.
     *
     * @param id L'identifiant de l'envoi à supprimer.
     * @throws IllegalArgumentException Si l'envoi n'est pas trouvé.
     * @throws RuntimeException En cas d'erreur lors de la suppression (propagation de l'exception).
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
                // Il est souvent préférable de ne pas lancer d'exception si l'ID n'existe pas pour une suppression,
                // car l'état souhaité (entité supprimée) est déjà atteint.
                // Cependant, si la logique métier exige une notification, IllegalArgumentException est appropriée.
                throw new IllegalArgumentException("Envoi introuvable avec ID: " + id + ". Impossible de supprimer.");
            }
            et.commit();
        } catch (IllegalArgumentException e) {
            // Re-lancer directement car c'est une erreur métier spécifique.
            if (et != null && et.isActive()) {
                et.rollback();
            }
            throw e;
        } catch (Exception e) {
            if (et != null && et.isActive()) {
                et.rollback(); // Annule la transaction en cas d'erreur
            }
            // Loggez l'erreur avant de la relancer pour faciliter le débogage.
            System.err.println("Erreur lors de la suppression de l'envoi avec ID " + id + ": " + e.getMessage());
            throw new RuntimeException("Erreur lors de la suppression de l'envoi avec ID " + id, e);
        } finally {
            em.close();
        }
    }

    /**
     * Met à jour une opération d'envoi existante.
     * Cette méthode gère sa propre transaction si GenericDAO ne le fait pas pour `merge`.
     * Il est préférable de déléguer à `GenericDAO.merge(entity)`.
     *
     * @param envoi L'objet Envoyer avec les données mises à jour.
     * @return L'entité Envoyer mise à jour et gérée.
     * @throws RuntimeException En cas d'erreur lors de la mise à jour (propagation de l'exception).
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
            // Loggez l'erreur avant de la relancer.
            System.err.println("Erreur lors de la mise à jour de l'envoi avec ID " + envoi.getIdEnv() + ": " + e.getMessage());
            throw new RuntimeException("Erreur lors de la mise à jour de l'envoi avec ID " + envoi.getIdEnv(), e);
        } finally {
            em.close();
        }
    }

    // --- Méthodes de Requête Spécifiques ---

    /**
     * Recherche les envois par date (jour, mois, année).
     * @param date La date à rechercher (seule la partie date est utilisée).
     * @return Une liste des envois effectués à cette date, triés par date décroissante.
     */
    public List<Envoyer> findByDate(LocalDateTime date) {
        EntityManager em = getEntityManager();
        try {
            // Utilisation de FUNCTION('DATE', ...) pour comparer seulement la partie date
            // Note: Si vous rencontrez une erreur similaire à 'MONTH'/'YEAR' avec 'DATE',
            // il faudrait envisager d'utiliser des comparaisons de dates sur les bornes (début/fin du jour).
            TypedQuery<Envoyer> query = em.createQuery(
                    "SELECT e FROM Envoyer e WHERE FUNCTION('DATE', e.date) = FUNCTION('DATE', :date) ORDER BY e.date DESC", Envoyer.class);
            query.setParameter("date", date); // JPA gérera la conversion de LocalDateTime en date si nécessaire
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
     * @return Une liste des envois correspondants, triés par date décroissante.
     */
    public List<Envoyer> findByEnvoyeurAndMonth(String numtel, int month, int year) {
        EntityManager em = getEntityManager();
        try {
            // *** CORRECTION APPLIQUÉE ICI : Utilisation de EXTRACT pour MONTH et YEAR ***
            TypedQuery<Envoyer> query = em.createQuery(
                    "SELECT e FROM Envoyer e WHERE e.envoyeur.numtel = :numtel " +
                            "AND EXTRACT(MONTH FROM e.date) = :month AND EXTRACT(YEAR FROM e.date) = :year ORDER BY e.date DESC", Envoyer.class);
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
     * Cette requête est sur la table FraisEnvoi.
     * @return La somme totale des frais définis dans la table FraisEnvoi. Retourne 0.0 si aucun frais n'est défini ou si la table est vide.
     */
    public double getTotalFrais() {
        EntityManager em = getEntityManager();
        try {
            // Requête pour sommer la colonne 'frais' de la table FraisEnvoi
            // Assurez-vous que l'entité FraisEnvoi est correctement mappée dans votre modèle JPA.
            Query query = em.createQuery("SELECT SUM(f.frais) FROM FraisEnvoi f");
            Object result = null;
            try {
                result = query.getSingleResult();
            } catch (NoResultException e) {
                // Ceci peut arriver si la table FraisEnvoi est vide et SUM() retourne aucun résultat au lieu de null.
                // Dans ce cas, la somme est 0.
                return 0.0;
            }

            // Gère le cas où SUM() retourne null (par exemple, si la table est vide et le SGBD retourne null pour SUM(vide))
            if (result == null) {
                return 0.0;
            }

            // Convertit le résultat (qui peut être Long ou BigDecimal selon la DB et le type de colonne) en double
            return ((Number) result).doubleValue();
        } catch (Exception e) {
            // Loggez l'erreur avant de la relancer, par exemple si la requête est mal formée ou si FraisEnvoi n'existe pas.
            System.err.println("Erreur lors du calcul du total des frais : " + e.getMessage());
            throw new RuntimeException("Erreur lors du calcul du total des frais.", e);
        } finally {
            em.close();
        }
    }
}