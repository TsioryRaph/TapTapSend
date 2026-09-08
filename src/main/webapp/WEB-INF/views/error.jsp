<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Erreur - TapTapSend</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free@6.5.2/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
</head>
<body style="background:var(--tt-bg); min-height:100vh; display:flex; align-items:center;">
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-md-5 text-center">
                <div class="stat-icon bg-rose mx-auto mb-4" style="width:72px;height:72px;font-size:28px;">
                    <i class="fas fa-triangle-exclamation"></i>
                </div>
                <h1 class="fw-800" style="font-size:52px; letter-spacing:-0.03em;">
                    ${requestScope['jakarta.servlet.error.status_code']}
                </h1>
                <p class="text-muted mb-4">
                    <c:choose>
                        <c:when test="${requestScope['jakarta.servlet.error.status_code'] == 404}">
                            La page que vous recherchez n'existe pas ou a été déplacée.
                        </c:when>
                        <c:otherwise>
                            Une erreur inattendue s'est produite lors du traitement de votre requête.
                        </c:otherwise>
                    </c:choose>
                </p>
                <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary px-4">
                    <i class="fas fa-house me-1"></i> Retour au tableau de bord
                </a>
            </div>
        </div>
    </div>
</body>
</html>