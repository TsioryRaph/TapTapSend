<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TapTapSend - ${pageTitle}</title>

    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css" rel="stylesheet">

    <link href="https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free@6.5.2/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/toastify-js/src/toastify.min.css">

    <%-- <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css"> --%>

    <style>
        .sidebar {
            min-height: 100vh;
            /* Adaptez la couleur de la sidebar au thème Solar */
            /* Utilisez une variable CSS de Bootswatch Solar pour une meilleure intégration */
            background-color: var(--bs-gray-dark); /* Couleur gris foncé de Solar : #073642 */
            /* Ou si vous préférez une couleur d'accent du thème, par exemple : */
            /* background-color: var(--bs-blue); */ /* Pour le bleu/doré du thème Solar : #b58900 */
        }
        .sidebar .nav-link {
            color: rgba(255, 255, 255, 0.75);
        }
        .sidebar .nav-link:hover {
            color: rgba(255, 255, 255, 1);
        }
        .sidebar .nav-link.active {
            color: white;
            /* Ajustez la couleur de fond pour l'élément actif */
            background-color: rgba(181, 137, 0, 0.2); /* Un doré/bleu plus transparent de Solar */
        }
    </style>
</head>
<body>
    <div class="container-fluid">
        <div class="row">
            <div class="col-md-2 d-none d-md-block sidebar p-0">
                <div class="p-3 text-white">
                    <h4>TapTapSend</h4>
                </div>
                <ul class="nav flex-column">
                    <li class="nav-item">
                        <a class="nav-link ${pageName eq 'dashboard' ? 'active' : ''}" href="${pageContext.request.contextPath}/dashboard">
                            <i class="fas fa-tachometer-alt me-2"></i>Dashboard
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link ${pageName eq 'clients' ? 'active' : ''}" href="${pageContext.request.contextPath}/clients">
                            <i class="fas fa-users me-2"></i>Clients
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
                    <li class="nav-item">
                        <a class="nav-link ${pageName eq 'envoyer' ? 'active' : ''}" href="${pageContext.request.contextPath}/envoyer">
                            <i class="fas fa-paper-plane me-2"></i>Envoyer argent
                        </a>
                    </li>
                </ul>
            </div>

            <div class="col-md-10 ms-sm-auto px-4">
                <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                    <h1 class="h2">${pageTitle}</h1>
                    <div class="btn-toolbar mb-2 mb-md-0">
                        <div class="btn-group me-2">
                            <button type="button" class="btn btn-sm btn-outline-secondary">Export</button>
                        </div>
                    </div>
                </div>

                <jsp:include page="${contentPage}" />
            </div>
        </div>
    </div>

    <div class="modal fade" id="formModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">${modalTitle}</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body" id="modalBody">
                    </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/jquery@3.7.1/dist/jquery.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/toastify-js"></script>

    <script>
        $(document).ready(function() {
            // Toast notifications
            const urlParams = new URLSearchParams(window.location.search);
            const success = urlParams.get('success');
            const error = urlParams.get('error');

            if (success) {
                Toastify({
                    text: success,
                    duration: 3000,
                    close: true,
                    gravity: "top",
                    position: "right",
                    backgroundColor: "#28a745",
                }).showToast();
            }

            if (error) {
                Toastify({
                    text: error,
                    duration: 3000,
                    close: true,
                    gravity: "top",
                    position: "right",
                    backgroundColor: "#dc3545",
                }).showToast();
            }

            // Handle modal forms
            $('.btn-edit, .btn-add').click(function(e) {
                e.preventDefault();
                const url = $(this).attr('href');
                const modal = $('#formModal');

                $.get(url, function(data) {
                    $('#modalBody').html(data);
                    modal.modal('show');
                });
            });

            // Handle form submission
            $(document).on('submit', '#modalForm', function(e) {
                e.preventDefault();
                const form = $(this);
                const url = form.attr('action');
                const method = form.attr('method');
                const data = form.serialize();

                $.ajax({
                    url: url,
                    type: method,
                    data: data,
                    success: function(response) {
                        if (response.redirect) {
                            window.location.href = response.redirect;
                        } else {
                            $('#modalBody').html(response);
                        }
                    },
                    error: function(xhr) {
                        $('#modalBody').html(xhr.responseText);
                    }
                });
            });
        });
    </script>
</body>
</html>