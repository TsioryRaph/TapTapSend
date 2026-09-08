<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="fr_FR" scope="session"/>

<div class="row g-3 mb-4">
    <div class="col-md-4">
        <div class="stat-card">
            <div class="stat-icon bg-indigo"><i class="fas fa-sack-dollar"></i></div>
            <div>
                <div class="stat-value"><fmt:formatNumber value="${totalFrais}" type="currency" currencyCode="EUR"/></div>
                <div class="stat-label">Recette totale (frais perçus)</div>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="stat-card">
            <div class="stat-icon bg-teal"><i class="fas fa-users"></i></div>
            <div>
                <div class="stat-value">${totalClients}</div>
                <div class="stat-label">Clients enregistrés</div>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="stat-card">
            <div class="stat-icon bg-amber"><i class="fas fa-paper-plane"></i></div>
            <div>
                <div class="stat-value">${totalEnvois}</div>
                <div class="stat-label">Transferts effectués</div>
            </div>
        </div>
    </div>
</div>

<div class="row g-3 mb-3">
    <div class="col-12">
        <div class="card">
            <div class="card-header">Activité des 7 derniers jours</div>
            <div class="card-body">
                <canvas id="transfersChart" height="90"></canvas>
            </div>
        </div>
    </div>
</div>

<div class="row g-3">
    <div class="col-md-4">
        <div class="card h-100">
            <div class="card-header">Actions rapides</div>
            <div class="card-body d-flex flex-column gap-2">
                <a href="${pageContext.request.contextPath}/envoyer" class="btn btn-primary">
                    <i class="fas fa-paper-plane me-1"></i> Nouvel envoi
                </a>
                <a href="${pageContext.request.contextPath}/clients" class="btn btn-outline-secondary">
                    <i class="fas fa-user-plus me-1"></i> Nouveau client
                </a>
                <a href="${pageContext.request.contextPath}/taux" class="btn btn-outline-secondary">
                    <i class="fas fa-exchange-alt me-1"></i> Gérer les taux
                </a>
            </div>
        </div>
    </div>

    <div class="col-md-8">
        <div class="card h-100">
            <div class="card-header">Derniers transferts</div>
            <div class="card-body p-0">
                <c:choose>
                    <c:when test="${not empty dernierEnvois}">
                        <div class="table-responsive">
                            <table class="table table-modern mb-0">
                                <thead>
                                    <tr>
                                        <th>Envoyeur</th>
                                        <th>Récepteur</th>
                                        <th>Montant</th>
                                        <th>Date</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${dernierEnvois}" var="envoi">
                                        <tr>
                                            <td data-label="Envoyeur"><c:out value="${envoi.envoyeur.nom}"/></td>
                                            <td data-label="Récepteur"><c:out value="${envoi.recepteur.nom}"/></td>
                                            <td data-label="Montant" class="badge-amount positive">
                                                <fmt:formatNumber value="${envoi.montant}" type="currency" currencyCode="${envoi.envoyeur.devise}"/>
                                            </td>
                                            <td data-label="Date"><fmt:formatDate value="${envoi.displayDate}" pattern="dd/MM/yyyy HH:mm"/></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-state">
                            <i class="fas fa-inbox"></i>
                            <p class="mb-0">Aucun transfert pour le moment</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>
<script>
(function() {
    const chartData = JSON.parse('${chartDataJson}');
    const ctx = document.getElementById('transfersChart');
    if (!ctx) return;

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: chartData.labels,
            datasets: [{
                label: 'Transferts',
                data: chartData.counts,
                backgroundColor: '#10B981',
                borderRadius: 6,
                maxBarThickness: 42
            }]
        },
        options: {
            responsive: true,
            plugins: { legend: { display: false } },
            scales: {
                y: { beginAtZero: true, ticks: { stepSize: 1, precision: 0 }, grid: { color: '#E2E8F0' } },
                x: { grid: { display: false } }
            }
        }
    });
})();
</script>