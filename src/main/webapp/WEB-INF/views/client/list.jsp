<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%-- Définir la locale pour le formatage monétaire (important pour fmt:formatNumber) --%>
<fmt:setLocale value="fr_FR" scope="session"/>

<div class="card">
    <div class="card-header d-flex justify-content-between align-items-center">
        <h5>Liste des clients</h5>
        <div>
            <%-- Bouton Ajouter : utilise la classe btn-add pour ouvrir la modale --%>
            <a href="${pageContext.request.contextPath}/clients?action=edit" class="btn btn-primary btn-sm btn-add"
               data-bs-toggle="modal" data-bs-target="#formModal" data-modal-title="Ajouter un nouveau client">
                <i class="fas fa-plus me-1"></i> Ajouter
            </a>
        </div>
    </div>
    <div class="card-body">
        <%-- LIGNES DE DÉBOGAGE (vous pouvez les supprimer une fois que tout fonctionne) --%>
        <%-- <p>Nombre de clients dans la liste : <c:out value="${clients.size()}" default="0"/></p> --%>
        <%-- FIN DES LIGNES DE DÉBOGAGE --%>

        <form method="get" action="${pageContext.request.contextPath}/clients" class="mb-3">
            <input type="hidden" name="action" value="search">
            <div class="input-group">
                <input type="text" name="searchTerm" class="form-control" placeholder="Rechercher un client...">
                <button type="submit" class="btn btn-outline-secondary">
                    <i class="fas fa-search"></i>
                </button>
            </div>
        </form>

        <div class="table-responsive">
            <table class="table table-striped table-hover">
                <thead>
                    <tr>
                        <th>Numéro</th>
                        <th>Nom</th>
                        <th>Sexe</th>
                        <th>Pays</th>
                        <th>Solde</th>
                        <th>Email</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${clients}" var="client">
                        <tr>
                            <td>${client.numtel}</td>
                            <td>${client.nom}</td>
                            <td>${client.sexe}</td>
                            <td>${client.pays}</td>
                            <td>
                                <%-- C'EST LA MODIFICATION CLÉ ICI : Utilisation de client.devise --%>
                                <fmt:formatNumber value="${client.solde}" type="currency" currencyCode="${client.devise}"/>
                            </td>
                            <td>${client.mail}</td>
                            <td>
                                <%-- Bouton Modifier : utilise la classe btn-edit pour ouvrir la modale --%>
                                <a href="${pageContext.request.contextPath}/clients?action=edit&numtel=${client.numtel}"
                                   class="btn btn-sm btn-outline-primary btn-edit"
                                   data-bs-toggle="modal" data-bs-target="#formModal" data-modal-title="Modifier client">
                                    <i class="fas fa-edit"></i>
                                    modifier
                                </a>
                                <a href="${pageContext.request.contextPath}/clients?action=delete&numtel=${client.numtel}"
                                   class="btn btn-sm btn-outline-danger"
                                   onclick="return confirm('Êtes-vous sûr de vouloir supprimer ce client?');">
                                    <i class="fas fa-trash"></i>
                                    supprimer
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty clients}">
                        <tr>
                            <td colspan="7" class="text-center">Aucun client trouvé.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>