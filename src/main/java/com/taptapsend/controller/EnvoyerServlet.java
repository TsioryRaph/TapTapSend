package com.taptapsend.controller;

import com.taptapsend.model.Client;
import com.taptapsend.model.Envoyer;
import com.taptapsend.service.ClientService;
import com.taptapsend.service.EnvoyerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletOutputStream; // Nécessaire pour écrire le PDF directement dans la réponse
import java.io.IOException;
import java.io.PrintWriter; // Nécessaire pour envoyer des réponses JSON
import java.net.URLEncoder; // Pour encoder les URLs en toute sécurité
import java.nio.charset.StandardCharsets; // Pour spécifier l'encodage des caractères
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID; // Pour générer des IDs uniques
import com.google.gson.Gson; // Pour la sérialisation/désérialisation JSON
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/envoyer")
public class EnvoyerServlet extends BaseServlet {

    private static final Logger logger = LoggerFactory.getLogger(EnvoyerServlet.class);

    // Initialisation des services pour interagir avec les données
    private final EnvoyerService envoyerService = new EnvoyerService();
    private final ClientService clientService = new ClientService();
    private final Gson gson = new Gson(); // Instance de Gson pour les réponses JSON

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        // Log l'action reçue pour le débogage
        logger.info("Requête GET reçue, action: {}", action != null ? action : "aucune");

        try {
            if ("add".equals(action)) {
                // Affiche le formulaire d'ajout d'un envoi
                // Récupère tous les clients pour les listes déroulantes (envoyeur/recepteur)
                List<Client> clients = clientService.getAllClients();
                request.setAttribute("clients", clients);
                // Forward vers le JSP du formulaire
                request.getRequestDispatcher("/WEB-INF/views/envoyer/form.jsp").forward(request, response);
            } else if ("edit".equals(action)) {
                // Affiche le formulaire de modification d'un envoi existant
                String idEnv = request.getParameter("id");
                if (idEnv != null && !idEnv.trim().isEmpty()) {
                    Envoyer envoi = envoyerService.getEnvoiById(idEnv);
                    if (envoi != null) {
                        List<Client> clients = clientService.getAllClients();
                        request.setAttribute("oldEnvoi", envoi); // "oldEnvoi" pour pré-remplir le formulaire
                        request.setAttribute("clients", clients);
                        request.getRequestDispatcher("/WEB-INF/views/envoyer/form.jsp").forward(request, response);
                    } else {
                        // Si l'envoi n'est pas trouvé, renvoie une erreur JSON (pour les requêtes AJAX)
                        logger.warn("Envoi non trouvé pour l'édition avec ID: {}", idEnv);
                        sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, new ErrorResponse("Envoi non trouvé pour l'édition."));
                    }
                } else {
                    // Si l'ID est manquant, renvoie une erreur JSON
                    logger.warn("ID d'envoi manquant pour l'édition.");
                    sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorResponse("ID d'envoi manquant pour l'édition."));
                }
            } else if ("search".equals(action)) {
                // Recherche des envois par date (ou affiche tous si aucune date n'est fournie)
                String dateStr = request.getParameter("date");
                LocalDateTime date = null;
                if (dateStr != null && !dateStr.trim().isEmpty()) {
                    try {
                        // Parse la date du formulaire (format YYYY-MM-DD)
                        date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE).atStartOfDay();
                    } catch (DateTimeParseException e) {
                        logger.warn("Format de date invalide pour la recherche : {}", dateStr, e);
                        request.setAttribute("error", "Format de date invalide pour la recherche.");
                    }
                }
                List<Envoyer> envois;
                if (date != null) {
                    envois = envoyerService.getEnvoisByDate(date);
                } else {
                    envois = envoyerService.getAllEnvois(); // Si pas de date, affiche tout
                }
                request.setAttribute("envois", envois);
                // Utilise forwardToTemplate pour inclure la liste dans la page principale
                forwardToTemplate("envoyer/list", "envoyer", "Liste des Envois", request, response);
            }  else if ("showPdfForm".equals(action)) {
                logger.info("Chargement du formulaire PDF pour la modale via AJAX.");
                // Récupère uniquement les clients qui ont effectué des envois
                List<Client> clientsWithTransactions = clientService.getClientsWithTransactions();
                request.setAttribute("clients", clientsWithTransactions);
                request.getRequestDispatcher("/WEB-INF/views/envoyer/releve.jsp").forward(request, response);
            } else if ("pdf".equals(action)) {
                // Gère la génération du fichier PDF et l'envoie au navigateur
                generatePdf(request, response);
            } else { // Action par défaut : afficher la liste de tous les envois
                List<Envoyer> envois = envoyerService.getAllEnvois();
                request.setAttribute("envois", envois);
                forwardToTemplate("envoyer/list", "envoyer", "Liste des Envois", request, response);
            }
        } catch (NumberFormatException e) {
            logger.error("Erreur de format de nombre dans les paramètres GET : {}", e.getMessage(), e);
            sendErrorRedirect(request, response, "Un paramètre numérique est invalide.");
        } catch (Exception e) {
            // Capture les exceptions inattendues et redirige vers une page d'erreur
            logger.error("Erreur inattendue dans doGet pour l'action {}: {}", action, e.getMessage(), e);
            sendErrorRedirect(request, response, "Une erreur inattendue est survenue : " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Configure la réponse pour du JSON, car les POST sont généralement pour les actions AJAX
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        logger.info("Requête POST reçue, action: {}", action);

        try {
            if ("delete".equals(action)) {
                handleDelete(request, response);
            } else if ("update".equals(action)) {
                handleUpdate(request, response);
            } else {
                // Par défaut, si l'action n'est ni delete ni update, on considère que c'est une création
                handleCreate(request, response);
            }
        } catch (Exception e) {
            logger.error("Erreur inattendue dans doPost pour l'action {}: {}", action, e.getMessage(), e);
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ErrorResponse("Erreur interne du serveur : " + e.getMessage()));
        }
    }

    /**
     * Gère la création d'un nouvel envoi.
     * @param request La requête HTTP
     * @param response La réponse HTTP
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    private void handleCreate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idEnv = generateId();
        String numEnvoyeur = request.getParameter("numEnvoyeur");
        String numRecepteur = request.getParameter("numRecepteur");
        String raison = request.getParameter("raison");

        double montant = 0.0; // CHANGÉ: int -> double
        try {
            montant = Double.parseDouble(request.getParameter("montant")); // CHANGÉ: Integer.parseInt -> Double.parseDouble
        } catch (NumberFormatException e) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorResponse("Le montant doit être un nombre valide."));
            return;
        }

        // Récupère les objets Client à partir des numéros de téléphone
        Client envoyeur = clientService.getClient(numEnvoyeur);
        Client recepteur = clientService.getClient(numRecepteur);

        // Validations des clients et du montant
        if (envoyeur == null) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorResponse("Expéditeur non trouvé."));
            return;
        }
        if (recepteur == null) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorResponse("Destinataire non trouvé."));
            return;
        }
        if (envoyeur.getNumtel().equals(recepteur.getNumtel())) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorResponse("L'expéditeur et le destinataire ne peuvent pas être les mêmes."));
            return;
        }
        // Validation des pays
        if (envoyeur.getPays().equals(recepteur.getPays())) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    new ErrorResponse("L'envoyeur et le récepteur doivent être de pays différents."));
            return;
        }

        Envoyer envoi = new Envoyer(idEnv, envoyeur, recepteur, montant, LocalDateTime.now(), raison);

        try {
            envoyerService.createEnvoi(envoi);
            sendJsonResponse(response, HttpServletResponse.SC_OK, new MessageResponse("Transfert effectué avec succès !"));
        } catch (IllegalArgumentException e) {
            logger.warn("Échec de la validation lors de la création de l'envoi: {}", e.getMessage());
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            logger.error("État illégal lors de la création de l'envoi: {}", e.getMessage(), e);
            sendJsonResponse(response, HttpServletResponse.SC_CONFLICT, new ErrorResponse(e.getMessage() + " Veuillez contacter l'administrateur."));
        } catch (Exception e) {
            logger.error("Erreur lors de l'enregistrement du transfert: {}", e.getMessage(), e);
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ErrorResponse("Erreur lors de l'enregistrement du transfert : " + e.getMessage()));
        }
    }

    /**
     * Gère la mise à jour d'un envoi existant.
     * @param request La requête HTTP
     * @param response La réponse HTTP
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    private void handleUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idEnv = request.getParameter("idEnv");
        String numEnvoyeur = request.getParameter("numEnvoyeur");
        String numRecepteur = request.getParameter("numRecepteur");
        String raison = request.getParameter("raison");

        if (idEnv == null || idEnv.trim().isEmpty()) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorResponse("ID d'envoi manquant pour la mise à jour."));
            return;
        }

        double montant = 0.0; // CHANGÉ: int -> double
        try {
            montant = Double.parseDouble(request.getParameter("montant")); // CHANGÉ: Integer.parseInt -> Double.parseDouble
        } catch (NumberFormatException e) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorResponse("Le montant doit être un nombre valide."));
            return;
        }

        Client envoyeur = clientService.getClient(numEnvoyeur);
        Client recepteur = clientService.getClient(numRecepteur);

        if (envoyeur == null) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorResponse("Expéditeur non trouvé."));
            return;
        }
        if (recepteur == null) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorResponse("Destinataire non trouvé."));
            return;
        }
        if (envoyeur.getNumtel().equals(recepteur.getNumtel())) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorResponse("L'expéditeur et le destinataire ne peuvent pas être les mêmes."));
            return;
        }

        // Récupère l'envoi existant pour conserver la date de création originale
        Envoyer existingEnvoi = envoyerService.getEnvoiById(idEnv);
        if (existingEnvoi == null) {
            sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, new ErrorResponse("Envoi introuvable pour la mise à jour."));
            return;
        }
        // Validation des pays
        if (envoyeur.getPays().equals(recepteur.getPays())) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    new ErrorResponse("L'envoyeur et le récepteur doivent être de pays différents."));
            return;
        }
        // Crée un nouvel objet Envoyer avec les données mises à jour (en conservant la date d'origine)
        Envoyer updatedEnvoi = new Envoyer(idEnv, envoyeur, recepteur, montant, existingEnvoi.getDate(), raison);

        try {
            envoyerService.updateEnvoi(updatedEnvoi);
            sendJsonResponse(response, HttpServletResponse.SC_OK, new MessageResponse("Envoi mis à jour avec succès !"));
        } catch (IllegalArgumentException e) {
            logger.warn("Échec de la validation lors de la mise à jour de l'envoi: {}", e.getMessage());
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour de l'envoi: {}", e.getMessage(), e);
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ErrorResponse("Erreur lors de la mise à jour de l'envoi : " + e.getMessage()));
        }
    }


    /**
     * Gère la suppression d'un envoi.
     * @param request La requête HTTP
     * @param response La réponse HTTP
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    private void handleDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idEnv = request.getParameter("id");

        if (idEnv == null || idEnv.trim().isEmpty()) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorResponse("ID d'envoi manquant pour la suppression."));
            return;
        }

        try {
            envoyerService.deleteEnvoi(idEnv);
            sendJsonResponse(response, HttpServletResponse.SC_OK, new MessageResponse("Envoi supprimé avec succès !"));
        } catch (IllegalArgumentException e) {
            logger.warn("Échec de la validation lors de la suppression de l'envoi: {}", e.getMessage());
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de l'envoi: {}", e.getMessage(), e);
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new ErrorResponse("Erreur lors de la suppression de l'envoi : " + e.getMessage()));
        }
    }

    /**
     * Génère un ID unique pour un envoi.
     * @return L'ID généré.
     */
    private String generateId() {
        return "ENV" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Gère la génération du rapport PDF et son envoi au client.
     * @param request La requête HTTP (contient les paramètres du client, mois, année)
     * @param response La réponse HTTP
     * @throws ServletException En cas d'erreur servlet
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    private void generatePdf(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String numtel = request.getParameter("numtel");
        int month = 0;
        int year = 0;
        try {
            month = Integer.parseInt(request.getParameter("month"));
            year = Integer.parseInt(request.getParameter("year"));
        } catch (NumberFormatException e) {
            logger.warn("Mois ou année invalide pour le PDF : {}", e.getMessage());
            sendErrorRedirect(request, response, "Mois ou année invalide pour la génération du PDF.");
            return;
        }

        try {
            Client client = clientService.getClient(numtel);
            if (client == null) {
                sendErrorRedirect(request, response, "Client introuvable pour la génération du PDF.");
                return;
            }

            // Récupère les envois pour le client et la période spécifiés
            List<Envoyer> envois = envoyerService.getEnvoisByEnvoyeurAndMonth(numtel, month, year);

            if (envois == null || envois.isEmpty()) {
                sendErrorRedirect(request, response,
                        "Aucun envoi trouvé pour ce client et cette période pour le PDF.");
                return;
            }

            // Génère le PDF via le service
            byte[] pdfBytes = envoyerService.generatePdfReleve(client, envois, month, year);

            // Configure les en-têtes de la réponse pour le téléchargement du PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode("releve_" + numtel + "_" + month + "_" + year + ".pdf", StandardCharsets.UTF_8.toString()));
            response.setContentLength(pdfBytes.length);

            // Écrit le contenu du PDF dans le flux de sortie de la réponse
            ServletOutputStream out = response.getOutputStream();
            out.write(pdfBytes);
            out.flush();
            out.close();
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du PDF pour client {} (Mois: {}, Année: {}): {}",
                    numtel, month, year, e.getMessage(), e);
            sendErrorRedirect(request, response, "Erreur lors de la génération du PDF: " + e.getMessage());
        }
    }

    /**
     * Méthode utilitaire pour forwarder vers le template principal (structure de page complète).
     * Cette méthode doit être utilisée pour les pages complètes, PAS pour le contenu des modales AJAX.
     * @param contentJspPath Le chemin relatif du JSP de contenu (ex: "envoyer/list")
     * @param pageName Le nom de la page (pour le menu actif, etc.)
     * @param pageTitle Le titre de la page
     * @param request La requête HTTP
     * @param response La réponse HTTP
     * @throws ServletException En cas d'erreur servlet
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    private void forwardToTemplate(String contentJspPath, String pageName, String pageTitle,
                                   HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageName", pageName);
        request.setAttribute("pageTitle", pageTitle);
        request.setAttribute("contentPage", "/WEB-INF/views/" + contentJspPath + ".jsp");
        request.getRequestDispatcher("/WEB-INF/views/template.jsp").forward(request, response);
    }

    /**
     * Méthode utilitaire pour rediriger vers une page d'erreur (ou la page actuelle avec un message).
     * Le message d'erreur est encodé pour être passé dans l'URL.
     * @param request La requête HTTP
     * @param response La réponse HTTP
     * @param message Le message d'erreur à afficher
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    private void sendErrorRedirect(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
        // Redirige vers la page d'envois avec un paramètre d'erreur
        response.sendRedirect(request.getContextPath() + "/envoyer?error=" + encodedMessage);
    }

    /**
     * Méthode utilitaire pour envoyer une réponse JSON.
     * Utile pour les appels AJAX où la réponse est un objet JSON (succès/erreur).
     * @param response La réponse HTTP
     * @param status Le code de statut HTTP (ex: 200 OK, 400 Bad Request, 500 Internal Server Error)
     * @param object L'objet à sérialiser en JSON
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    private void sendJsonResponse(HttpServletResponse response, int status, Object object) throws IOException {
        response.setStatus(status);
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(object)); // Convertit l'objet en JSON et l'écrit
        out.flush(); // S'assure que toutes les données sont envoyées
    }

    // Classes internes pour les réponses JSON standards
    private static class RedirectInfo {
        public String redirect;
        public RedirectInfo(String redirect) {
            this.redirect = redirect;
        }
    }

    private static class ErrorResponse {
        public String error;
        public ErrorResponse(String error) {
            this.error = error;
        }
    }

    private static class MessageResponse {
        public String message;
        public MessageResponse(String message) {
            this.message = message;
        }
    }
}