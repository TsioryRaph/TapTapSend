<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- Le formulaire lui-même, sans <html>, <head>, <body> --%>
<form id="modalForm" method="post" action="${pageContext.request.contextPath}/clients">
    <div class="mb-3">
        <label for="numtel" class="form-label">Numéro de téléphone</label>
        <input type="text" class="form-control" id="numtel" name="numtel"
               value="${client.numtel}" ${not empty client.numtel ? 'readonly' : ''} required>
        <%-- 'readonly' si on est en mode modification --%>
    </div>
    <div class="mb-3">
        <label for="nom" class="form-label">Nom complet</label>
        <input type="text" class="form-control" id="nom" name="nom"
               value="${client.nom}" required>
    </div>
    <div class="mb-3">
        <label for="sexe" class="form-label">Sexe</label>
        <select class="form-select" id="sexe" name="sexe" required>
            <option value="Masculin" ${client.sexe eq 'Masculin' ? 'selected' : ''}>Masculin</option>
            <option value="Féminin" ${client.sexe eq 'Féminin' ? 'selected' : ''}>Féminin</option>
        </select>
    </div>
    <div class="mb-3">
        <label for="pays" class="form-label">Pays</label>
        <input type="text" class="form-control" id="pays" name="pays"
               value="${client.pays}" required>
    </div>
    <div class="mb-3">
        <label for="solde" class="form-label">Solde initial</label>
        <input type="number" class="form-control" id="solde" name="solde"
               value="${client.solde}" required>
    </div>
    <div class="mb-3">
        <label for="mail" class="form-label">Email</label>
        <input type="email" class="form-control" id="mail" name="mail"
               value="${client.mail}" required>
    </div>
    <div class="d-flex justify-content-end">
        <button type="button" class="btn btn-secondary me-2" data-bs-dismiss="modal">Annuler</button>
        <button type="submit" class="btn btn-primary">Enregistrer</button>
    </div>
</form>