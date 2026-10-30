<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>My Profile | RythuSahayak</title>
    <link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Manrope:wght@500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body>
<a class="skip-link" href="#main-content">Skip to content</a>
<jsp:include page="/WEB-INF/jspf/navigation.jsp"><jsp:param name="active" value="profile"/></jsp:include>
<main id="main-content" class="page-main dashboard-main"><div class="container">
    <div class="page-intro dashboard-page-intro"><span class="eyebrow">Your account</span><h1>Profile</h1><p>These are the account details currently linked to your farmer profile.</p></div>
    <section class="profile-panel" aria-label="Farmer account details">
        <div class="profile-avatar" aria-hidden="true"><i class="bi bi-person"></i></div>
        <dl class="profile-details">
            <div><dt>Full name</dt><dd><c:out value="${profile.fullName}"/></dd></div>
            <div><dt>Email address</dt><dd><c:out value="${profile.email}"/></dd></div>
            <div><dt>Mobile number</dt><dd><c:choose><c:when test="${not empty profile.mobile}"><c:out value="${profile.mobile}"/></c:when><c:otherwise>Not provided</c:otherwise></c:choose></dd></div>
            <div><dt>Account type</dt><dd>Farmer</dd></div>
        </dl>
        <p class="profile-note"><i class="bi bi-info-circle" aria-hidden="true"></i> Profile editing is not available in this demonstration.</p>
        <a class="btn btn-primary-green" href="${pageContext.request.contextPath}/farmer/complaints">Back to dashboard</a>
    </section>
</div></main>
<footer class="site-footer compact-footer"><div class="container footer-bottom"><a class="footer-home-link" href="${pageContext.request.contextPath}/">RythuSahayak</a><span>Student demonstration project · not an official service</span></div></footer>
</body>
</html>
