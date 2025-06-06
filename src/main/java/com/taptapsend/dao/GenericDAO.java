package com.taptapsend.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import java.util.List;
import com.taptapsend.util.HibernateUtil; // Assurez-vous que cette classe utilitaire est correcte

public abstract class GenericDAO<T> {
    private final Class<T> entityClass;

    protected GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected static EntityManager getEntityManager() {
        // Cette méthode crée une nouvelle instance d'EntityManager à chaque appel.
        // C'est typique pour les DAOs simples, mais pour des applications plus complexes,
        // la gestion de l'EntityManager (par exemple, via un gestionnaire de transactions ou CDI/Spring)
        // serait plus sophistiquée pour les sessions longues ou les transactions distribuées.
        return HibernateUtil.getEntityManagerFactory().createEntityManager();
    }

    public void create(T entity) {
        EntityManager em = getEntityManager();
        EntityTransaction et = null;
        try {
            et = em.getTransaction();
            et.begin();
            em.persist(entity);
            et.commit();
        } catch (Exception ex) {
            if (et != null && et.isActive()) {
                et.rollback();
            }
            // Loggez l'exception ou encapsulez-la dans une exception métier si nécessaire
            throw ex;
        } finally {
            if (em != null && em.isOpen()) { // S'assurer que l'EntityManager est fermé
                em.close();
            }
        }
    }

    public void update(T entity) {
        EntityManager em = getEntityManager();
        EntityTransaction et = null;
        try {
            et = em.getTransaction();
            et.begin();
            em.merge(entity);
            et.commit();
        } catch (Exception ex) {
            if (et != null && et.isActive()) {
                et.rollback();
            }
            // Loggez l'exception ou encapsulez-la
            throw ex;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public void delete(Object id) {
        EntityManager em = getEntityManager();
        EntityTransaction et = null;
        try {
            et = em.getTransaction();
            et.begin();
            T entity = em.find(entityClass, id); // Tente de trouver l'entité par son ID
            if (entity != null) {
                em.remove(entity); // Si trouvée, la supprime
            } else {
                // Optionnel : Loggez un avertissement si l'entité à supprimer n'est pas trouvée
                System.out.println("DEBUG: Entité de type " + entityClass.getSimpleName() + " avec ID " + id + " non trouvée pour suppression.");
            }
            et.commit();
        } catch (Exception ex) {
            if (et != null && et.isActive()) {
                et.rollback();
            }
            // Loggez l'exception ou encapsulez-la
            throw ex;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public T find(Object id) {
        EntityManager em = getEntityManager();
        try {
            // Pas besoin de clear ici, car on cherche une entité spécifique par ID.
            return em.find(entityClass, id);
        } catch (Exception ex) {
            // Optionnel : Loggez l'exception si find échoue (par exemple, ID invalide)
            // Mais l'erreur "id to load is required" est souvent lancée plus haut par Hibernate.
            throw ex; // Re-lance l'exception pour que le service puisse la gérer
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        EntityManager em = getEntityManager();
        try {
            // **MODIFICATION CLÉ : Effacer le cache de l'EntityManager**
            // Ceci garantit que la requête va chercher les données les plus récentes directement en base
            // et ne réutilise pas d'objets obsolètes potentiellement mis en cache par cette session.
            em.clear();

            Query query = em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e");
            return query.getResultList();
        } catch (Exception ex) {
            // Loggez l'exception si la récupération de la liste échoue
            throw ex; // Re-lance l'exception
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}