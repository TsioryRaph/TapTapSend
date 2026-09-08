<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="fr_FR" scope="session"/>

<div class="card mb-3">
    <div class="card-body">
        <form method="get" action="${pageContext.request.contextPath}/envoyer" class="row g-2 align-items-end">
            <input type="hidden" name="action" value="search">
            <div class="col-md-4">
                <label for="date" class="form-label">Rechercher par date</label>
                <input type="date" class="form-control" id="date" name="date" required>
            </div>
            <div class="col-md-2">
                <button type="submit" class="btn btn-primary w-100">
                    <i class="fas fa-search me-1"></i> Rechercher
                </button>
            </div>
            <div class="col-md-6 text-md-end">
                <a href="${pageContext.request.contextPath}/envoyer?action=add" class="btn btn-primary btn-add"
                   data-bs-toggle="modal" data-bs-target="#formModal"
                   data-modal-title="Nouvel envoi" data-modal-icon="fa-paper-plane" data-modal-size="lg">
                    <i class="fas fa-plus me-1"></i> Nouvel envoi
                </a>
                <a href="${pageContext.request.contextPath}/envoyer?action=showPdfForm" class="btn btn-outline-secondary btn-pdf-modal-open"
                   data-bs-toggle="modal" data-bs-target="#formModal"
                   data-modal-title="Générer Relevé PDF" data-modal-icon="fa-file-pdf">
                    <i class="fas fa-file-pdf me-1"></i> Relevé PDF
                </a>
            </div>
        </form>
    </div>
</div>

<div class="card">
    <div class="card-header d-flex justify-content-between align-items-center flex-wrap gap-2">
        <h5 class="mb-0">Historique des opérations</h5>
        <div class="d-flex align-items-center gap-2">
            <div class="input-group" style="max-width:280px;">
                <span class="input-group-text bg-white border-end-0"><i class="fas fa-search text-muted"></i></span>
                <input type="text" id="liveSearchEnvoi" class="form-control search-input border-start-0"
                       placeholder="Filtrer par nom, raison...">
            </div>
            <span class="results-count" id="resultsCountEnvoi"></span>
        </div>
    </div>
    <div class="card-body p-0">
        <c:choose>
            <c:when test="${not empty envois}">
                <div class="table-responsive">
                    <table class="table table-modern mb-0" id="envoisTable">
                        <thead>
                            <tr>
                                <th>Envoyeur</th>
                                <th>Récepteur</th>
                                <th class="sortable" data-sort="montant">Montant <i class="fas fa-sort sort-icon"></i></th>
                                <th class="sortable" data-sort="date">Date <i class="fas fa-sort sort-icon"></i></th>
                                <th>Raison</th>
                                <th class="text-end">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${envois}" var="envoi">
                                <tr class="envoi-row"
                                    data-envoyeur="<c:out value='${fn:toLowerCase(envoi.envoyeur.nom)}'/>"
                                    data-recepteur="<c:out value='${fn:toLowerCase(envoi.recepteur.nom)}'/>"
                                    data-raison="<c:out value='${fn:toLowerCase(envoi.raison)}'/>"
                                    data-montant="${envoi.montant}"
                                    data-date="<fmt:formatDate value='${envoi.displayDate}' pattern='yyyyMMddHHmm'/>">
                                    <td data-label="Envoyeur"><c:out value="${envoi.envoyeur.nom}"/> <small class="text-muted">(${envoi.envoyeur.numtel})</small></td>
                                    <td data-label="Récepteur"><c:out value="${envoi.recepteur.nom}"/> <small class="text-muted">(${envoi.recepteur.numtel})</small></td>
                                    <td data-label="Montant" class="badge-amount positive">
                                        <fmt:formatNumber value="${envoi.montant}" type="currency" currencyCode="${envoi.envoyeur.devise}"/>
                                    </td>
                                    <td data-label="Date"><fmt:formatDate value="${envoi.displayDate}" pattern="dd/MM/yyyy HH:mm"/></td>
                                    <td data-label="Raison" class="text-muted"><c:out value="${envoi.raison}"/></td>
                                    <td class="actions-td text-end">
                                        <a href="${pageContext.request.contextPath}/envoyer?action=edit&id=${envoi.idEnv}"
                                           class="btn btn-sm btn-outline-secondary btn-edit"
                                           data-bs-toggle="modal" data-bs-target="#formModal"
                                           data-modal-title="Modifier un envoi" data-modal-icon="fa-pen" data-modal-size="lg" title="Modifier">
                                            <i class="fas fa-pen"></i>
                                        </a>
                                        <form class="btn-delete-confirm d-inline" action="${pageContext.request.contextPath}/envoyer" method="post"
                                              data-delete-name="cet envoi">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${envoi.idEnv}">
                                            <button type="submit" class="btn btn-sm btn-outline-danger" title="Supprimer">
                                                <i class="fas fa-trash-alt"></i>
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>

                    <div class="empty-state" id="noResultsEnvoi" style="display:none;">
                        <i class="fas fa-magnifying-glass"></i>
                        <p class="mb-0">Aucun envoi ne correspond à votre recherche.</p>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    <i class="fas fa-paper-plane"></i>
                    <p class="mb-0">Aucune opération d'envoi trouvée.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<script>
(function() {
    const searchInput = document.getElementById('liveSearchEnvoi');
    const rows = Array.from(document.querySelectorAll('#envoisTable .envoi-row'));
    const resultsCount = document.getElementById('resultsCountEnvoi');
    const noResultsState = document.getElementById('noResultsEnvoi');
    const tbody = document.querySelector('#envoisTable tbody');
    if (!searchInput) return;

    function updateCount() {
        const visible = rows.filter(r => r.style.display !== 'none').length;
        resultsCount.textContent = visible + ' / ' + rows.length;
        noResultsState.style.display = (visible === 0) ? 'block' : 'none';
    }

    searchInput.addEventListener('input', function() {
        const term = this.value.trim().toLowerCase();
        rows.forEach(function(row) {
            const haystack = row.dataset.envoyeur + ' ' + row.dataset.recepteur + ' ' + row.dataset.raison;
            row.style.display = haystack.includes(term) ? '' : 'none';
        });
        updateCount();
    });
    updateCount();

    let currentSort = { key: null, asc: true };
    document.querySelectorAll('#envoisTable .sortable').forEach(function(th) {
        th.addEventListener('click', function() {
            const key = th.dataset.sort;
            const asc = currentSort.key === key ? !currentSort.asc : false;
            currentSort = { key: key, asc: asc };

            document.querySelectorAll('#envoisTable .sortable').forEach(h => h.classList.remove('sort-active'));
            th.classList.add('sort-active');
            th.querySelector('.sort-icon').className = 'fas ' + (asc ? 'fa-sort-up' : 'fa-sort-down') + ' sort-icon';

            const sorted = rows.slice().sort(function(a, b) {
                const va = parseFloat(a.dataset[key]), vb = parseFloat(b.dataset[key]);
                return asc ? va - vb : vb - va;
            });
            sorted.forEach(row => tbody.appendChild(row));
        });
    });
})();
</script>