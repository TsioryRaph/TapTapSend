<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TapTapSend - ${pageTitle}</title>
    <link rel="icon" href="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Crect width='100' height='100' rx='22' fill='%230F172A'/%3E%3Ctext x='50' y='68' font-size='48' font-weight='800' text-anchor='middle' fill='%2310B981' font-family='Arial'%3ETT%3C/text%3E%3C/svg%3E">

    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free@6.5.2/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
</head>
<body>

    <div id="tt-toast-stack"></div>

    <div class="mobile-topbar d-flex d-md-none align-items-center justify-content-between">
        <div class="sidebar-brand">
            <div class="logo-badge">TT</div>
            <span>TapTapSend</span>
        </div>
        <button class="btn-burger" type="button" data-bs-toggle="offcanvas" data-bs-target="#mobileSidebar">
            <i class="fas fa-bars"></i>
        </button>
    </div>

    <div class="offcanvas offcanvas-start text-bg-dark" tabindex="-1" id="mobileSidebar">
        <div class="offcanvas-header sidebar-brand">
            <div class="d-flex align-items-center">
                <div class="logo-badge me-2">TT</div>
                <span>TapTapSend</span>
            </div>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="offcanvas"></button>
        </div>
        <div class="offcanvas-body p-0">
            <ul class="nav">
                <jsp:include page="/WEB-INF/views/partials/nav-links.jsp" />
            </ul>
        </div>
    </div>

    <div class="d-flex">
        <nav class="sidebar d-none d-md-flex" style="width:250px;">
            <div class="sidebar-brand">
                <div class="logo-badge">TT</div>
                <span>TapTapSend</span>
            </div>
            <ul class="nav flex-column">
                <jsp:include page="/WEB-INF/views/partials/nav-links.jsp" />
            </ul>
            <div class="sidebar-footer">© 2026 TapTapSend</div>
        </nav>

        <div class="flex-grow-1" style="min-width:0;">
            <div class="topbar d-none d-md-flex justify-content-between align-items-center">
                <div>
                    <h1>${pageTitle}</h1>
                    <div class="subtitle">Gérez vos opérations en temps réel</div>
                </div>
                <div class="btn-toolbar">
                    <button type="button" class="btn btn-outline-secondary btn-sm">
                        <i class="fas fa-download me-1"></i> Exporter
                    </button>
                </div>
            </div>
            <div class="topbar d-flex d-md-none">
                <h1 style="font-size:18px;">${pageTitle}</h1>
            </div>

            <div class="main-content">
                <jsp:include page="${contentPage}" />
            </div>
        </div>
    </div>

    <div class="modal fade" id="formModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title d-flex align-items-center" id="formModalLabel">
                        <span class="modal-title-icon" id="formModalIcon"><i class="fas fa-pen"></i></span>
                        <span id="formModalText"></span>
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body" id="modalBody"></div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="confirmDeleteModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-body p-4">
                    <div class="confirm-icon-ring">
                        <i class="fas fa-trash"></i>
                    </div>
                    <h5 class="mb-2">Confirmer la suppression</h5>
                    <p class="text-muted mb-4" id="confirmDeleteText">Cette action est irréversible.</p>
                    <div class="d-flex justify-content-center gap-2">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal" id="cancelDeleteBtn">Annuler</button>
                        <button type="button" class="btn btn-danger" id="confirmDeleteBtn">
                            <i class="fas fa-trash me-1"></i>Supprimer
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/jquery@3.7.1/dist/jquery.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

    <script>
        function showToast(message, type) {
            type = type === 'success' || type === 'error' ? type : 'info';
            const icons = { success: 'fa-circle-check', error: 'fa-circle-exclamation', info: 'fa-circle-info' };
            const duration = 4000;

            const stack = document.getElementById('tt-toast-stack');
            const toast = document.createElement('div');
            toast.className = 'tt-toast ' + type;
            toast.innerHTML =
                '<div class="tt-toast-icon"><i class="fas ' + icons[type] + '"></i></div>' +
                '<div class="tt-toast-body"></div>' +
                '<button type="button" class="tt-toast-close"><i class="fas fa-xmark"></i></button>' +
                '<div class="tt-toast-progress" style="animation-duration:' + duration + 'ms;"></div>';
            toast.querySelector('.tt-toast-body').textContent = message;
            stack.appendChild(toast);

            function remove() {
                toast.classList.add('tt-toast-out');
                setTimeout(() => toast.remove(), 200);
            }
            const timer = setTimeout(remove, duration);
            toast.querySelector('.tt-toast-close').addEventListener('click', function() {
                clearTimeout(timer);
                remove();
            });
        }

        $(document).ready(function() {
            const urlParams = new URLSearchParams(window.location.search);
            const success = urlParams.get('success');
            const error = urlParams.get('error');
            if (success) {
                showToast(decodeURIComponent(success.replace(/\+/g, ' ')), "success");
                urlParams.delete('success');
                window.history.replaceState({}, document.title, "?" + urlParams.toString());
            }
            if (error) {
                showToast(decodeURIComponent(error.replace(/\+/g, ' ')), "error");
                urlParams.delete('error');
                window.history.replaceState({}, document.title, "?" + urlParams.toString());
            }

            $(document).on('click', '.btn-add, .btn-edit, .btn-pdf-modal-open', function(e) {
                e.preventDefault();
                const $trigger = $(this);
                const url = $trigger.attr('href');
                const modalTitle = $trigger.data('modal-title') || '';
                const modalIcon = $trigger.data('modal-icon') || 'fa-pen';
                const modalSize = $trigger.data('modal-size');

                $('#formModalText').text(modalTitle);
                $('#formModalIcon').html('<i class="fas ' + modalIcon + '"></i>');
                $('#formModal .modal-dialog').removeClass('modal-sm modal-lg modal-xl').addClass(modalSize ? 'modal-' + modalSize : '');

                $('#modalBody').html(
                    '<div class="skeleton-line" style="width:60%"></div>' +
                    '<div class="skeleton-line" style="width:90%"></div>' +
                    '<div class="skeleton-line" style="width:75%"></div>' +
                    '<div class="skeleton-line" style="width:40%"></div>'
                );
                $('#formModal').modal('show');

                $.ajax({
                    url: url, type: 'GET',
                    success: function(data) { $('#modalBody').html(data); },
                    error: function(xhr) {
                        $('#modalBody').html('<div class="alert alert-danger">Erreur lors du chargement du formulaire : ' + (xhr.responseText || "Requête échouée") + '</div>');
                    }
                });
            });

            $(document).on('submit', '#modalBody form:not(#pdfForm)', function(e) {
                e.preventDefault();
                const form = $(this);
                const $btn = form.find('button[type="submit"]');
                const originalHtml = $btn.html();
                $btn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm me-1"></span> Enregistrement...');

                $.ajax({
                    url: form.attr('action'), type: form.attr('method'), data: form.serialize(), dataType: 'json',
                    success: function(jsonResponse) {
                        if (jsonResponse && jsonResponse.redirect) {
                            window.location.href = jsonResponse.redirect;
                        } else if (jsonResponse && jsonResponse.message) {
                            showToast(jsonResponse.message, "success");
                            $('#formModal').modal('hide');
                            window.location.reload();
                        } else if (jsonResponse && jsonResponse.error) {
                            showToast(jsonResponse.error, "error");
                            $btn.prop('disabled', false).html(originalHtml);
                        } else {
                            $('#modalBody').html(jsonResponse);
                        }
                    },
                    error: function(xhr) {
                        showToast("Erreur interne du serveur : " + (xhr.responseText || "Requête échouée"), "error");
                        $btn.prop('disabled', false).html(originalHtml);
                    }
                });
            });

            /* ===== Confirmation de suppression V2 : nom en gras, bouton avec état "loading" ===== */
            let pendingDelete = null;

            function openDeleteConfirm(name) {
                $('#confirmDeleteText').html('Voulez-vous vraiment supprimer <strong>' + (name || 'cet élément') + '</strong> ? Cette action est irréversible.');
                const $confirmBtn = $('#confirmDeleteBtn');
                $confirmBtn.prop('disabled', false).html('<i class="fas fa-trash me-1"></i>Supprimer');
                $('#confirmDeleteModal').modal('show');
            }

            $(document).on('click', 'a.btn-delete-confirm', function(e) {
                e.preventDefault();
                pendingDelete = { type: 'link', url: $(this).attr('href') };
                openDeleteConfirm($(this).data('delete-name'));
            });

            $(document).on('submit', 'form.btn-delete-confirm', function(e) {
                e.preventDefault();
                pendingDelete = { type: 'form', el: $(this) };
                openDeleteConfirm($(this).data('delete-name'));
            });

            $('#confirmDeleteBtn').on('click', function() {
                if (!pendingDelete) return;
                const $btn = $(this);
                $btn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm me-1"></span> Suppression...');
                $('#cancelDeleteBtn').prop('disabled', true);

                if (pendingDelete.type === 'link') {
                    window.location.href = pendingDelete.url;
                    return;
                }

                const form = pendingDelete.el;
                $.ajax({
                    url: form.attr('action'), type: 'POST', data: form.serialize(), dataType: 'json',
                    success: function(jsonResponse) {
                        if (jsonResponse && jsonResponse.message) {
                            showToast(jsonResponse.message, "success");
                            window.location.reload();
                        } else if (jsonResponse && jsonResponse.error) {
                            showToast(jsonResponse.error, "error");
                            $btn.prop('disabled', false).html('<i class="fas fa-trash me-1"></i>Supprimer');
                            $('#cancelDeleteBtn').prop('disabled', false);
                        }
                    },
                    error: function(xhr) {
                        showToast("Erreur lors de la suppression : " + (xhr.responseText || "Requête échouée"), "error");
                        $btn.prop('disabled', false).html('<i class="fas fa-trash me-1"></i>Supprimer');
                        $('#cancelDeleteBtn').prop('disabled', false);
                    },
                    complete: function() {
                        $('#confirmDeleteModal').modal('hide');
                        pendingDelete = null;
                    }
                });
            });
        });
    </script>
</body>
</html>