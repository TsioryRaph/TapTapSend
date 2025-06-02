package com.taptapsend.controller;

import com.taptapsend.model.Taux;
import com.taptapsend.service.TauxService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List; // Importez List si vous l'utilisez pour getAllTaux ou autre
import com.google.gson.Gson; // Importez Gson

@WebServlet("/taux")
public class TauxServlet extends BaseServlet {
    private final TauxService tauxService = new TauxService();
    private final Gson gson = new Gson(); // Initialisation de Gson

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("edit".equals(action)) { // Cette branche gère l'ajout ET la modification
            String idtaux = request.getParameter("idtaux");
            Taux taux = null; // Initialisez le taux à null

            // Si un ID de taux est fourni (pour la modification)
            if (idtaux != null && !idtaux.trim().isEmpty()) {
                taux = tauxService.getTaux(idtaux);
                // Si le taux n'est pas trouvé, on crée un objet vide pour l'ajout (ou formulaire vide)
                if (taux == null) {
                    System.out.println("DEBUG: Taux avec ID " + idtaux + " introuvable. Préparation d'un formulaire vide.");
                    taux = new Taux();
                }
            } else {
                // Si aucun ID de taux n'est fourni (c'est le cas pour l'ajout)
                System.out.println("DEBUG: Aucun ID de taux fourni. Préparation d'un formulaire vide pour un nouveau taux.");
                taux = new Taux();
            }

            request.setAttribute("taux", taux); // Le taux (existant ou vide) est mis en attribut
            // IMPORTANT : Forward direct vers le JSP du formulaire pour la modale
            request.getRequestDispatcher("/WEB-INF/views/taux/form.jsp").forward(request, response);
            // Pas besoin de 'return' ici, forward() termine déjà le traitement

        } else if ("delete".equals(action)) {
            String idtaux = request.getParameter("idtaux");
            tauxService.deleteTaux(idtaux);
            // La suppression n'est généralement pas faite via AJAX pour la confirmation,
            // donc une redirection normale est acceptable ici.
            response.sendRedirect(request.getContextPath() + "/taux?success=Taux+supprimé+avec+succès");
        } else {
            // Le cas par défaut : affiche la liste des taux
            List<Taux> tauxList = tauxService.getAllTaux(); // Récupère la liste
            request.setAttribute("tauxList", tauxList); // Met la liste en attribut
            // Utilise forwardToTemplate pour inclure la liste dans le layout principal
            forwardToTemplate("taux/list", "taux", "Gestion des Taux", request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // IMPORTANT : Définir le type de contenu de la réponse pour le JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String idtaux = request.getParameter("idtaux");
        int montant1 = 0;
        int montant2 = 0;

        try {
            montant1 = Integer.parseInt(request.getParameter("montant1"));
            montant2 = Integer.parseInt(request.getParameter("montant2"));
        } catch (NumberFormatException e) {
            String jsonError = "{\"error\": \"Les montants doivent être des nombres valides.\"}";
            response.getWriter().write(jsonError);
            return; // Arrêter l'exécution
        }

        Taux taux = new Taux(idtaux, montant1, montant2);

        String successMessage = "";
        String errorMessage = "";

        // Vérifie si le taux existe déjà pour décider de créer ou mettre à jour
        if (tauxService.getTaux(idtaux) != null) {
            tauxService.updateTaux(taux);
            successMessage = "Taux mis à jour avec succès !";
        } else {
            try {
                tauxService.createTaux(taux);
                successMessage = "Taux créé avec succès !";
            } catch (Exception e) {
                e.printStackTrace(); // Log l'exception pour le débogage
                errorMessage = "Erreur lors de la création du taux : " + e.getMessage();
                // Ajoutez ici une logique pour les erreurs de doublon si l'ID est généré côté client
            }
        }

        // Préparer la réponse JSON pour le JavaScript dans le template
        if (!successMessage.isEmpty()) {
            String redirectUrl = request.getContextPath() + "/taux?success=" + successMessage.replace(" ", "+");
            response.getWriter().write(gson.toJson(new RedirectResponse(redirectUrl)));
        } else if (!errorMessage.isEmpty()) {
            response.getWriter().write(gson.toJson(new ErrorResponse(errorMessage)));
        }
    }

    /**
     * Méthode utilitaire pour forwarder vers le template principal.
     * Cette méthode devrait idéalement être dans votre BaseServlet pour réutilisation.
     */
    private void forwardToTemplate(String contentJspPath, String pageName, String pageTitle,
                                   HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageName", pageName);
        request.setAttribute("pageTitle", pageTitle);
        request.setAttribute("contentPage", "/WEB-INF/views/" + contentJspPath + ".jsp");
        request.getRequestDispatcher("/WEB-INF/views/template.jsp").forward(request, response);
    }

    // Classes internes pour les réponses JSON (peuvent être des classes séparées si vous préférez)
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