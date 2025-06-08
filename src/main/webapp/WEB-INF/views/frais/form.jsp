<form id="modalForm" method="post" action="${pageContext.request.contextPath}/frais">
    <div class="mb-3">
        <label for="idfrais" class="form-label">ID Frais</label>
        <input type="text" class="form-control" id="idfrais" name="idfrais"
               value="${frais.idfrais}" required>
    </div>
    <div class="mb-3">
        <label for="montant1" class="form-label">Montant 1 (Euro)</label>
        <input type="number" class="form-control" id="montant1" name="montant1"
               value="${frais.montant1}" step="0.01" required>
    </div>
    <div class="mb-3">
        <label for="montant2" class="form-label">Montant 2 (Euro)</label>
        <input type="number" class="form-control" id="montant2" name="montant2"
               value="${frais.montant2}" step="0.01" required>
    </div>
    <div class="mb-3">
        <label for="frais" class="form-label">Frais (Euro)</label>
        <input type="number" class="form-control" id="frais" name="frais"
               value="${frais.frais}" step="0.01" required>
    </div>
    <div class="d-flex justify-content-end">
        <button type="button" class="btn btn-secondary me-2" data-bs-dismiss="modal">Annuler</button>
        <button type="submit" class="btn btn-primary">Enregistrer</button>
    </div>
</form>