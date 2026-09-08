<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="fr_FR" scope="session"/>

<div class="card">
    <div class="card-header d-flex justify-content-between align-items-center">
        <h5 class="mb-0">Taux de change</h5>
        <a href="${pageContext.request.contextPath}/taux?action=edit" class="btn btn-primary btn-sm btn-add"
           data-bs-toggle="modal" data-bs-target="#formModal"
           data-modal-title="Ajouter un taux de change" data-modal-icon="fa-plus">
            <i class="fas fa-plus me-1"></i> Ajouter
        </a>
    </div>
    <div class="card-body">
        <div class="table-responsive">
            <table class="table table-modern">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>1 EUR équivaut à</th>
                        <th class="text-end">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${tauxList}" var="taux">
                        <tr>
                            <td data-label="ID"><span class="badge-country">#<c:out value="${taux.idtaux}"/></span></td>
                            <td data-label="Équivalence">
                                <div class="d-flex align-items-center gap-2">
                                    <span class="badge-amount positive">
                                        <fmt:formatNumber value="${taux.montant1}" type="currency" currencyCode="EUR"/>
                                    </span>
                                    <i class="fas fa-arrow-right text-muted"></i>
                                    <span class="badge-amount positive">
                                        <fmt:formatNumber value="${taux.montant2}" type="currency" currencyCode="MGA"/>
                                    </span>
                                </div>
                            </td>
                            <td class="actions-td text-end">
                                <a href="${pageContext.request.contextPath}/taux?action=edit&idtaux=${taux.idtaux}"
                                   class="btn btn-sm btn-outline-secondary btn-edit"
                                   data-bs-toggle="modal" data-bs-target="#formModal"
                                   data-modal-title="Modifier le taux" data-modal-icon="fa-pen" title="Modifier">
                                    <i class="fas fa-pen"></i>
                                </a>
                                <a href="${pageContext.request.contextPath}/taux?action=delete&idtaux=${taux.idtaux}"
                                   class="btn btn-sm btn-outline-danger btn-delete-confirm"
                                   data-delete-name="ce taux" title="Supprimer">
                                    <i class="fas fa-trash"></i>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

            <c:if test="${empty tauxList}">
                <div class="empty-state">
                    <i class="fas fa-exchange-alt"></i>
                    <p class="mb-0">Aucun taux de change défini.</p>
                </div>
            </c:if>
        </div>
    </div>
</div>