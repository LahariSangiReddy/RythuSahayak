<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sign In | RythuSahayak</title>
    <link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Manrope:wght@500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body>
<a class="skip-link" href="#main-content">Skip to content</a>
<jsp:include page="/WEB-INF/jspf/navigation.jsp"><jsp:param name="active" value="login"/></jsp:include>
<main id="main-content" class="page-main auth-main"><div class="container auth-container">
    <section class="auth-panel" aria-labelledby="login-title">
        <div class="auth-icon"><i class="bi bi-person-lock" aria-hidden="true"></i></div>
        <span class="eyebrow">Welcome back</span><h1 id="login-title">Sign in to your account</h1>
        <p class="auth-intro">Farmers can review their linked reports. Authorized administrators can manage complaint status.</p>
        <c:if test="${not empty param.required}"><div class="alert alert-warning" role="status">Sign in with an administrator account to open that page.</div></c:if>
        <c:if test="${not empty sessionScope.registrationSuccess}"><div class="alert alert-success" role="status"><c:out value="${sessionScope.registrationSuccess}"/></div><c:remove var="registrationSuccess" scope="session"/></c:if>
        <c:if test="${not empty loginError}"><div class="alert alert-danger" role="alert"><c:out value="${loginError}"/></div></c:if>
        <form action="${pageContext.request.contextPath}/login" method="post">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
            <div class="mb-3"><label class="form-label" for="email">Email address</label><input class="form-control" id="email" name="email" type="email" value="<c:out value='${loginEmail}'/>" maxlength="254" autocomplete="username" required></div>
            <div class="mb-3"><label class="form-label" for="password">Password</label><div class="password-field"><input class="form-control" id="password" name="password" type="password" autocomplete="current-password" maxlength="72" required><button class="password-toggle" type="button" data-password-toggle="password" aria-label="Show password" aria-pressed="false"><i class="bi bi-eye" aria-hidden="true"></i></button></div></div>
            <button class="btn btn-primary-green btn-lg w-100" type="submit">Login <i class="bi bi-arrow-right ms-1" aria-hidden="true"></i></button>
        </form>
        <p class="auth-switch">New to RythuSahayak? <a href="${pageContext.request.contextPath}/register.jsp">Create an account</a></p>
        <p class="auth-footnote"><i class="bi bi-shield-lock" aria-hidden="true"></i> Administrator access is provisioned locally; public registration creates farmer accounts only.</p>
    </section>
</div></main>
<footer class="site-footer compact-footer"><div class="container footer-bottom"><a class="footer-home-link" href="${pageContext.request.contextPath}/">RythuSahayak</a><span>Student demonstration project · not an official government portal</span></div></footer>
<script src="${pageContext.request.contextPath}/assets/js/app.js" defer></script>
</body>
</html>
