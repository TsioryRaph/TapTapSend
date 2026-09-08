<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="fr_FR" scope="session"/>

<div class="card">
    <div class="card-header d-flex justify-content-between align-items-center">
        <h5 class="mb-0">Frais d'envoi</h5>
        <a href="${pageContext.request.contextPath}/frais?action=edit" class="btn btn-primary btn-sm btn-add"
           data-bs-toggle="modal" data-bs-target="#formModal"
           data-modal-title="Ajouter une tranche de frais" data-modal-icon="fa-plus">
            <i class="fas fa-plus me-1"></i> Ajouter
        </a>
    </div>
    <div class="card-body">
        <div class="table-responsive">
            <table class="table table-modern">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Tranche de montant</th>
                        <th>Frais appliqués</th>
                        <th class="text-end">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${fraisList}" var="frais">
                        <tr>
                            <td data-label="ID"><span class="badge-country">#<c:out value="${frais.idfrais}"/></span></td>
                            <td data-label="Tranche">
                                de <fmt:formatNumber value="${frais.montant1}" type="currency" currencyCode="EUR"/>
                                à <fmt:formatNumber value="${frais.montant2}" type="currency" currencyCode="EUR"/>
                            </td>
                            <td data-label="Frais" class="badge-amount positive">
                                <fmt:formatNumber value="${frais.frais}" type="currency" currencyCode="EUR"/>
                            </td>
                            <td class="actions-td text-end">
                                <a href="${pageContext.request.contextPath}/frais?action=edit&idfrais=${frais.idfrais}"
                                   class="btn btn-sm btn-outline-secondary btn-edit"
                                   data-bs-toggle="modal" data-bs-target="#formModal"
                                   data-modal-title="Modifier les frais" data-modal-icon="fa-pen" title="Modifier">
                                    <i class="fas fa-pen"></i>
                                </a>
                                <a href="${pageContext.request.contextPath}/frais?action=delete&idfrais=${frais.idfrais}"
                                   class="btn btn-sm btn-outline-danger btn-delete-confirm"
                                   data-delete-name="cette tranche de frais" title="Supprimer">
                                    <i class="fas fa-trash"></i>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

            <c:if test="${empty fraisList}">
                <div class="empty-state">
                    <i class="fas fa-money-bill-wave"></i>
                    <p class="mb-0">Aucune tranche de frais définie.</p>
                </div>
            </c:if>
        </div>
    </div>
</div>