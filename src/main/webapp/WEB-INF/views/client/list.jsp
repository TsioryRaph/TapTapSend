<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="fr_FR" scope="session"/>

<div class="card">
    <div class="card-header d-flex justify-content-between align-items-center">
        <h5 class="mb-0">Liste des clients</h5>
        <a href="${pageContext.request.contextPath}/clients?action=edit" class="btn btn-primary btn-sm btn-add"
           data-bs-toggle="modal" data-bs-target="#formModal"
           data-modal-title="Ajouter un nouveau client" data-modal-icon="fa-user-plus">
            <i class="fas fa-plus me-1"></i> Ajouter
        </a>
    </div>
    <div class="card-body">
        <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
            <div class="input-group" style="max-width:360px;">
                <span class="input-group-text bg-white border-end-0"><i class="fas fa-search text-muted"></i></span>
                <input type="text" id="liveSearch" class="form-control search-input border-start-0"
                       placeholder="Filtrer par nom, téléphone, pays...">
            </div>
            <span class="results-count" id="resultsCount"></span>
        </div>

        <div class="table-responsive">
            <table class="table table-modern" id="clientsTable">
                <thead>
                    <tr>
                        <th class="sortable" data-sort="nom">Client <i class="fas fa-sort sort-icon"></i></th>
                        <th class="sortable" data-sort="sexe">Sexe <i class="fas fa-sort sort-icon"></i></th>
                        <th class="sortable" data-sort="pays">Pays <i class="fas fa-sort sort-icon"></i></th>
                        <th class="sortable" data-sort="solde">Solde <i class="fas fa-sort sort-icon"></i></th>
                        <th>Email</th>
                        <th class="text-end">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${clients}" var="client">
                        <tr class="client-row"
                            data-nom="<c:out value='${fn:toLowerCase(client.nom)}'/>"
                            data-numtel="<c:out value='${client.numtel}'/>"
                            data-pays="<c:out value='${fn:toLowerCase(client.pays)}'/>"
                            data-sexe="<c:out value='${client.sexe}'/>"
                            data-solde="${client.solde}">
                            <td class="person-cell-td">
                                <div class="person-cell">
                                    <div class="avatar-circle">
                                        <c:out value="${fn:toUpperCase(fn:substring(client.nom, 0, 1))}"/>
                                    </div>
                                    <div class="person-meta">
                                        <div><c:out value="${client.nom}"/></div>
                                        <small>
                                            <a href="tel:${client.numtel}" class="phone-link">
                                                <i class="fas fa-phone" style="font-size:10px;"></i> <c:out value="${client.numtel}"/>
                                            </a>
                                            <button type="button" class="copy-btn" title="Copier le numéro"
                                                    onclick="copyPhone('${client.numtel}', this)">
                                                <i class="fas fa-copy"></i>
                                            </button>
                                        </small>
                                    </div>
                                </div>
                            </td>
                            <td data-label="Sexe"><c:out value="${client.sexe}"/></td>
                            <td data-label="Pays"><span class="badge-country"><c:out value="${client.pays}"/></span></td>
                            <td data-label="Solde">
                                <c:choose>
                                    <c:when test="${client.solde <= 0}">
                                        <span class="badge-amount zero">
                                            <fmt:formatNumber value="${client.solde}" type="currency" currencyCode="${client.devise}"/>
                                        </span>
                                    </c:when>
                                    <c:when test="${client.solde < 20}">
                                        <span class="badge-amount low">
                                            <i class="fas fa-triangle-exclamation" style="font-size:11px;"></i>
                                            <fmt:formatNumber value="${client.solde}" type="currency" currencyCode="${client.devise}"/>
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge-amount positive">
                                            <fmt:formatNumber value="${client.solde}" type="currency" currencyCode="${client.devise}"/>
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td data-label="Email" class="text-muted"><c:out value="${client.mail}"/></td>
                            <td class="actions-td text-end">
                                <a href="${pageContext.request.contextPath}/clients?action=edit&numtel=${client.numtel}"
                                   class="btn btn-sm btn-outline-secondary btn-edit"
                                   data-bs-toggle="modal" data-bs-target="#formModal"
                                   data-modal-title="Modifier client" data-modal-icon="fa-pen" title="Modifier">
                                    <i class="fas fa-pen"></i>
                                </a>
                                <a href="${pageContext.request.contextPath}/clients?action=delete&numtel=${client.numtel}"
                                   class="btn btn-sm btn-outline-danger btn-delete-confirm"
                                   data-delete-name="le client <c:out value='${client.nom}'/>" title="Supprimer">
                                    <i class="fas fa-trash"></i>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

            <div class="empty-state" id="noResultsState" style="display:none;">
                <i class="fas fa-user-slash"></i>
                <p class="mb-0">Aucun client ne correspond à votre recherche.</p>
            </div>

            <c:if test="${empty clients}">
                <div class="empty-state">
                    <i class="fas fa-user-slash"></i>
                    <p class="mb-0">Aucun client trouvé.</p>
                </div>
            </c:if>
        </div>
    </div>
</div>

<script>
(function() {
    const searchInput = document.getElementById('liveSearch');
    const rows = Array.from(document.querySelectorAll('#clientsTable .client-row'));
    const resultsCount = document.getElementById('resultsCount');
    const noResultsState = document.getElementById('noResultsState');
    const tbody = document.querySelector('#clientsTable tbody');

    function updateCount() {
        const visible = rows.filter(r => r.style.display !== 'none').length;
        if (rows.length > 0) {
            resultsCount.textContent = visible + ' / ' + rows.length + ' client' + (rows.length > 1 ? 's' : '');
        }
        noResultsState.style.display = (visible === 0 && rows.length > 0) ? 'block' : 'none';
    }

    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const term = this.value.trim().toLowerCase();
            rows.forEach(function(row) {
                const haystack = row.dataset.nom + ' ' + row.dataset.numtel + ' ' + row.dataset.pays;
                row.style.display = haystack.includes(term) ? '' : 'none';
            });
            updateCount();
        });
        updateCount();
    }

    let currentSort = { key: null, asc: true };
    document.querySelectorAll('.sortable').forEach(function(th) {
        th.addEventListener('click', function() {
            const key = th.dataset.sort;
            const asc = currentSort.key === key ? !currentSort.asc : true;
            currentSort = { key: key, asc: asc };

            document.querySelectorAll('.sortable').forEach(h => h.classList.remove('sort-active'));
            th.classList.add('sort-active');
            th.querySelector('.sort-icon').className = 'fas ' + (asc ? 'fa-sort-up' : 'fa-sort-down') + ' sort-icon';

            const sorted = rows.slice().sort(function(a, b) {
                let va = a.dataset[key], vb = b.dataset[key];
                if (key === 'solde') { va = parseFloat(va); vb = parseFloat(vb); }
                if (va < vb) return asc ? -1 : 1;
                if (va > vb) return asc ? 1 : -1;
                return 0;
            });
            sorted.forEach(row => tbody.appendChild(row));
        });
    });

    window.copyPhone = function(numero, btn) {
        navigator.clipboard.writeText(numero).then(function() {
            const icon = btn.querySelector('i');
            icon.className = 'fas fa-check';
            setTimeout(function() { icon.className = 'fas fa-copy'; }, 1200);
        });
    };
})();
</script>