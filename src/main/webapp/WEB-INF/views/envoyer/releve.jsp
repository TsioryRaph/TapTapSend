<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="fr_FR" scope="session"/>

<p class="text-muted mb-3">Sélectionnez les critères pour générer le relevé (PDF).</p>

<form id="pdfForm" action="${pageContext.request.contextPath}/envoyer" method="get">
    <div class="mb-3">
        <label for="numtelReleve" class="form-label">Client envoyeur</label>
        <select class="form-select" id="numtelReleve" name="numtel" required>
            <option value="">Sélectionner un client envoyeur</option>
            <c:forEach items="${clients}" var="client">
                <option value="${client.numtel}"><c:out value="${client.nom}"/> (${client.numtel}) - <c:out value="${client.pays}"/></option>
            </c:forEach>
        </select>
        <div class="form-text">Seuls les clients ayant effectué des envois sont affichés.</div>
    </div>

    <div class="row g-3">
        <div class="col-md-8">
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
        <div class="col-md-4">
            <div class="tt-field">
                <input type="number" class="form-control" id="yearReleve" name="year" placeholder=" " required
                       min="2000" max="${java.time.LocalDate.now().getYear()}" value="${java.time.LocalDate.now().getYear()}">
                <label for="yearReleve">Année</label>
            </div>
        </div>
    </div>

    <input type="hidden" name="action" value="pdf">

    <div class="modal-footer-actions">
        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Annuler</button>
        <button type="submit" class="btn btn-primary" formtarget="_blank">
            <i class="fas fa-file-pdf me-1"></i> Générer PDF
        </button>
    </div>
</form>