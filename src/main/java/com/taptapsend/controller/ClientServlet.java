package com.taptapsend.controller;

import com.taptapsend.model.Client;
import com.taptapsend.service.ClientService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import com.google.gson.Gson; // Assurez-vous d'avoir cette dépendance dans votre pom.xml

/**
 * Servlet pour la gestion des clients (affichage, ajout, modification, suppression).
 * Le numéro de téléphone est traité comme un identifiant unique pour un client.
 * Cette version corrige la logique de distinction entre création et mise à jour.
 */
@WebServlet("/clients")
public class ClientServlet extends BaseServlet {
    // Initialisation du service client pour les opérations métier
    private final ClientService clientService = new ClientService();
    // Initialisation de Gson pour la conversion JSON des réponses
    private final Gson gson = new Gson();

    /**
     * Gère les requêtes GET pour afficher les clients, le formulaire d'édition/ajout ou la suppression.
     * @param request L'objet HttpServletRequest
     * @param response L'objet HttpServletResponse
     * @throws ServletException Si une erreur spécifique aux servlets survient
     * @throws IOException Si une erreur d'entrée/sortie survient
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action"); // Récupère l'action demandée

        if ("edit".equals(action)) {
            // Cette branche gère le chargement du formulaire pour l'ajout ou la modification d'un client.
            String numtel = request.getParameter("numtel"); // Récupère le numéro de téléphone pour la modification
            Client client = null;

            // Si un numéro de téléphone est fourni, on est en mode modification
            if (numtel != null && !numtel.trim().isEmpty()) {
                client = clientService.getClient(numtel); // Tente de récupérer le client existant
                if (client == null) {
                    System.out.println("Client avec numtel " + numtel + " introuvable. Préparation d'un formulaire vide.");
                    client = new Client(); // Si non trouvé, fournit un objet Client vide
                }
            } else {
                // Si aucun numéro de téléphone n'est fourni, on est en mode ajout
                System.out.println("Aucun numtel fourni. Préparation d'un formulaire vide pour un nouveau client.");
                client = new Client(); // Crée un nouvel objet Client vide pour le formulaire d'ajout
            }

            request.setAttribute("client", client); // Le client (existant ou vide) est mis en attribut de requête
            // Forward directement vers le JSP du formulaire pour l'affichage dans la modale.
            request.getRequestDispatcher("/WEB-INF/views/client/form.jsp").forward(request, response);

        } else if ("delete".equals(action)) {
            // Gère la suppression d'un client.
            String numtel = request.getParameter("numtel");
            clientService.deleteClient(numtel); // Appelle le service pour supprimer le client
            // Redirige vers la liste des clients avec un message de succès.
            response.sendRedirect(request.getContextPath() + "/clients?success=Client+supprimé+avec+succès");

        } else if ("search".equals(action)) {
            // Gère la recherche de clients.
            String searchTerm = request.getParameter("searchTerm");
            List<Client> clients = clientService.searchClients(searchTerm); // Effectue la recherche
            request.setAttribute("clients", clients); // Met les résultats en attribut
            // Utilise forwardToTemplate pour inclure la liste dans le layout principal.
            forwardToTemplate("client/list", "clients", "Liste des Clients", request, response);

        } else {
            // Action par défaut : affiche la liste de tous les clients.
            List<Client> clients = clientService.getAllClients(); // Récupère tous les clients
            request.setAttribute("clients", clients); // Met la liste en attribut
            // Utilise forwardToTemplate pour inclure la liste dans le layout principal.
            forwardToTemplate("client/list", "clients", "Liste des Clients", request, response);
        }
    }

    /**
     * Gère les requêtes POST pour la création ou la mise à jour d'un client.
     * C'est ici que la logique de distinction entre création et mise à jour est appliquée.
     * @param request L'objet HttpServletRequest
     * @param response L'objet HttpServletResponse
     * @throws ServletException Si une erreur spécifique aux servlets survient
     * @throws IOException Si une erreur d'entrée/sortie survient
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Définit le type de contenu de la réponse pour le JSON, essentiel pour les requêtes AJAX.
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Récupère les paramètres du formulaire
        String numtel = request.getParameter("numtel");
        String nom = request.getParameter("nom");
        String sexe = request.getParameter("sexe");
        String pays = request.getParameter("pays");
        String mail = request.getParameter("mail");

        int solde = 0;
        try {
            solde = Integer.parseInt(request.getParameter("solde")); // Convertit le solde en entier
        } catch (NumberFormatException e) {
            // Gère l'erreur si le solde n'est pas un nombre valide.
            String jsonError = "{\"error\": \"Le solde doit être un nombre valide.\"}";
            response.getWriter().write(jsonError); // Renvoie une erreur JSON
            return; // Arrête l'exécution de la méthode
        }

        // Crée un objet Client avec les données soumises.
        Client client = new Client(numtel, nom, sexe, pays, solde, mail);
        String successMessage = "";
        String errorMessage = "";

        // >>> LOGIQUE CLÉ : DISTINGUER CRÉATION ET MISE À JOUR <<<
        // Le champ 'originalNumtel' est envoyé par le JSP UNIQUEMENT si le formulaire est en mode modification.
        // Sa présence (non null et non vide) indique que l'utilisateur tente de modifier un client existant.
        String originalNumtel = request.getParameter("originalNumtel");
        boolean isUpdateOperation = (originalNumtel != null && !originalNumtel.trim().isEmpty());

        if (isUpdateOperation) {
            // SCÉNARIO : C'est une opération de MISE À JOUR d'un client existant.
            // Dans votre JSP, le champ 'numtel' est marqué 'readonly' en mode modification.
            // Cela signifie que l'utilisateur ne peut pas changer le numéro de téléphone existant.
            // Par conséquent, le 'numtel' soumis est forcément l'original, et nous n'avons pas
            // à vérifier si le nouveau 'numtel' est déjà pris par un autre client.
            // Si vous décidez de rendre le 'numtel' modifiable en mode update,
            // vous devrez ajouter ici une vérification si le nouveau numtel (s'il diffère de originalNumtel)
            // est déjà pris par un AUTRE client.

            clientService.updateClient(client); // Appelle le service pour mettre à jour le client
            successMessage = "Client mis à jour avec succès !";

        } else {
            // SCÉNARIO : C'est une opération de CRÉATION d'un nouveau client.
            // On vérifie STRICTEMENT si le numéro de téléphone soumis est déjà utilisé par un client existant.
            // Si c'est le cas, on bloque la création et on renvoie un message d'erreur.
            if (clientService.getClient(numtel) != null) {
                errorMessage = "Ce numéro de téléphone est déjà utilisé par un client existant. Impossible de créer un nouveau client avec ce numéro.";
                response.getWriter().write(gson.toJson(new ErrorResponse(errorMessage))); // Renvoie une erreur JSON
                return; // Arrête l'exécution pour empêcher la création/l'écrasement
            }

            // Si le numéro de téléphone n'est pas utilisé, on procède à la création du nouveau client.
            try {
                clientService.createClient(client); // Appelle le service pour créer le nouveau client
                successMessage = "Client créé avec succès !";
            } catch (Exception e) {
                // Ce bloc `catch` agit comme un filet de sécurité.
                // Il capture les erreurs potentielles lors de la création, notamment
                // si une contrainte UNIQUE de la base de données est violée (ex: à cause d'une condition de concurrence
                // où un autre processus aurait créé le même numéro juste avant).
                errorMessage = "Erreur lors de la création du client : " + e.getMessage();
                // Si l'erreur provient spécifiquement d'une violation de contrainte de doublon (selon le message d'erreur de la DB)
                if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) { // Exemple pour MySQL/PostgreSQL
                    errorMessage = "Un client avec ce numéro de téléphone existe déjà. Impossible de créer un nouveau client avec ce numéro.";
                }
            }
        }

        // Préparer la réponse JSON pour le JavaScript côté client.
        if (!successMessage.isEmpty()) {
            // En cas de succès (mise à jour ou nouvelle création sans doublon), renvoie une URL de redirection.
            String redirectUrl = request.getContextPath() + "/clients?success=" + successMessage.replace(" ", "+");
            response.getWriter().write(gson.toJson(new RedirectResponse(redirectUrl)));
        } else if (!errorMessage.isEmpty()) {
            // En cas d'erreur (ex: solde invalide, ou tentative de création avec un numtel existant déjà),
            // renvoie un message d'erreur.
            response.getWriter().write(gson.toJson(new ErrorResponse(errorMessage)));
        }
    }

    /**
     * Méthode utilitaire pour forwarder vers le template principal (layout).
     * Cette méthode devrait idéalement être dans votre BaseServlet si elle est utilisée ailleurs.
     * @param contentJspPath Le chemin relatif du JSP de contenu (ex: "client/list")
     * @param pageName Le nom logique de la page
     * @param pageTitle Le titre de la page affiché dans le navigateur
     * @param request L'objet HttpServletRequest
     * @param response L'objet HttpServletResponse
     * @throws ServletException Si une erreur spécifique aux servlets survient
     * @throws IOException Si une erreur d'entrée/sortie survient
     */
    private void forwardToTemplate(String contentJspPath, String pageName, String pageTitle,
                                   HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageName", pageName); // Nom de la page pour le template
        request.setAttribute("pageTitle", pageTitle); // Titre de la page pour le template
        // Le chemin complet vers le JSP de contenu à inclure dans le template principal
        request.setAttribute("contentPage", "/WEB-INF/views/" + contentJspPath + ".jsp");
        // Forward vers le template principal qui se chargera d'inclure le contenu.
        request.getRequestDispatcher("/WEB-INF/views/template.jsp").forward(request, response);
    }

    // Classes internes pour les réponses JSON (peuvent être des classes séparées si vous préférez)
    private static class RedirectResponse {
        public String redirect; // URL de redirection
        public RedirectResponse(String redirect) {
            this.redirect = redirect;
        }
    }

    private static class ErrorResponse {
        public String error; // Message d'erreur
        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}