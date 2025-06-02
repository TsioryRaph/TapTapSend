<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="card">
    <div class="card-header d-flex justify-content-between align-items-center">
        <h5>Recherche d'opérations par date</h5>
        <div>
            <a href="${pageContext.request.contextPath}/envoyer?action=add" class="btn btn-primary btn-sm btn-add">
                <i class="fas fa-plus me-1"></i> Nouvel envoi
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
                            <th>Actions</th> <%-- NOUVELLE COLONNE POUR LES ACTIONS --%>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${envois}" var="envoi">
                            <tr>
                                <td>${envoi.idEnv}</td>
                                <td>${envoi.envoyeur.nom} (${envoi.envoyeur.numtel})</td>
                                <td>${envoi.recepteur.nom} (${envoi.recepteur.numtel})</td>
                                <td>
                                    <fmt:formatNumber value="${envoi.montant}" type="currency" currencyCode="EUR"/>
                                </td>
                                <td>
                                    <fmt:formatDate value="${envoi.displayDate}" pattern="dd/MM/yyyy HH:mm"/>
                                </td>
                                <td>${envoi.raison}</td>
                                <td>
                                    <%-- Bouton Modifier --%>
                                    <a href="${pageContext.request.contextPath}/envoyer?action=edit&id=${envoi.idEnv}"
                                       class="btn btn-warning btn-sm" title="Modifier">
                                        <i class="fas fa-edit"></i>
                                    </a>
                                    <%-- Bouton Supprimer --%>
                                    <%-- Utilisation d'un formulaire pour la suppression est plus sécurisée (méthode POST) --%>
                                    <form action="${pageContext.request.contextPath}/envoyer" method="post" style="display:inline;"
                                          onsubmit="return confirm('Êtes-vous sûr de vouloir supprimer cet envoi ?');">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${envoi.idEnv}">
                                        <button type="submit" class="btn btn-danger btn-sm" title="Supprimer">
                                            <i class="fas fa-trash-alt"></i>
                                        </button>
                                    </form>
                                </td> <%-- FIN DE LA NOUVELLE COLONNE --%>
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