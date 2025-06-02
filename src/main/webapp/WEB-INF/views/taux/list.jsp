<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><div class="card">

    <div class="card-header d-flex justify-content-between align-items-center">
        <h5>Taux de change</h5>
        <div>
            <a href="${pageContext.request.contextPath}/taux?action=edit" class="btn btn-primary btn-sm btn-add">
                <i class="fas fa-plus me-1"></i> Ajouter
            </a>
        </div>
    </div>
    <div class="card-body">
        <div class="table-responsive">
            <table class="table table-striped table-hover">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Montant 1</th>
                        <th>Montant 2</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${tauxList}" var="taux">
                        <tr>
                            <td>${taux.idtaux}</td>
                            <td>${taux.montant1}</td>
                            <td>${taux.montant2}</td>
                            <td>
                                <a href="${pageContext.request.contextPath}/taux?action=edit&idtaux=${taux.idtaux}"
                                   class="btn btn-sm btn-outline-primary btn-edit">
                                    <i class="fas fa-edit"></i>
                                    modifier
                                </a>
                                <a href="${pageContext.request.contextPath}/taux?action=delete&idtaux=${taux.idtaux}"
                                   class="btn btn-sm btn-outline-danger"
                                   onclick="return confirm('Êtes-vous sûr de vouloir supprimer ce taux?');">
                                    <i class="fas fa-trash"></i>
                                    supprimer
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>