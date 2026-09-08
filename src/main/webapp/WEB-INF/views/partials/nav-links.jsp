<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<li class="nav-item">
    <a class="nav-link ${pageName eq 'dashboard' ? 'active' : ''}" href="${pageContext.request.contextPath}/dashboard">
        <i class="fas fa-chart-pie me-2"></i>Tableau de bord
    </a>
</li>
<li class="nav-item">
    <a class="nav-link ${pageName eq 'clients' ? 'active' : ''}" href="${pageContext.request.contextPath}/clients">
        <i class="fas fa-users me-2"></i>Clients
    </a>
</li>
<li class="nav-item">
    <a class="nav-link ${pageName eq 'envoyer' ? 'active' : ''}" href="${pageContext.request.contextPath}/envoyer">
        <i class="fas fa-paper-plane me-2"></i>Envoyer argent
    </a>
</li>
<li class="nav-item">
    <a class="nav-link ${pageName eq 'taux' ? 'active' : ''}" href="${pageContext.request.contextPath}/taux">
        <i class="fas fa-exchange-alt me-2"></i>Taux de change
    </a>
</li>
<li class="nav-item">
    <a class="nav-link ${pageName eq 'frais' ? 'active' : ''}" href="${pageContext.request.contextPath}/frais">
        <i class="fas fa-money-bill-wave me-2"></i>Frais d'envoi
    </a>
</li>