<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><div class="card">

<div class="card">
    <div class="card-header d-flex justify-content-between align-items-center">
        <h5>Frais d'envoi</h5>
        <div>
            <a href="${pageContext.request.contextPath}/frais?action=edit" class="btn btn-primary btn-sm btn-add">
                <i class="fas fa-plus me-1"></i> Ajouter
            </a>
        </div>
    </div>
    <div class="card-body">
        <div class="table-responsive">
            <table class="table table-striped table-hover">
                <thead>
                    <tr>
                        <th>ID Frais</th>
                        <th>Montant 1</th>
                        <th>Montant 2</th>
                        <th>Frais</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${fraisList}" var="frais">
                        <tr>
                            <td>${frais.idfrais}</td>
                            <td>
                                <fmt:formatNumber value="${frais.montant1}" type="currency" currencyCode="EUR"/>
                            </td>
                            <td>
                                <fmt:formatNumber value="${frais.montant2}" type="currency" currencyCode="EUR"/>
                            </td>
                            <td>
                                <fmt:formatNumber value="${frais.frais}" type="currency" currencyCode="EUR"/>
                            </td>
                            <td>
                                <a href="${pageContext.request.contextPath}/frais?action=edit&idfrais=${frais.idfrais}"
                                   class="btn btn-sm btn-outline-primary btn-edit">
                                    <i class="fas fa-edit"></i>
                                    modifier
                                </a>
                                <a href="${pageContext.request.contextPath}/frais?action=delete&idfrais=${frais.idfrais}"
                                   class="btn btn-sm btn-outline-danger"
                                   onclick="return confirm('Êtes-vous sûr de vouloir supprimer ces frais?');">
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