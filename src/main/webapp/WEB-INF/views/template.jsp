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
    <%-- Vous pouvez également utiliser Bootswatch Solar via un CDN pour un thème complet --%>
    <%-- <link href="https://cdn.jsdelivr.net/npm/bootswatch@5.3.3/dist/solar/bootstrap.min.css" rel="stylesheet"> --%>

    <link href="https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free@6.5.2/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/toastify-js/src/toastify.min.css">

    <%-- <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css"> --%>

    <style>
        .sidebar {
            min-height: 100vh;
            /* Adaptez la couleur de la sidebar au thème Solar */
            background-color: var(--bs-gray-dark); /* Couleur gris foncé de Solar : #073642 */
        }
        .sidebar .nav-link {
            color: rgba(255, 255, 255, 0.75);
        }
        .sidebar .nav-link:hover {
            color: rgba(255, 255, 255, 1);
        }
        .sidebar .nav-link.active {
            color: white;
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

                <%-- C'est ici que le contenu spécifique de la page sera inclus --%>
                <jsp:include page="${contentPage}" />
            </div>
        </div>
    </div>

    <div class="modal fade" id="formModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="formModalLabel"></h5>
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
            // Fonction utilitaire pour afficher les messages Toastify
            function showToast(message, type) {
                let backgroundColor = "";
                if (type === "success") {
                    backgroundColor = "#28a745"; // Vert
                } else if (type === "error") {
                    backgroundColor = "#dc3545"; // Rouge
                } else {
                    backgroundColor = "#17a2b8"; // Bleu (pour info/défaut)
                }

                Toastify({
                    text: message,
                    duration: 3000,
                    close: true,
                    gravity: "top",
                    position: "right",
                    backgroundColor: backgroundColor,
                }).showToast();
            }

            // Notifications Toastify pour les messages passés via URL (pour les redirections initiales)
            const urlParams = new URLSearchParams(window.location.search);
            const success = urlParams.get('success');
            const error = urlParams.get('error');

            if (success) {
                showToast(decodeURIComponent(success.replace(/\+/g, ' ')), "success");
                // Nettoyer l'URL après affichage du toast pour éviter de le réafficher au rechargement
                urlParams.delete('success');
                window.history.replaceState({}, document.title, "?" + urlParams.toString());
            }

            if (error) {
                showToast(decodeURIComponent(error.replace(/\+/g, ' ')), "error");
                // Nettoyer l'URL après affichage du toast
                urlParams.delete('error');
                window.history.replaceState({}, document.title, "?" + urlParams.toString());
            }

            // Gérer l'ouverture des modales (Ajout, Édition, PDF)
            $(document).on('click', '.btn-add, .btn-edit, .btn-pdf-modal-open', function(e) {
                e.preventDefault();
                const url = $(this).attr('href');
                const modalTitle = $(this).data('modal-title');

                $('#formModalLabel').text(modalTitle);

                $.ajax({
                    url: url,
                    type: 'GET',
                    success: function(data) {
                        $('#modalBody').html(data);
                        $('#formModal').modal('show');
                    },
                    error: function(xhr) {
                        console.error("Erreur lors du chargement du formulaire:", xhr.responseText);
                        $('#modalBody').html('<div class="alert alert-danger">Erreur lors du chargement du formulaire : ' + (xhr.responseText || "Requête échouée") + '</div>');
                        $('#formModal').modal('show');
                    }
                });
            });

            // Gérer la soumission des formulaires dans la modale via AJAX (pour ajout/modification)
            $(document).on('submit', '#modalBody form:not(#pdfForm)', function(e) {
                e.preventDefault(); // Empêche la soumission normale du formulaire
                const form = $(this);
                const url = form.attr('action');
                const method = form.attr('method');
                const data = form.serialize();

                $.ajax({
                    url: url,
                    type: method,
                    data: data,
                    dataType: 'json', // Indique que nous attendons du JSON
                    success: function(jsonResponse) {
                        if (jsonResponse && jsonResponse.redirect) {
                            // Cas où le serveur demande une redirection complète (si nécessaire)
                            window.location.href = jsonResponse.redirect;
                        } else if (jsonResponse && jsonResponse.message) {
                            // Afficher un message de succès
                            showToast(jsonResponse.message, "success");
                            $('#formModal').modal('hide'); // Fermer la modale
                            window.location.reload(); // Recharger toute la page pour rafraîchir la liste
                        } else if (jsonResponse && jsonResponse.error) {
                            // Afficher un message d'erreur
                            showToast(jsonResponse.error, "error");
                            // Ne pas fermer la modale pour laisser l'utilisateur corriger l'erreur
                        } else {
                            // Si la réponse n'est pas un JSON structuré (peut arriver si le serveur renvoie du HTML en cas d'erreur non gérée)
                            console.warn("Réponse non structurée (non JSON de redirection/message/erreur):", jsonResponse);
                            $('#modalBody').html(jsonResponse); // Tente d'afficher la réponse HTML dans la modale
                        }
                    },
                    error: function(xhr) {
                        console.error("Erreur de soumission du formulaire AJAX:", xhr.responseText);
                        showToast("Erreur interne du serveur : " + (xhr.responseText || "Requête échouée"), "error");
                        $('#modalBody').html('<div class="alert alert-danger">Erreur de soumission : ' + (xhr.responseText || "Requête échouée") + '</div>');
                    }
                });
            });

            // Gérer la soumission des formulaires de suppression via AJAX
            $(document).on('submit', '.delete-envoi-form', function(e) {
                e.preventDefault(); // Empêche la soumission normale du formulaire
                const form = $(this);
                const url = form.attr('action');
                const data = form.serialize();

                // Confirmer la suppression avec l'utilisateur
                if (confirm('Êtes-vous sûr de vouloir supprimer cet envoi ?')) {
                    $.ajax({
                        url: url,
                        type: 'POST', // Les suppressions sont généralement des POST
                        data: data,
                        dataType: 'json', // Attendez une réponse JSON
                        success: function(jsonResponse) {
                            if (jsonResponse && jsonResponse.message) {
                                showToast(jsonResponse.message, "success");
                                window.location.reload(); // Recharger la page pour mettre à jour la liste
                            } else if (jsonResponse && jsonResponse.error) {
                                showToast(jsonResponse.error, "error");
                            } else {
                                console.warn("Réponse inattendue après suppression:", jsonResponse);
                                showToast("Une erreur inconnue est survenue lors de la suppression.", "error");
                            }
                        },
                        error: function(xhr) {
                            console.error("Erreur AJAX lors de la suppression:", xhr.responseText);
                            showToast("Erreur lors de la suppression : " + (xhr.responseText || "Requête échouée"), "error");
                        }
                    });
                }
            });
        });
    </script>
</body>
</html>