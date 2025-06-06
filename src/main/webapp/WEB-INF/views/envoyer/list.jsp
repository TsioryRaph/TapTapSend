<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%-- Définir la locale pour le formatage monétaire (important pour fmt:formatNumber) --%>
<fmt:setLocale value="fr_FR" scope="session"/>

<div class="card">
    <div class="card-header d-flex justify-content-between align-items-center">
        <h5>Recherche d'opérations par date</h5>
        <div>
            <a href="${pageContext.request.contextPath}/envoyer?action=add" class="btn btn-primary btn-sm btn-add"
               data-bs-toggle="modal" data-bs-target="#formModal" data-modal-title="Nouvel envoi">
                <i class="fas fa-plus me-1"></i> Nouvel envoi
            </a>
            <%-- CORRECTION ICI : Modifier le href pour pointer vers le servlet qui gère l'action "showPdfForm" --%>
            <a href="${pageContext.request.contextPath}/envoyer?action=showPdfForm" class="btn btn-secondary btn-sm btn-pdf-modal-open"
               data-bs-toggle="modal" data-bs-target="#formModal" data-modal-title="Générer Relevé PDF">
                <i class="fas fa-file-pdf me-1"></i> Générer Relevé PDF
            </a>
        </div>
    </div>
    <div class="card-body">
        <form method="get" action="${pageContext.request.contextPath}/envoyer" class="mb-3">
            <input type="hidden" name="action" value="search">
            <div class="row">
                <div class="col-md-6">
                    <label for="date" class="form-label">Date</label>
                    <input type="date" class="form-control" id="date" name="date" required>
                </div>
                <div class="col-md-6 d-flex align-items-end">
                    <button type="submit" class="btn btn-primary">
                        <i class="fas fa-search me-1"></i> Rechercher
                    </button>
                </div>
            </div>
        </form>

        <c:if test="${not empty envois}">
            <div class="table-responsive">
                <table class="table table-striped table-hover">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Envoyeur</th>
                            <th>Récepteur</th>
                            <th>Montant</th>
                            <th>Date</th>
                            <th>Raison</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${envois}" var="envoi">
                            <tr>
                                <td>${envoi.idEnv}</td>
                                <td>${envoi.envoyeur.nom} (${envoi.envoyeur.numtel})</td>
                                <td>${envoi.recepteur.nom} (${envoi.recepteur.numtel})</td>
                                <td>
                                    <fmt:formatNumber value="${envoi.montant}" type="currency" currencyCode="${envoi.envoyeur.devise}"/>
                                </td>
                                <td>
                                    <fmt:formatDate value="${envoi.displayDate}" pattern="dd/MM/yyyy HH:mm"/>
                                </td>
                                <td>${envoi.raison}</td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/envoyer?action=edit&id=${envoi.idEnv}"
                                       class="btn btn-warning btn-sm btn-edit"
                                       data-bs-toggle="modal" data-bs-target="#formModal" data-modal-title="Modifier un envoi"
                                       title="Modifier">
                                         <i class="fas fa-edit"></i>
                                         modifier
                                     </a>
                                    <form class="delete-envoi-form" action="${pageContext.request.contextPath}/envoyer" method="post" style="display:inline;">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${envoi.idEnv}">
                                        <button type="submit" class="btn btn-danger btn-sm" title="Supprimer">
                                            <i class="fas fa-trash-alt"></i>
                                            supprimer
                                        </button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:if>
        <c:if test="${empty envois}">
            <div class="alert alert-info text-center" role="alert">
                Aucune opération d'envoi trouvée.
            </div>
        </c:if>
    </div>
</div>