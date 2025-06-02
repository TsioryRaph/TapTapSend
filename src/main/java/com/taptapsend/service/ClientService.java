package com.taptapsend.service;

import com.taptapsend.dao.ClientDAO;
import com.taptapsend.model.Client;
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
}