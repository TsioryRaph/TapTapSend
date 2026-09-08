<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:if test="${not empty param.success}">
    <div class="alert alert-success">${param.success}</div>
</c:if>
<c:if test="${not empty requestScope.error}">
    <div class="alert alert-danger">Erreur : ${requestScope.error}</div>
</c:if>

<form method="post" action="${pageContext.request.contextPath}/envoyer">
    <c:if test="${not empty oldEnvoi && not empty oldEnvoi.idEnv}">
        <input type="hidden" name="action" value="update">
        <input type="hidden" name="idEnv" value="${oldEnvoi.idEnv}">
    </c:if>
    <c:if test="${empty oldEnvoi || empty oldEnvoi.idEnv}">
        <input type="hidden" name="action" value="create">
    </c:if>

    <div class="row g-3">
        <div class="col-md-6">
            <label for="numEnvoyeur" class="form-label"><i class="fas fa-user text-muted me-1"></i>Envoyeur</label>
            <select class="form-select" id="numEnvoyeur" name="numEnvoyeur" required onchange="validateTransfer()">
                <option value="">-- Sélectionnez l'envoyeur --</option>
                <c:forEach items="${clients}" var="client">
                    <option value="${client.numtel}" data-pays="${client.pays}" data-devise="${client.devise}" data-solde="${client.solde}" data-nom="${client.nom}"
                            <c:if test="${not empty oldEnvoi && oldEnvoi.envoyeur.numtel eq client.numtel}">selected</c:if>>
                        <c:out value="${client.nom}"/> (${client.numtel}) - <c:out value="${client.pays}"/>
                    </option>
                </c:forEach>
            </select>
            <div class="profile-preview mt-2" id="envoyeurPreview">
                <span class="placeholder-text">Aucun envoyeur sélectionné</span>
            </div>
        </div>

        <div class="col-md-6">
            <label for="numRecepteur" class="form-label"><i class="fas fa-user-check text-muted me-1"></i>Récepteur</label>
            <select class="form-select" id="numRecepteur" name="numRecepteur" required onchange="validateTransfer()">
                <option value="">-- Sélectionnez le récepteur --</option>
                <c:forEach items="${clients}" var="client">
                    <option value="${client.numtel}" data-pays="${client.pays}" data-devise="${client.devise}" data-solde="${client.solde}" data-nom="${client.nom}"
                            <c:if test="${not empty oldEnvoi && oldEnvoi.recepteur.numtel eq client.numtel}">selected</c:if>>
                        <c:out value="${client.nom}"/> (${client.numtel}) - <c:out value="${client.pays}"/>
                    </option>
                </c:forEach>
            </select>
            <div class="profile-preview mt-2" id="recepteurPreview">
                <span class="placeholder-text">Aucun récepteur sélectionné</span>
            </div>
        </div>

        <div class="col-md-6">
            <div class="tt-field">
                <input type="number" class="form-control" id="montant" name="montant" placeholder=" " required min="0.01" step="0.01"
                       value="${not empty oldEnvoi ? oldEnvoi.montant : ''}" oninput="validateTransfer()">
                <label for="montant">Montant (<span id="montantDevise">EUR</span>)</label>
            </div>
        </div>

        <div class="col-md-6">
            <div class="tt-field">
                <input type="datetime-local" class="form-control" id="dateTransfert" name="date" placeholder=" "
                       value="<fmt:formatDate value="${not empty oldEnvoi ? oldEnvoi.displayDate : ''}" pattern="yyyy-MM-dd'T'HH:mm"/>" required>
                <label for="dateTransfert">Date et heure</label>
            </div>
        </div>

        <div class="col-12">
            <div class="tt-field">
                <input type="text" class="form-control" id="raison" name="raison" placeholder=" " required
                       value="${not empty oldEnvoi ? oldEnvoi.raison : ''}">
                <label for="raison">Raison du transfert</label>
            </div>
        </div>
    </div>

    <div class="modal-footer-actions">
        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Annuler</button>
        <button type="submit" class="btn btn-primary" id="submitButton">
            <i class="fas fa-paper-plane me-1"></i>
            <c:choose>
                <c:when test="${not empty oldEnvoi && not empty oldEnvoi.idEnv}">Mettre à jour</c:when>
                <c:otherwise>Effectuer le transfert</c:otherwise>
            </c:choose>
        </button>
    </div>
</form>

<script>
function renderProfilePreview(select, containerId) {
    const container = document.getElementById(containerId);
    if (!select.value) {
        container.innerHTML = '<span class="placeholder-text">Aucune sélection</span>';
        return;
    }
    const opt = select.options[select.selectedIndex];
    const nom = opt.getAttribute('data-nom');
    const pays = opt.getAttribute('data-pays');
    const devise = opt.getAttribute('data-devise');
    const solde = parseFloat(opt.getAttribute('data-solde')).toFixed(2);
    const initiale = nom ? nom.charAt(0).toUpperCase() : '?';

    container.innerHTML =
        '<div class="avatar-circle">' + initiale + '</div>' +
        '<div>' +
            '<div class="fw-semibold">' + nom + '</div>' +
            '<small class="text-muted">' + pays + ' &middot; Solde: ' + solde + ' ' + devise + '</small>' +
        '</div>';
}

function validateTransfer() {
    const envoyeurSelect = document.getElementById('numEnvoyeur');
    const recepteurSelect = document.getElementById('numRecepteur');
    const montantInput = document.getElementById('montant');
    const submitButton = document.getElementById('submitButton');
    const montantDeviseSpan = document.getElementById('montantDevise');

    renderProfilePreview(envoyeurSelect, 'envoyeurPreview');
    renderProfilePreview(recepteurSelect, 'recepteurPreview');

    submitButton.disabled = false;

    if (!envoyeurSelect.value || !recepteurSelect.value) {
        montantDeviseSpan.textContent = 'EUR';
        return true;
    }

    const selectedEnvoyeur = envoyeurSelect.options[envoyeurSelect.selectedIndex];
    const selectedRecepteur = recepteurSelect.options[recepteurSelect.selectedIndex];

    const paysEnvoyeur = selectedEnvoyeur.getAttribute('data-pays');
    const paysRecepteur = selectedRecepteur.getAttribute('data-pays');
    const deviseEnvoyeur = selectedEnvoyeur.getAttribute('data-devise');
    const deviseRecepteur = selectedRecepteur.getAttribute('data-devise');
    const soldeEnvoyeur = parseFloat(selectedEnvoyeur.getAttribute('data-solde')) || 0;
    const montant = parseFloat(montantInput.value);

    montantDeviseSpan.textContent = deviseEnvoyeur;

    if (envoyeurSelect.value === recepteurSelect.value) {
        alert("L'expéditeur et le destinataire ne peuvent pas être les mêmes.");
        submitButton.disabled = true;
        return false;
    }
    if (paysEnvoyeur === paysRecepteur) {
        alert("L'envoyeur et le récepteur doivent être de pays différents pour un transfert international !");
        submitButton.disabled = true;
        return false;
    }
    if (deviseEnvoyeur === "EUR" && deviseRecepteur !== "MGA") {
        alert("Un envoi depuis un pays en EUR doit être vers Madagascar (MGA).");
        submitButton.disabled = true;
        return false;
    }
    if (deviseEnvoyeur === "MGA" && deviseRecepteur !== "EUR") {
        alert("Un envoi depuis Madagascar (MGA) doit être vers un pays en EUR.");
        submitButton.disabled = true;
        return false;
    }
    if (!isNaN(montant) && deviseEnvoyeur === "EUR" && montant > soldeEnvoyeur) {
        alert("Solde insuffisant pour l'envoyeur (EUR). Le montant à envoyer est supérieur au solde disponible.");
        submitButton.disabled = true;
        return false;
    }

    return true;
}

document.querySelector('form').addEventListener('submit', function(e) {
    if (!validateTransfer()) e.preventDefault();
});
document.addEventListener('DOMContentLoaded', validateTransfer);
</script>