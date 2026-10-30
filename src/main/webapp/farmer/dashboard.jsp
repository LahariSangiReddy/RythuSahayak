<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Farmer Dashboard | RythuSahayak</title>
    <link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Manrope:wght@500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body>
<a class="skip-link" href="#main-content">Skip to content</a>
<jsp:include page="/WEB-INF/jspf/navigation.jsp"><jsp:param name="active" value="farmer-dashboard"/></jsp:include>
<main id="main-content" class="page-main dashboard-main"><div class="container">
    <div class="dashboard-heading"><div><span class="eyebrow">Farmer workspace</span><h1>Welcome back, <c:out value="${displayName}"/></h1><p>Review your crop reports and choose a service to continue.</p></div></div>
    <c:if test="${not empty createdReference}"><div class="alert alert-success dashboard-flash" role="status"><span>Your report was saved. Reference: <strong><c:out value="${createdReference}"/></strong></span><a class="alert-link" href="${pageContext.request.contextPath}/complaints/track?reference=<c:out value='${createdReference}'/>">Track status</a></div></c:if>
    <section class="summary-grid" aria-label="Complaint summary">
        <article class="summary-card"><span class="summary-label">All complaints</span><strong><c:out value="${complaintSummary.total}"/></strong><span class="summary-hint">Linked to your account</span></article>
        <article class="summary-card summary-pending"><span class="summary-label">Pending</span><strong><c:out value="${complaintSummary.pending}"/></strong><span class="summary-hint">Waiting for review</span></article>
        <article class="summary-card summary-progress"><span class="summary-label">In progress</span><strong><c:out value="${complaintSummary.inProgress}"/></strong><span class="summary-hint">Under review or action recorded</span></article>
        <article class="summary-card summary-resolved"><span class="summary-label">Resolved</span><strong><c:out value="${complaintSummary.resolved}"/></strong><span class="summary-hint">Marked resolved</span></article>
    </section>
    <section class="module-section dashboard-section" aria-labelledby="farmer-modules-title">
        <div class="dashboard-section-heading"><div><span class="section-kicker">AVAILABLE SERVICES</span><h2 id="farmer-modules-title">Your services</h2></div></div>
        <div class="module-grid">
            <article class="module-card"><span class="module-icon"><i class="bi bi-clipboard2-plus" aria-hidden="true"></i></span><h3>Report a complaint</h3><p>Share a crop concern and receive a reference to follow its status.</p><a href="${pageContext.request.contextPath}/report-issue.jsp">Report issue <i class="bi bi-arrow-right" aria-hidden="true"></i></a></article>
            <article class="module-card"><span class="module-icon"><i class="bi bi-list-check" aria-hidden="true"></i></span><h3>My complaints</h3><p>Review the reports linked to your account and their latest saved status.</p><a href="${pageContext.request.contextPath}/farmer/complaints#recent-complaints">View complaints <i class="bi bi-arrow-right" aria-hidden="true"></i></a></article>
            <article class="module-card"><span class="module-icon"><i class="bi bi-search" aria-hidden="true"></i></span><h3>Track a complaint</h3><p>Look up limited public status information using a complaint reference.</p><a href="${pageContext.request.contextPath}/track-complaint.jsp">Track status <i class="bi bi-arrow-right" aria-hidden="true"></i></a></article>
            <article class="module-card"><span class="module-icon"><i class="bi bi-journal-bookmark" aria-hidden="true"></i></span><h3>Crop advisory</h3><p>Browse illustrative crop learning notes and filter by crop or issue.</p><a href="${pageContext.request.contextPath}/advisories">Explore notes <i class="bi bi-arrow-right" aria-hidden="true"></i></a></article>
            <article class="module-card"><span class="module-icon"><i class="bi bi-person-vcard" aria-hidden="true"></i></span><h3>My profile</h3><p>View the account details currently linked to your farmer profile.</p><a href="${pageContext.request.contextPath}/farmer/profile">View profile <i class="bi bi-arrow-right" aria-hidden="true"></i></a></article>
        </div>
    </section>
    <section id="recent-complaints" class="dashboard-section" aria-labelledby="recent-complaints-title">
        <div class="dashboard-section-heading"><div><span class="section-kicker">YOUR REPORTS</span><h2 id="recent-complaints-title">Recent complaints</h2></div><a class="text-link" href="${pageContext.request.contextPath}/report-issue.jsp">Report another issue <i class="bi bi-arrow-right" aria-hidden="true"></i></a></div>
        <c:choose>
            <c:when test="${empty complaints}"><div class="empty-state dashboard-empty"><span class="empty-icon"><i class="bi bi-clipboard2-plus" aria-hidden="true"></i></span><h3>No complaints yet</h3><p>When you submit a crop issue while signed in, you can follow it here.</p><a class="btn btn-primary-green" href="${pageContext.request.contextPath}/report-issue.jsp">Create your first report</a></div></c:when>
            <c:otherwise><div class="table-responsive dashboard-table-wrap"><table class="table dashboard-table align-middle"><thead><tr><th scope="col">Reference</th><th scope="col">Crop issue</th><th scope="col">Submitted</th><th scope="col">Status</th><th scope="col"><span class="visually-hidden">Actions</span></th></tr></thead><tbody>
                <c:forEach items="${complaints}" var="complaint"><tr><td class="reference-cell"><c:out value="${complaint.referenceNumber}"/></td><td><strong><c:out value="${complaint.crop}"/></strong><span class="table-subtext"><c:out value="${complaint.category}"/></span></td><td><c:out value="${complaint.createdAtLabel}"/></td><td><span class="status-badge ${complaint.statusCssClass}"><c:out value="${complaint.status}"/></span></td><td><a class="table-action" href="${pageContext.request.contextPath}/complaints/track?reference=<c:out value='${complaint.referenceNumber}'/>">Track <span class="visually-hidden">complaint <c:out value="${complaint.referenceNumber}"/></span><i class="bi bi-arrow-up-right" aria-hidden="true"></i></a></td></tr></c:forEach>
            </tbody></table></div></c:otherwise>
        </c:choose>
    </section>
</div></main>
<footer class="site-footer compact-footer"><div class="container footer-bottom"><a class="footer-home-link" href="${pageContext.request.contextPath}/">RythuSahayak</a><span>Student demonstration project · not an official service</span></div></footer>
</body>
</html>
