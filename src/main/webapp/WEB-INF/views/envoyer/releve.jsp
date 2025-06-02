<div class="card">
    <div class="card-header">
        <h5>Relevé d'opérations</h5>
    </div>
    <div class="card-body">
        <div class="mb-4">
            <h4>Date :
                <fmt:formatDate value="${java.time.YearMonth.of(year, month).atDay(1)}" pattern="MMMM yyyy"/>
            </h4>
            <p>Contact : ${client.numtel}</p>
            <p>${client.nom}</p>
            <p>${client.sexe}</p>
        </div>

        <div class="mb-4">
            <h5>Solde actuel :
                <fmt:formatNumber value="${client.solde}" type="currency" currencyCode="EUR"/>
            </h5>
        </div>

        <div class="table-responsive mb-4">
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Raison</th>
                        <th>Nom du récepteur</th>
                        <th>Montant</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${envois}" var="envoi">
                        <tr>
                            <td>
                                <fmt:formatDate value="${envoi.date}" pattern="dd/MM/yyyy"/>
                            </td>
                            <td>${envoi.raison}</td>
                            <td>${envoi.recepteur.nom}</td>
                            <td>
                                <fmt:formatNumber value="${envoi.montant}" type="currency" currencyCode="EUR"/>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>

        <div class="text-end">
            <h5>Total Débit :
                <fmt:formatNumber
                    value="${envois.stream().map(e -> e.montant).reduce(0, Integer::sum)}"
                    type="currency" currencyCode="EUR"/>
            </h5>
        </div>

        <div class="d-flex justify-content-between mt-4">
            <a href="${pageContext.request.contextPath}/envoyer" class="btn btn-secondary">
                <i class="fas fa-arrow-left me-1"></i> Retour
            </a>
            <a href="${pageContext.request.contextPath}/envoyer?action=pdf&numtel=${client.numtel}&month=${month}&year=${year}"
               class="btn btn-primary">
                <i class="fas fa-file-pdf me-1"></i> Générer PDF
            </a>
        </div>
    </div>
</div>