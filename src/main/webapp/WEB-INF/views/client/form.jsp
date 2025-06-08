<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%--
    Ce formulaire est conçu pour être inclus dans une modale ou une autre section de page.
    Il gère à la fois l'ajout d'un nouveau client et la modification d'un client existant.
    La présence du champ caché 'originalNumtel' et l'attribut 'readonly' sur 'numtel'
    permettent au servlet de distinguer l'opération.
--%>
<form id="modalForm" method="post" action="${pageContext.request.contextPath}/clients">
    <%--
        Champ caché pour identifier si l'opération est une mise à jour.
        Ce champ est présent UNIQUEMENT si un client existant est en cours de modification
        (c-à-d, si ${client.numtel} n'est pas vide, car il est rempli par le doGet pour les clients existants).
        Sa valeur est le numéro de téléphone original du client.
    --%>
    <c:if test="${client.numtel != null && client.numtel != ''}">
        <input type="hidden" name="originalNumtel" value="${client.numtel}">
    </c:if>

    <div class="mb-3">
        <label for="numtel" class="form-label">Numéro de téléphone</label>
        <input type="text" class="form-control" id="numtel" name="numtel"
               value="${client.numtel}"
               <c:if test="${not empty client.numtel}">readonly</c:if> <%-- Ajoute 'readonly' si le numtel est déjà défini (mode modification) --%>
               required>
        <%-- Explication: si le numtel est déjà présent (pour une modification), il est en lecture seule.
             Cela empêche l'utilisateur de changer le numéro de téléphone d'un client existant,
             simplifiant la logique de mise à jour. Si ce n'est pas le comportement désiré
             (si vous voulez permettre de changer le numtel d'un client existant), retirez 'readonly'.
             Toutefois, si numtel est la clé primaire, le changer signifierait créer un nouveau client
             ou une logique de remplacement complexe. Il est généralement préférable de le laisser en lecture seule.
        --%>
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