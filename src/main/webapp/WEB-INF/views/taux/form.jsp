<form id="modalForm" method="post" action="${pageContext.request.contextPath}/taux">
    <div class="mb-3">
        <label for="idtaux" class="form-label">ID Taux</label>
        <input type="text" class="form-control" id="idtaux" name="idtaux"
               value="${taux.idtaux}" required>
    </div>
    <div class="mb-3">
        <label for="montant1" class="form-label">Montant 1 (Euro)</label>
        <input type="number" class="form-control" id="montant1" name="montant1"
               value="${taux.montant1}" required>
    </div>
    <div class="mb-3">
        <label for="montant2" class="form-label">Montant 2 (Ariary)</label>
        <input type="number" class="form-control" id="montant2" name="montant2"
               value="${taux.montant2}" required>
    </div>
    <div class="d-flex justify-content-end">
        <button type="button" class="btn btn-secondary me-2" data-bs-dismiss="modal">Annuler</button>
        <button type="submit" class="btn btn-primary">Enregistrer</button>
    </div>
</form>