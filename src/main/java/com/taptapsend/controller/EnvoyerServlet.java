package com.taptapsend.controller;

import com.taptapsend.model.Client;
import com.taptapsend.model.Envoyer;
import com.taptapsend.service.ClientService;
import com.taptapsend.service.EnvoyerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletOutputStream; // Keep this import for PDF generation
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID; // For UUID generation
import com.google.gson.Gson;

@WebServlet("/envoyer")
public class EnvoyerServlet extends BaseServlet { // Assuming BaseServlet provides common utilities
    private final EnvoyerService envoyerService = new EnvoyerService();
    private final ClientService clientService = new ClientService();
    private final Gson gson = new Gson(); // Initialisation de Gson

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("add".equals(action)) { // Charger le formulaire d'ajout
            List<Client> clients = clientService.getAllClients();
            request.setAttribute("clients", clients);
            request.getRequestDispatcher("/WEB-INF/views/envoyer/form.jsp").forward(request, response);
        } else if ("edit".equals(action)) { // Charger le formulaire pour l'édition
            String idEnv = request.getParameter("id");
            if (idEnv != null && !idEnv.trim().isEmpty()) {
                try {
                    Envoyer envoi = envoyerService.getEnvoiById(idEnv);
                    if (envoi != null) {
                        List<Client> clients = clientService.getAllClients(); // Pour les listes déroulantes
                        request.setAttribute("oldEnvoi", envoi); // Envoyer l'objet à éditer au JSP
                        request.setAttribute("clients", clients);
                        request.getRequestDispatcher("/WEB-INF/views/envoyer/form.jsp").forward(request, response);
                    } else {
                        response.sendRedirect(request.getContextPath() + "/envoyer?error=Envoi non trouvé pour l'édition.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    response.sendRedirect(request.getContextPath() + "/envoyer?error=Erreur lors du chargement de l'envoi pour édition.");
                }
            } else {
                response.sendRedirect(request.getContextPath() + "/envoyer?error=ID d'envoi manquant pour l'édition.");
            }
        } else if ("search".equals(action)) {
            String dateStr = request.getParameter("date");
            LocalDateTime date = null;
            if (dateStr != null && !dateStr.trim().isEmpty()) {
                date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE).atStartOfDay();
            }
            List<Envoyer> envois;
            if (date != null) {
                envois = envoyerService.getEnvoisByDate(date);
            } else {
                envois = envoyerService.getAllEnvois(); // Fallback si la date est nulle
            }
            request.setAttribute("envois", envois);
            forwardToTemplate("envoyer/list", "envoyer", "Liste des Envois", request, response);
        } else if ("releve".equals(action)) {
            String numtel = request.getParameter("numtel");
            int month = Integer.parseInt(request.getParameter("month"));
            int year = Integer.parseInt(request.getParameter("year"));

            List<Envoyer> envois = envoyerService.getEnvoisByEnvoyeurAndMonth(numtel, month, year);
            Client client = clientService.getClient(numtel);

            request.setAttribute("client", client);
            request.setAttribute("envois", envois);
            request.setAttribute("month", month);
            request.setAttribute("year", year);

            forwardToTemplate("envoyer/releve", "envoyer", "Relevé d'Envois", request, response);
        } else if ("pdf".equals(action)) {
            generatePdf(request, response);
        } else { // Action par défaut : afficher la liste de tous les envois
            List<Envoyer> envois = envoyerService.getAllEnvois();
            request.setAttribute("envois", envois);
            forwardToTemplate("envoyer/list", "envoyer", "Liste des Envois", request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action"); // Récupérer l'action du formulaire

        if ("delete".equals(action)) { // Logique de suppression
            handleDelete(request, response);
        } else if ("update".equals(action)) { // Logique de mise à jour (pour l'édition)
            handleUpdate(request, response);
        }
        else { // Logique de création (action par défaut si aucune action spécifique n'est définie)
            handleCreate(request, response);
        }
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idEnv = generateId(); // Génère un ID unique pour le nouvel envoi
        String numEnvoyeur = request.getParameter("numEnvoyeur");
        String numRecepteur = request.getParameter("numRecepteur");
        String raison = request.getParameter("raison");

        int montant = 0;
        try {
            montant = Integer.parseInt(request.getParameter("montant"));
        } catch (NumberFormatException e) {
            response.getWriter().write(gson.toJson(new ErrorResponse("Le montant doit être un nombre valide.")));
            return;
        }

        Client envoyeur = clientService.getClient(numEnvoyeur);
        Client recepteur = clientService.getClient(numRecepteur);

        // Validations
        if (envoyeur == null) {
            response.getWriter().write(gson.toJson(new ErrorResponse("Expéditeur non trouvé.")));
            return;
        }
        if (recepteur == null) {
            response.getWriter().write(gson.toJson(new ErrorResponse("Destinataire non trouvé.")));
            return;
        }
        if (envoyeur.getNumtel().equals(recepteur.getNumtel())) {
            response.getWriter().write(gson.toJson(new ErrorResponse("L'expéditeur et le destinataire ne peuvent pas être les mêmes.")));
            return;
        }
        // Autres validations comme le solde suffisant seront gérées dans le service.

        Envoyer envoi = new Envoyer(idEnv, envoyeur, recepteur, montant, LocalDateTime.now(), raison);

        try {
            envoyerService.createEnvoi(envoi);
            String successMessage = "Transfert effectué avec succès !";
            String redirectUrl = request.getContextPath() + "/envoyer?success=" + successMessage.replace(" ", "+");
            response.getWriter().write(gson.toJson(new RedirectResponse(redirectUrl)));
        } catch (IllegalArgumentException e) {
            response.getWriter().write(gson.toJson(new ErrorResponse(e.getMessage())));
        } catch (IllegalStateException e) {
            response.getWriter().write(gson.toJson(new ErrorResponse(e.getMessage() + " Veuillez contacter l'administrateur.")));
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write(gson.toJson(new ErrorResponse("Erreur lors de l'enregistrement du transfert : " + e.getMessage())));
        }
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idEnv = request.getParameter("idEnv"); // Récupérer l'ID de l'envoi à modifier
        String numEnvoyeur = request.getParameter("numEnvoyeur");
        String numRecepteur = request.getParameter("numRecepteur");
        String raison = request.getParameter("raison");

        if (idEnv == null || idEnv.trim().isEmpty()) {
            response.getWriter().write(gson.toJson(new ErrorResponse("ID d'envoi manquant pour la mise à jour.")));
            return;
        }

        int montant = 0;
        try {
            montant = Integer.parseInt(request.getParameter("montant"));
        } catch (NumberFormatException e) {
            response.getWriter().write(gson.toJson(new ErrorResponse("Le montant doit être un nombre valide.")));
            return;
        }

        Client envoyeur = clientService.getClient(numEnvoyeur);
        Client recepteur = clientService.getClient(numRecepteur);

        // Validations
        if (envoyeur == null) {
            response.getWriter().write(gson.toJson(new ErrorResponse("Expéditeur non trouvé.")));
            return;
        }
        if (recepteur == null) {
            response.getWriter().write(gson.toJson(new ErrorResponse("Destinataire non trouvé.")));
            return;
        }
        if (envoyeur.getNumtel().equals(recepteur.getNumtel())) {
            response.getWriter().write(gson.toJson(new ErrorResponse("L'expéditeur et le destinataire ne peuvent pas être les mêmes.")));
            return;
        }

        // Créez un objet Envoyer avec les données mises à jour
        // La date pourrait être la date originale ou la date de modification, selon votre logique métier.
        // Ici, je suppose que la date n'est pas modifiée lors d'une simple édition du montant/raison.
        // Si vous voulez une nouvelle date de modification, utilisez LocalDateTime.now().
        Envoyer existingEnvoi = envoyerService.getEnvoiById(idEnv); // Récupérer l'original pour conserver la date si non changée
        if (existingEnvoi == null) {
            response.getWriter().write(gson.toJson(new ErrorResponse("Envoi introuvable pour la mise à jour.")));
            return;
        }

        Envoyer updatedEnvoi = new Envoyer(idEnv, envoyeur, recepteur, montant, existingEnvoi.getDate(), raison);


        try {
            envoyerService.updateEnvoi(updatedEnvoi);
            String successMessage = "Envoi mis à jour avec succès !";
            String redirectUrl = request.getContextPath() + "/envoyer?success=" + successMessage.replace(" ", "+");
            response.getWriter().write(gson.toJson(new RedirectResponse(redirectUrl)));
        } catch (IllegalArgumentException e) {
            response.getWriter().write(gson.toJson(new ErrorResponse(e.getMessage())));
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write(gson.toJson(new ErrorResponse("Erreur lors de la mise à jour de l'envoi : " + e.getMessage())));
        }
    }


    private void handleDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idEnv = request.getParameter("id"); // L'ID vient du formulaire de suppression

        if (idEnv == null || idEnv.trim().isEmpty()) {
            response.getWriter().write(gson.toJson(new ErrorResponse("ID d'envoi manquant pour la suppression.")));
            return;
        }

        try {
            envoyerService.deleteEnvoi(idEnv);
            String successMessage = "Envoi supprimé avec succès !";
            // Redirection vers la liste après suppression
            String redirectUrl = request.getContextPath() + "/envoyer?success=" + successMessage.replace(" ", "+");
            response.getWriter().write(gson.toJson(new RedirectResponse(redirectUrl)));
        } catch (IllegalArgumentException e) {
            response.getWriter().write(gson.toJson(new ErrorResponse(e.getMessage())));
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write(gson.toJson(new ErrorResponse("Erreur lors de la suppression de l'envoi : " + e.getMessage())));
        }
    }


    private String generateId() {
        return "ENV" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void generatePdf(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String numtel = request.getParameter("numtel");
        // Récupérer l'ID de l'envoi si besoin pour le PDF, actuellement c'est par client et mois/année
        // String idEnv = request.getParameter("idEnv");

        int month = 0;
        int year = 0;
        try {
            month = Integer.parseInt(request.getParameter("month"));
            year = Integer.parseInt(request.getParameter("year"));
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/envoyer?error=Mois ou année invalide pour le PDF.");
            return;
        }

        try {
            Client client = clientService.getClient(numtel);
            List<Envoyer> envois = envoyerService.getEnvoisByEnvoyeurAndMonth(numtel, month, year);

            if (client == null) {
                throw new Exception("Client introuvable pour la génération du PDF.");
            }
            if (envois == null || envois.isEmpty()) {
                // Ne pas lancer d'exception si aucun envoi, mais un PDF avec un message "aucun envoi"
                // Ou rediriger avec un message si c'est plus approprié pour l'UX
                response.sendRedirect(request.getContextPath() +
                        "/envoyer?error=Aucun+envoi+trouvé+pour+ce+client+et+cette+période+pour+le+PDF.");
                return; // Stop processing if no envois
            }

            byte[] pdfBytes = envoyerService.generatePdfReleve(client, envois, month, year);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=releve_" + numtel + "_" + month + "_" + year + ".pdf");
            response.setContentLength(pdfBytes.length);

            ServletOutputStream out = response.getOutputStream();
            out.write(pdfBytes);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() +
                    "/envoyer?error=Erreur+de+génération+du+PDF:+" + e.getMessage().replace(" ", "+"));
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