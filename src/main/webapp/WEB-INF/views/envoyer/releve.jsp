<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<fmt:setLocale value="fr_FR" scope="session"/>

<%-- ATTENTION: Ce fichier ne doit contenir AUCUN tag <script> ou code JavaScript.
     Le JavaScript pour la modale et la soumission est dans template.jsp.
     Le div avec l'ID "modalForm" DOIT ÊTRE SUPPRIMÉ ici. --%>

<%-- Le contenu qui sera inséré directement dans <div id="modalBody"> --%>
<p>Sélectionnez les critères pour générer le relevé (PDF).</p>

<form id="pdfForm" action="${pageContext.request.contextPath}/envoyer" method="get">
    <div class="mb-3">
        <label for="numtelReleve" class="form-label">Client</label> <%-- Changé le label pour "Client" --%>
        <%-- L'input text est remplacé par un select pour une meilleure UX --%>
        <select class="form-select" id="numtelReleve" name="numtel" required>
            <option value="">Sélectionner un client</option>
            <c:forEach items="${clients}" var="client">
                <option value="${client.numtel}">${client.nom} (${client.numtel})</option>
            </c:forEach>
        </select>
    </div>

    <div class="mb-3">
        <label for="monthReleve" class="form-label">Mois</label>
        <select class="form-select" id="monthReleve" name="month" required>
            <option value="">Sélectionner un mois</option>
            <option value="1">Janvier</option>
            <option value="2">Février</option>
            <option value="3">Mars</option>
            <option value="4">Avril</option>
            <option value="5">Mai</option>
            <option value="6">Juin</option>
            <option value="7">Juillet</option>
            <option value="8">Août</option>
            <option value="9">Septembre</option>
            <option value="10">Octobre</option>
            <option value="11">Novembre</option>
            <option value="12">Décembre</option>
        </select>
    </div>

    <div class="mb-3">
        <label for="yearReleve" class="form-label">Année</label>
        <input type="number" class="form-control" id="yearReleve" name="year" required
               min="2000" max="${java.time.LocalDate.now().getYear()}" value="${java.time.LocalDate.now().getYear()}">
    </div>

    <%-- Input caché pour l'action PDF, nécessaire pour le servlet --%>
    <input type="hidden" name="action" value="pdf">

    <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Annuler</button>

        <%-- Bouton pour générer directement le PDF --%>
        <%-- formtarget="_blank" est important pour ouvrir le PDF dans un nouvel onglet --%>
        <button type="submit" class="btn btn-primary" formtarget="_blank">
            <i class="fas fa-file-pdf me-1"></i> Générer PDF
        </button>
    </div>
</form>