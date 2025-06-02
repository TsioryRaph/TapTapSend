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

@WebServlet("/clients")
public class ClientServlet extends BaseServlet {
    private final ClientService clientService = new ClientService();
    private final Gson gson = new Gson(); // Initialisation de Gson pour la conversion JSON

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("edit".equals(action)) { // Cette branche gère maintenant l'ajout ET la modification
            String numtel = request.getParameter("numtel");
            Client client = null;

            // Si un numéro de téléphone est fourni (pour la modification)
            if (numtel != null && !numtel.trim().isEmpty()) {
                client = clientService.getClient(numtel);
                if (client == null) {
                    System.out.println("Client avec numtel " + numtel + " introuvable. Préparation d'un formulaire vide.");
                    client = new Client(); // Fournir un nouvel objet Client vide
                }
            } else {
                // Si aucun numéro de téléphone n'est fourni (c'est le cas pour l'ajout)
                System.out.println("Aucun numtel fourni. Préparation d'un formulaire vide pour un nouveau client.");
                client = new Client(); // Créer un nouvel objet Client vide pour le formulaire d'ajout
            }

            request.setAttribute("client", client); // Le client (existant ou vide) est mis en attribut
            // IMPORTANT : Pour l'action "edit" (chargement du formulaire dans la modale),
            // on ne passe PAS par le template principal. On forward directement vers le JSP du formulaire.
            request.getRequestDispatcher("/WEB-INF/views/client/form.jsp").forward(request, response);
            // Pas besoin de 'return' ici, car forward() termine le traitement de la requête.

        } else if ("delete".equals(action)) {
            String numtel = request.getParameter("numtel");
            clientService.deleteClient(numtel);
            // La suppression n'est généralement pas faite via AJAX pour la confirmation,
            // donc une redirection normale est acceptable ici.
            response.sendRedirect(request.getContextPath() + "/clients?success=Client+supprimé+avec+succès");

        } else if ("search".equals(action)) { // Ou n'importe quelle autre action qui affiche la liste
            String searchTerm = request.getParameter("searchTerm");
            List<Client> clients = clientService.searchClients(searchTerm);
            request.setAttribute("clients", clients);
            // Utilise forwardToTemplate pour inclure la liste dans le layout principal
            forwardToTemplate("client/list", "clients", "Liste des Clients", request, response);

        } else { // Action par défaut : afficher tous les clients
            List<Client> clients = clientService.getAllClients();
            request.setAttribute("clients", clients);
            // Utilise forwardToTemplate pour inclure la liste dans le layout principal
            forwardToTemplate("client/list", "clients", "Liste des Clients", request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // IMPORTANT : Définir le type de contenu de la réponse pour le JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String numtel = request.getParameter("numtel");
        String nom = request.getParameter("nom");
        String sexe = request.getParameter("sexe");
        String pays = request.getParameter("pays");
        String mail = request.getParameter("mail");

        int solde = 0;
        try {
            solde = Integer.parseInt(request.getParameter("solde"));
        } catch (NumberFormatException e) {
            // Gérer l'erreur si le solde n'est pas un nombre valide
            String jsonError = "{\"error\": \"Le solde doit être un nombre valide.\"}";
            response.getWriter().write(jsonError);
            return; // Arrêter l'exécution de la méthode
        }

        Client client = new Client(numtel, nom, sexe, pays, solde, mail);
        String successMessage = "";
        String errorMessage = "";

        if (clientService.getClient(numtel) != null) {
            // C'est une mise à jour
            clientService.updateClient(client);
            successMessage = "Client mis à jour avec succès !";
        } else {
            // C'est une création
            // Il est recommandé de vérifier l'unicité du numéro de téléphone avant la création
            // Si votre service le gère déjà et lève une exception, capturez-la.
            try {
                clientService.createClient(client);
                successMessage = "Client créé avec succès !";
            } catch (Exception e) { // Remplacez par une exception plus spécifique si votre service en lève
                errorMessage = "Erreur lors de la création du client : " + e.getMessage();
                // Si le service indique une erreur de doublon, ajustez le message
                if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) { // Exemple pour MySQL
                    errorMessage = "Un client avec ce numéro de téléphone existe déjà.";
                }
            }
        }

        // Préparer la réponse JSON pour le JavaScript dans le template
        if (!successMessage.isEmpty()) {
            // En cas de succès, renvoyer une redirection
            String redirectUrl = request.getContextPath() + "/clients?success=" + successMessage.replace(" ", "+");
            response.getWriter().write(gson.toJson(new RedirectResponse(redirectUrl)));
        } else if (!errorMessage.isEmpty()) {
            // En cas d'erreur de validation ou logique, renvoyer un message d'erreur
            // Ou vous pourriez re-render le form.jsp avec des messages d'erreur si vous voulez l'afficher dans la modale
            response.getWriter().write(gson.toJson(new ErrorResponse(errorMessage)));
        }
    }

    /**
     * Méthode utilitaire pour forwarder vers le template principal.
     * Cette méthode devrait idéalement être dans votre BaseServlet.
     */
    private void forwardToTemplate(String contentJspPath, String pageName, String pageTitle,
                                   HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageName", pageName);
        request.setAttribute("pageTitle", pageTitle);
        // Le chemin complet vers le JSP de contenu à inclure dans le template
        request.setAttribute("contentPage", "/WEB-INF/views/" + contentJspPath + ".jsp");
        // Forward vers le template principal
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