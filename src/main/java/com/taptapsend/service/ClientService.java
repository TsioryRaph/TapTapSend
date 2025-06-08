package com.taptapsend.service;

import com.taptapsend.dao.ClientDAO;
import com.taptapsend.model.Client;
import com.taptapsend.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class ClientService {
    private final ClientDAO clientDAO;

    public ClientService() {
        this.clientDAO = new ClientDAO();
    }

    public void createClient(Client client) {
        clientDAO.create(client);
    }

    public void updateClient(Client client) {
        clientDAO.update(client);
    }

    public void deleteClient(String numtel) {
        clientDAO.delete(numtel);
    }

    public Client getClient(String numtel) {
        return clientDAO.find(numtel);
    }

    public List<Client> getAllClients() {
        return clientDAO.findAll();
    }

    public List<Client> searchClients(String searchTerm) {
        return clientDAO.searchClients(searchTerm);
    }

    public List<Client> getClientsWithTransactions() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            // Requête pour récupérer les clients distincts qui apparaissent comme envoyeurs dans la table Envoyer
            TypedQuery<Client> query = em.createQuery(
                    "SELECT DISTINCT e.envoyeur FROM Envoyer e", Client.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}