<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="row">

    <div class="col-md-6">
        <div class="card mb-4">
            <div class="card-header">
                <h5>Recette totale</h5>
            </div>
            <div class="card-body">
                <h3 class="card-title">
                    <fmt:formatNumber value="${totalFrais}" type="currency" currencyCode="EUR"/>
                </h3>
                <p class="card-text">Total des frais d'envoi perçus</p>
            </div>
        </div>
    </div>
    <div class="col-md-6">
        <div class="card mb-4">
            <div class="card-header">
                <h5>Actions rapides</h5>
            </div>
            <div class="card-body">
                <a href="${pageContext.request.contextPath}/envoyer" class="btn btn-primary me-2">
                    <i class="fas fa-paper-plane me-1"></i> Nouvel envoi
                </a>
                <a href="${pageContext.request.contextPath}/clients" class="btn btn-success me-2">
                    <i class="fas fa-user-plus me-1"></i> Nouveau client
                </a>
            </div>
        </div>
    </div>
</div>

<div class="row mt-4">
    <div class="col-12">
        <div class="card">
            <div class="card-header">
                <h5>Derniers transferts</h5>
            </div>
            <div class="card-body">
                <p>Statistiques et graphiques peuvent être ajoutés ici</p>
            </div>
        </div>
    </div>
</div>