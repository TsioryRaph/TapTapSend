package com.taptapsend.service;

import com.taptapsend.dao.FraisEnvoiDAO;
import com.taptapsend.model.FraisEnvoi;
import java.util.List;

public class FraisEnvoiService {
    private final FraisEnvoiDAO fraisEnvoiDAO;

    public FraisEnvoiService() {
        this.fraisEnvoiDAO = new FraisEnvoiDAO();
    }

    public void createFraisEnvoi(FraisEnvoi fraisEnvoi) {
        fraisEnvoiDAO.create(fraisEnvoi);
    }

    public void updateFraisEnvoi(FraisEnvoi fraisEnvoi) {
        fraisEnvoiDAO.update(fraisEnvoi);
    }

    public void deleteFraisEnvoi(String idfrais) {
        fraisEnvoiDAO.delete(idfrais);
    }

    public FraisEnvoi getFraisEnvoi(String idfrais) {
        return fraisEnvoiDAO.find(idfrais);
    }

    public List<FraisEnvoi> getAllFraisEnvoi() {
        return fraisEnvoiDAO.findAll();
    }
}