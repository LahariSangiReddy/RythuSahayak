<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Create Farmer Account | RythuSahayak</title>
    <link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Manrope:wght@500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body>
<a class="skip-link" href="#main-content">Skip to content</a>
<jsp:include page="/WEB-INF/jspf/navigation.jsp"><jsp:param name="active" value="register"/></jsp:include>
<main id="main-content" class="page-main auth-main"><div class="container auth-container">
    <section class="auth-panel" aria-labelledby="register-title">
        <div class="auth-icon"><i class="bi bi-person-plus" aria-hidden="true"></i></div>
        <span class="eyebrow">Join the farmer community</span><h1 id="register-title">Create a farmer account</h1>
        <p class="auth-intro">An account lets you see the reports you submit while signed in. Guest reports are not linked to an account.</p>
        <c:if test="${not empty registerError}"><div class="alert alert-danger" role="alert"><c:out value="${registerError}"/></div></c:if>
        <form id="register-form" action="${pageContext.request.contextPath}/register" method="post">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
            <div class="mb-3"><label class="form-label" for="full-name">Full name</label><input class="form-control" id="full-name" name="fullName" type="text" value="<c:out value='${formFullName}'/>" minlength="2" maxlength="80" autocomplete="name" required></div>
            <div class="mb-3"><label class="form-label" for="email">Email address</label><input class="form-control" id="email" name="email" type="email" value="<c:out value='${formEmail}'/>" maxlength="254" autocomplete="email" required></div>
            <div class="mb-3"><label class="form-label" for="mobile">Mobile number <span class="text-secondary">(optional)</span></label><input class="form-control" id="mobile" name="mobile" type="tel" value="<c:out value='${formMobile}'/>" inputmode="numeric" pattern="[6-9][0-9]{9}" maxlength="10" autocomplete="tel-national"></div>
            <div class="mb-3"><label class="form-label" for="password">Password</label><div class="password-field"><input class="form-control" id="password" name="password" type="password" minlength="12" maxlength="72" autocomplete="new-password" aria-describedby="password-help" required><button class="password-toggle" type="button" data-password-toggle="password" aria-label="Show password" aria-pressed="false"><i class="bi bi-eye" aria-hidden="true"></i></button></div><div class="form-text" id="password-help">Use 12–72 characters. Passwords are stored as BCrypt hashes.</div></div>
            <div class="mb-4"><label class="form-label" for="confirm-password">Confirm password</label><div class="password-field"><input class="form-control" id="confirm-password" name="confirmPassword" type="password" minlength="12" maxlength="72" autocomplete="new-password" aria-describedby="confirm-password-help" required><button class="password-toggle" type="button" data-password-toggle="confirm-password" aria-label="Show password" aria-pressed="false"><i class="bi bi-eye" aria-hidden="true"></i></button></div><div class="form-text" id="confirm-password-help">Enter the same password again.</div></div>
            <button class="btn btn-primary-green btn-lg w-100" type="submit">Create farmer account</button>
        </form>
        <p class="auth-switch">Already registered? <a href="${pageContext.request.contextPath}/login.jsp">Sign in</a></p>
        <p class="auth-footnote"><i class="bi bi-shield-lock" aria-hidden="true"></i> This form can create farmer accounts only. Administrator accounts must be provisioned locally.</p>
    </section>
</div></main>
<footer class="site-footer compact-footer"><div class="container footer-bottom"><a class="footer-home-link" href="${pageContext.request.contextPath}/">RythuSahayak</a><span>Student demonstration project · not an official government portal</span></div></footer>
<script src="${pageContext.request.contextPath}/assets/js/app.js" defer></script>
</body>
</html>
