<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %> <%-- Nécessaire pour formater la date --%>

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
                <input type="hidden" name="action" value="update"> <%-- Indique que c'est une mise à jour --%>
                <input type="hidden" name="idEnv" value="${oldEnvoi.idEnv}"> <%-- Envoie l'ID pour la mise à jour --%>
            </c:if>
            <c:if test="${empty oldEnvoi || empty oldEnvoi.idEnv}">
                <input type="hidden" name="action" value="create"> <%-- Indique que c'est une création --%>
            </c:if>

            <div class="mb-3">
                <label for="numEnvoyeur" class="form-label">Envoyeur</label>
                <select class="form-select" id="numEnvoyeur" name="numEnvoyeur" required>
                    <option value="">-- Sélectionnez l'envoyeur --</option>
                    <c:forEach items="${clients}" var="client">
                        <option value="${client.numtel}"
                                <c:if test="${not empty oldEnvoi && oldEnvoi.envoyeur.numtel eq client.numtel}">selected</c:if>
                        >
                            ${client.nom} (${client.numtel}) - Solde: ${client.solde} EUR - Pays: ${client.pays}
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="mb-3">
                <label for="numRecepteur" class="form-label">Récepteur</label>
                <select class="form-select" id="numRecepteur" name="numRecepteur" required>
                    <option value="">-- Sélectionnez le récepteur --</option>
                    <c:forEach items="${clients}" var="client">
                        <option value="${client.numtel}"
                                <c:if test="${not empty oldEnvoi && oldEnvoi.recepteur.numtel eq client.numtel}">selected</c:if>
                        >
                            ${client.nom} (${client.numtel}) - Pays: ${client.pays}
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="mb-3">
                <label for="montant" class="form-label">Montant (EUR)</label>
                <input type="number" class="form-control" id="montant" name="montant" required min="1"
                       value="${not empty oldEnvoi ? oldEnvoi.montant : ''}">
            </div>

            <%-- NOUVEAU CHAMP POUR LA DATE --%>
            <div class="mb-3">
                <label for="dateTransfert" class="form-label">Date et Heure du transfert</label>
                <%-- Utilisation de <fmt:formatDate> pour formater la date de l'objet Envoyer --%>
                <%-- au format attendu par <input type="datetime-local"> (YYYY-MM-DDTHH:MM). --%>
                <%-- Nous utilisons la méthode 'displayDate' de l'objet Envoyer pour la compatibilité. --%>
                <input type="datetime-local" class="form-control" id="dateTransfert" name="date"
                       value="<fmt:formatDate value="${not empty oldEnvoi ? oldEnvoi.displayDate : ''}" pattern="yyyy-MM-dd'T'HH:mm"/>" required>
            </div>

            <div class="mb-3">
                <label for="raison" class="form-label">Raison du transfert</label>
                <input type="text" class="form-control" id="raison" name="raison" required
                       value="${not empty oldEnvoi ? oldEnvoi.raison : ''}">
            </div>

            <button type="submit" class="btn btn-success">
                <c:choose>
                    <c:when test="${not empty oldEnvoi && not empty oldEnvoi.idEnv}">Mettre à jour</c:when>
                    <c:otherwise>Effectuer le transfert</c:otherwise>
                </c:choose>
            </button>
            <a href="${pageContext.request.contextPath}/envoyer" class="btn btn-secondary">Annuler</a>
        </form>
    </div>
</div>
