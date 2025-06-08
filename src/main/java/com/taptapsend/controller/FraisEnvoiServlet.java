package com.taptapsend.controller;

import com.taptapsend.model.FraisEnvoi;
import com.taptapsend.service.FraisEnvoiService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import com.google.gson.Gson;

@WebServlet("/frais")
public class FraisEnvoiServlet extends BaseServlet {
    private final FraisEnvoiService fraisEnvoiService = new FraisEnvoiService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("edit".equals(action)) {
            String idfrais = request.getParameter("idfrais");
            FraisEnvoi frais = null;

            if (idfrais != null && !idfrais.trim().isEmpty()) {
                frais = fraisEnvoiService.getFraisEnvoi(idfrais);
                if (frais == null) {
                    System.out.println("DEBUG: Frais d'envoi avec ID " + idfrais + " introuvable. Préparation d'un formulaire vide.");
                    frais = new FraisEnvoi();
                }
            } else {
                // CORRECTION ICI : System.gout.println -> System.out.println
                System.out.println("DEBUG: Aucun ID de frais fourni. Préparation d'un formulaire vide pour un nouveau frais.");
                frais = new FraisEnvoi();
            }

            request.setAttribute("frais", frais);
            request.getRequestDispatcher("/WEB-INF/views/frais/form.jsp").forward(request, response);

        } else if ("delete".equals(action)) {
            String idfrais = request.getParameter("idfrais");
            fraisEnvoiService.deleteFraisEnvoi(idfrais);
            response.sendRedirect(request.getContextPath() + "/frais?success=Frais+supprimé+avec+succès");
        } else {
            List<FraisEnvoi> fraisList = fraisEnvoiService.getAllFraisEnvoi();
            request.setAttribute("fraisList", fraisList);
            forwardToTemplate("frais/list", "frais", "Frais d'Envoi", request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String idfrais = request.getParameter("idfrais");
        double montant1 = 0.0; // CHANGÉ: int -> double
        double montant2 = 0.0; // CHANGÉ: int -> double
        double fraisVal = 0.0; // CHANGÉ: int -> double

        try {
            montant1 = Double.parseDouble(request.getParameter("montant1")); // CHANGÉ: Integer.parseInt -> Double.parseDouble
            montant2 = Double.parseDouble(request.getParameter("montant2")); // CHANGÉ: Integer.parseInt -> Double.parseDouble
            fraisVal = Double.parseDouble(request.getParameter("frais")); // CHANGÉ: Integer.parseInt -> Double.parseDouble
        } catch (NumberFormatException e) {
            String jsonError = "{\"error\": \"Les montants et frais doivent être des nombres valides.\"}";
            response.getWriter().write(jsonError);
            return;
        }

        FraisEnvoi fraisEnvoi = new FraisEnvoi(idfrais, montant1, montant2, fraisVal);

        String successMessage = "";
        String errorMessage = "";

        if (fraisEnvoiService.getFraisEnvoi(idfrais) != null) {
            fraisEnvoiService.updateFraisEnvoi(fraisEnvoi);
            successMessage = "Frais mis à jour avec succès !";
        } else {
            try {
                fraisEnvoiService.createFraisEnvoi(fraisEnvoi);
                successMessage = "Frais créé avec succès !";
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = "Erreur lors de la création du frais : " + e.getMessage();
            }
        }

        if (!successMessage.isEmpty()) {
            String redirectUrl = request.getContextPath() + "/frais?success=" + successMessage.replace(" ", "+");
            response.getWriter().write(gson.toJson(new RedirectResponse(redirectUrl)));
        } else if (!errorMessage.isEmpty()) {
            response.getWriter().write(gson.toJson(new ErrorResponse(errorMessage)));
        }
    }

    private void forwardToTemplate(String contentJspPath, String pageName, String pageTitle,
                                   HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageName", pageName);
        request.setAttribute("pageTitle", pageTitle);
        request.setAttribute("contentPage", "/WEB-INF/views/" + contentJspPath + ".jsp");
        request.getRequestDispatcher("/WEB-INF/views/template.jsp").forward(request, response);
    }

    private static class RedirectResponse {
        public String redirect;
        public RedirectResponse(String redirect) {
            this.redirect = redirect;
        }
    }

    private static class ErrorResponse {
        public String error;
        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}