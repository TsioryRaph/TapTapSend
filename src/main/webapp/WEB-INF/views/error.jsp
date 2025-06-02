<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Erreur</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div class="card border-danger">
                    <div class="card-header bg-danger text-white">
                        <h3 class="card-title">Erreur ${requestScope['jakarta.servlet.error.status_code']}</h3>
                    </div>
                    <div class="card-body">
                        <p class="card-text">
                            <c:choose>
                                <c:when test="${requestScope['jakarta.servlet.error.status_code'] == 404}">
                                    La page que vous recherchez n'existe pas.
                                </c:when>
                                <c:otherwise>
                                    Une erreur s'est produite lors du traitement de votre requête.
                                </c:otherwise>
                            </c:choose>
                        </p>
                        <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary">
                            Retour à l'accueil
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>