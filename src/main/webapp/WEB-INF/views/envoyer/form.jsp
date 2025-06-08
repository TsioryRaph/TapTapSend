<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="card">
    <div class="card-header">
        <h5>
            <c:choose>
                <c:when test="${not empty oldEnvoi && not empty oldEnvoi.idEnv}">Modifier un transfert</c:when>
                <c:otherwise>Nouveau transfert d'argent</c:otherwise>
            </c:choose>
        </h5>
    </div>
    <div class="card-body">
        <%-- Affichage des messages de succès ou d'erreur --%>
        <c:if test="${not empty param.success}">
            <div class="alert alert-success" role="alert">
                ${param.success}
            </div>
        </c:if>
        <c:if test="${not empty requestScope.error}">
            <div class="alert alert-danger" role="alert">
                Erreur : ${requestScope.error}
            </div>
        </c:if>

        <%-- Formulaire qui s'adapte pour l'ajout ou la modification --%>
        <form method="post" action="${pageContext.request.contextPath}/envoyer">
            <c:if test="${not empty oldEnvoi && not empty oldEnvoi.idEnv}">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="idEnv" value="${oldEnvoi.idEnv}">
            </c:if>
            <c:if test="${empty oldEnvoi || empty oldEnvoi.idEnv}">
                <input type="hidden" name="action" value="create">
            </c:if>

            <div class="mb-3">
                <label for="numEnvoyeur" class="form-label">Envoyeur</label>
                <select class="form-select" id="numEnvoyeur" name="numEnvoyeur" required onchange="validateTransfer()">
                    <option value="">-- Sélectionnez l'envoyeur --</option>
                    <c:forEach items="${clients}" var="client">
                        <option value="${client.numtel}" data-pays="${client.pays}" data-devise="${client.devise}"
                                <c:if test="${not empty oldEnvoi && oldEnvoi.envoyeur.numtel eq client.numtel}">selected</c:if>
                        >
                            ${client.nom} (${client.numtel}) - Solde: <fmt:formatNumber value="${client.solde}" type="number" minFractionDigits="2" maxFractionDigits="2"/> ${client.devise} - Pays: ${client.pays}
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="mb-3">
                <label for="numRecepteur" class="form-label">Récepteur</label>
                <select class="form-select" id="numRecepteur" name="numRecepteur" required onchange="validateTransfer()">
                    <option value="">-- Sélectionnez le récepteur --</option>
                    <c:forEach items="${clients}" var="client">
                        <option value="${client.numtel}" data-pays="${client.pays}" data-devise="${client.devise}"
                                <c:if test="${not empty oldEnvoi && oldEnvoi.recepteur.numtel eq client.numtel}">selected</c:if>
                        >
                            ${client.nom} (${client.numtel}) - Pays: ${client.pays} - Solde: <fmt:formatNumber value="${client.solde}" type="number" minFractionDigits="2" maxFractionDigits="2"/> ${client.devise}
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="mb-3">
                <label for="montant" class="form-label">Montant à envoyer (<span id="montantDevise">EUR</span>)</label>
                <input type="number" class="form-control" id="montant" name="montant" required min="0.01" step="0.01"
                        value="${not empty oldEnvoi ? oldEnvoi.montant : ''}" oninput="validateTransfer()">
            </div>

            <%-- NOUVEAU CHAMP POUR LA DATE --%>
            <div class="mb-3">
                <label for="dateTransfert" class="form-label">Date et Heure du transfert</label>
                <input type="datetime-local" class="form-control" id="dateTransfert" name="date"
                        value="<fmt:formatDate value="${not empty oldEnvoi ? oldEnvoi.displayDate : ''}" pattern="yyyy-MM-dd'T'HH:mm"/>" required>
            </div>

            <div class="mb-3">
                <label for="raison" class="form-label">Raison du transfert</label>
                <input type="text" class="form-control" id="raison" name="raison" required
                        value="${not empty oldEnvoi ? oldEnvoi.raison : ''}">
            </div>

            <button type="submit" class="btn btn-success" id="submitButton">
                <c:choose>
                    <c:when test="${not empty oldEnvoi && not empty oldEnvoi.idEnv}">Mettre à jour</c:when>
                    <c:otherwise>Effectuer le transfert</c:otherwise>
                </c:choose>
            </button>
            <a href="${pageContext.request.contextPath}/envoyer" class="btn btn-secondary">Annuler</a>
        </form>
    </div>
</div>

<script>
function validateTransfer() {
    const envoyeurSelect = document.getElementById('numEnvoyeur');
    const recepteurSelect = document.getElementById('numRecepteur');
    const montantInput = document.getElementById('montant');
    const submitButton = document.getElementById('submitButton');
    const montantDeviseSpan = document.getElementById('montantDevise');

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

    // Extraire le solde avec parseFloat pour gérer les décimales
    const soldeText = selectedEnvoyeur.textContent.match(/Solde: ([\d,]+\.?\d*)/);
    const soldeEnvoyeur = soldeText ? parseFloat(soldeText[1].replace(',', '')) : 0;
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

    if (!isNaN(montant)) {
        if (deviseEnvoyeur === "EUR" && montant > soldeEnvoyeur) {
            alert("Solde insuffisant pour l'envoyeur (EUR). Le montant à envoyer est supérieur au solde disponible.");
            submitButton.disabled = true;
            return false;
        }
    }

    return true;
}

document.querySelector('form').addEventListener('submit', function(e) {
    if (!validateTransfer()) {
        e.preventDefault();
    }
});

document.addEventListener('DOMContentLoaded', validateTransfer);
</script>