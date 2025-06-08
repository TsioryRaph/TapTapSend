package com.taptapsend.dao;

import com.taptapsend.model.Client;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;

public class ClientDAO extends GenericDAO<Client> {
    public ClientDAO() {
        super(Client.class);
    }

    public List<Client> searchClients(String searchTerm) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createQuery(
                    "SELECT c FROM Client c WHERE c.nom LIKE :searchTerm OR c.numtel LIKE :searchTerm OR c.mail LIKE :searchTerm");
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Client> findClientsWithTransactions() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT e.envoyeur FROM Envoyer e", Client.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}