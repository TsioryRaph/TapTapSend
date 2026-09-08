<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<form id="modalForm" method="post" action="${pageContext.request.contextPath}/clients">
    <c:if test="${client.numtel != null && client.numtel != ''}">
        <input type="hidden" name="originalNumtel" value="${client.numtel}">
    </c:if>

    <div class="avatar-preview" id="avatarPreview">
        <c:out value="${not empty client.nom ? fn:toUpperCase(fn:substring(client.nom,0,1)) : '?'}"/>
    </div>

    <div class="row g-3">
        <div class="col-md-6">
            <div class="tt-field">
                <input type="text" class="form-control" id="numtel" name="numtel" placeholder=" "
                       value="${client.numtel}" <c:if test="${not empty client.numtel}">readonly</c:if> required>
                <label for="numtel">Numéro de téléphone</label>
            </div>
        </div>
        <div class="col-md-6">
            <div class="tt-field">
                <input type="text" class="form-control" id="nom" name="nom" placeholder=" " value="${client.nom}" required>
                <label for="nom">Nom complet</label>
            </div>
        </div>

        <div class="col-md-6">
            <label class="form-label">Sexe</label>
            <div class="segmented">
                <input type="radio" name="sexe" id="sexeM" value="Masculin" ${client.sexe eq 'Masculin' || empty client.sexe ? 'checked' : ''}>
                <label for="sexeM"><i class="fas fa-mars me-1"></i>Masculin</label>
                <input type="radio" name="sexe" id="sexeF" value="Féminin" ${client.sexe eq 'Féminin' ? 'checked' : ''}>
                <label for="sexeF"><i class="fas fa-venus me-1"></i>Féminin</label>
            </div>
        </div>
        <div class="col-md-6">
            <div class="tt-field">
                <input type="text" class="form-control" id="pays" name="pays" placeholder=" " value="${client.pays}" required>
                <label for="pays">Pays</label>
            </div>
        </div>
        <div class="col-md-6">
            <div class="tt-field">
                <input type="number" class="form-control" id="solde" name="solde" placeholder=" "
                       value="${client.solde}" step="0.01" inputmode="decimal" required>
                <label for="solde">Solde initial</label>
            </div>
        </div>
        <div class="col-md-6">
            <div class="tt-field">
                <input type="email" class="form-control" id="mail" name="mail" placeholder=" " value="${client.mail}" required>
                <label for="mail">Email</label>
            </div>
        </div>
    </div>

    <div class="modal-footer-actions">
        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Annuler</button>
        <button type="submit" class="btn btn-primary"><i class="fas fa-check me-1"></i>Enregistrer</button>
    </div>
</form>

<script>
(function() {
    const nomInput = document.getElementById('nom');
    const preview = document.getElementById('avatarPreview');
    if (nomInput && preview) {
        nomInput.addEventListener('input', function() {
            preview.textContent = this.value.trim().charAt(0).toUpperCase() || '?';
        });
    }
})();
</script>