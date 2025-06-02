package com.taptapsend.service;

import com.taptapsend.dao.TauxDAO;
import com.taptapsend.model.Taux;
import java.util.List;

public class TauxService {
    private final TauxDAO tauxDAO;

    public TauxService() {
        this.tauxDAO = new TauxDAO();
    }

    public void createTaux(Taux taux) {
        tauxDAO.create(taux);
    }

    public void updateTaux(Taux taux) {
        tauxDAO.update(taux);
    }

    public void deleteTaux(String idtaux) {
        tauxDAO.delete(idtaux);
    }

    public Taux getTaux(String idtaux) {
        return tauxDAO.find(idtaux);
    }

    public List<Taux> getAllTaux() {
        return tauxDAO.findAll();
    }
}