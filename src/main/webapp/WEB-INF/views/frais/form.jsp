<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<form id="modalForm" method="post" action="${pageContext.request.contextPath}/frais">
    <div class="row g-3">
        <div class="col-12">
            <div class="tt-field">
                <input type="text" class="form-control" id="idfrais" name="idfrais" placeholder=" " value="${frais.idfrais}" required>
                <label for="idfrais">ID Frais</label>
            </div>
        </div>
        <div class="col-md-6">
            <div class="tt-field">
                <input type="number" class="form-control" id="montant1" name="montant1" placeholder=" "
                       value="${frais.montant1}" step="0.01" required>
                <label for="montant1">Montant min (Euro)</label>
            </div>
        </div>
        <div class="col-md-6">
            <div class="tt-field">
                <input type="number" class="form-control" id="montant2" name="montant2" placeholder=" "
                       value="${frais.montant2}" step="0.01" required>
                <label for="montant2">Montant max (Euro)</label>
            </div>
        </div>
        <div class="col-12">
            <div class="tt-field">
                <input type="number" class="form-control" id="frais" name="frais" placeholder=" "
                       value="${frais.frais}" step="0.01" required>
                <label for="frais">Frais appliqués (Euro)</label>
            </div>
        </div>
    </div>

    <div class="modal-footer-actions">
        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Annuler</button>
        <button type="submit" class="btn btn-primary"><i class="fas fa-check me-1"></i>Enregistrer</button>
    </div>
</form>