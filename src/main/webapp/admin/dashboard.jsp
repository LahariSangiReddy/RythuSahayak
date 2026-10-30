<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Complaint Management | RythuSahayak</title>
    <link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Manrope:wght@500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body>
<a class="skip-link" href="#main-content">Skip to content</a>
<jsp:include page="/WEB-INF/jspf/navigation.jsp"><jsp:param name="active" value="admin-dashboard"/></jsp:include>
<main id="main-content" class="page-main admin-main"><div class="container">
    <div class="page-intro admin-page-intro"><span class="eyebrow">Administrator dashboard</span><h1>Complaint management</h1><p>Review reports and record a status with a farmer-safe public remark. Up to 100 most recent matching complaints are shown.</p></div>
    <section class="module-section admin-module-section" aria-label="Administrator services">
        <div class="module-grid admin-module-grid">
            <article class="module-card"><span class="module-icon"><i class="bi bi-inboxes" aria-hidden="true"></i></span><h2>Review complaints</h2><p>Filter reports by status, review their details, and record a farmer-visible update.</p><a href="#admin-complaints">Open complaint list <i class="bi bi-arrow-right" aria-hidden="true"></i></a></article>
            <article class="module-card"><span class="module-icon"><i class="bi bi-journal-bookmark" aria-hidden="true"></i></span><h2>Crop advisory</h2><p>Browse the public illustrative crop learning catalogue.</p><a href="${pageContext.request.contextPath}/advisories">Explore notes <i class="bi bi-arrow-right" aria-hidden="true"></i></a></article>
        </div>
    </section>
    <section class="summary-grid admin-summary-grid" aria-label="Complaint summary">
        <article class="summary-card"><span class="summary-label">All complaints</span><strong><c:out value="${complaintSummary.total}"/></strong><span class="summary-hint">Across all farmers</span></article>
        <article class="summary-card summary-pending"><span class="summary-label">Pending</span><strong><c:out value="${complaintSummary.pending}"/></strong><span class="summary-hint">Waiting for review</span></article>
        <article class="summary-card summary-progress"><span class="summary-label">In progress</span><strong><c:out value="${complaintSummary.inProgress}"/></strong><span class="summary-hint">Under review or action recorded</span></article>
        <article class="summary-card summary-resolved"><span class="summary-label">Resolved</span><strong><c:out value="${complaintSummary.resolved}"/></strong><span class="summary-hint">Marked resolved</span></article>
    </section>
    <c:if test="${not empty adminFlash}"><div class="alert alert-success" role="status"><c:out value="${adminFlash}"/></div></c:if>
    <section id="admin-complaints" class="admin-filter-panel"><form action="${pageContext.request.contextPath}/admin/complaints" method="get" class="admin-filter-form"><label class="form-label mb-0" for="status-filter">Filter by status</label><select class="form-select" id="status-filter" name="status"><option value="" <c:if test="${empty selectedStatus}">selected</c:if>>All statuses</option><option value="PENDING" <c:if test="${selectedStatus eq 'PENDING'}">selected</c:if>>Pending</option><option value="UNDER_REVIEW" <c:if test="${selectedStatus eq 'UNDER_REVIEW'}">selected</c:if>>Under review</option><option value="ACTION_RECORDED" <c:if test="${selectedStatus eq 'ACTION_RECORDED'}">selected</c:if>>Action recorded</option><option value="RESOLVED" <c:if test="${selectedStatus eq 'RESOLVED'}">selected</c:if>>Resolved</option></select><button class="btn btn-outline-green" type="submit">Apply filter</button></form><span class="admin-count"><c:out value="${complaints.size()}"/> records shown</span></section>
    <c:choose>
        <c:when test="${empty complaints}"><section class="empty-state"><span class="empty-icon"><i class="bi bi-inbox" aria-hidden="true"></i></span><h2>No complaints found</h2><p>No complaints match this status filter.</p></section></c:when>
        <c:otherwise>
            <div class="admin-complaints">
                <c:forEach items="${complaints}" var="complaint">
                    <article class="admin-complaint-card">
                        <div class="admin-complaint-header">                                                <div><span class="section-kicker">REFERENCE</span><h2><c:out value="${complaint.referenceNumber}"/></h2></div><span class="status-badge ${complaint.statusCssClass}"><c:out value="${complaint.status}"/></span></div>
                        <div class="admin-complaint-details">
                            <div><span>Farmer</span><strong><c:out value="${complaint.farmerName}"/></strong></div>
                            <div><span>Mobile (private)</span><strong><c:out value="${complaint.mobile}"/></strong></div>
                            <div><span>Village / District</span><strong><c:out value="${complaint.village}"/>, <c:out value="${complaint.district}"/></strong></div>
                            <div><span>Crop / Category</span><strong><c:out value="${complaint.crop}"/> · <c:out value="${complaint.category}"/></strong></div>
                            <div><span>Observed / submitted</span><strong><c:out value="${complaint.observedDate}"/> · <c:out value="${complaint.createdAtLabel}"/></strong></div>
                        </div>
                        <div class="admin-description"><strong>Private report description</strong><p><c:out value="${complaint.description}"/></p></div>
                        <form class="admin-update-form" action="${pageContext.request.contextPath}/admin/complaints/update" method="post">
                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                            <input type="hidden" name="complaintId" value="<c:out value='${complaint.id}'/>">
                            <div><label class="form-label" for="status-${complaint.id}">New status</label><select class="form-select" id="status-${complaint.id}" name="status" required><option value="PENDING" <c:if test="${complaint.status eq 'PENDING'}">selected</c:if>>Pending</option><option value="UNDER_REVIEW" <c:if test="${complaint.status eq 'UNDER_REVIEW'}">selected</c:if>>Under review</option><option value="ACTION_RECORDED" <c:if test="${complaint.status eq 'ACTION_RECORDED'}">selected</c:if>>Action recorded</option><option value="RESOLVED" <c:if test="${complaint.status eq 'RESOLVED'}">selected</c:if>>Resolved</option></select></div>
                            <div><label class="form-label" for="remark-${complaint.id}">Public remark</label><input class="form-control" id="remark-${complaint.id}" name="publicRemark" type="text" minlength="3" maxlength="500" required placeholder="A short update safe for the farmer to see"></div>
                            <button class="btn btn-primary-green" type="submit">Save status</button>
                        </form>
                    </article>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div></main>
<footer class="site-footer compact-footer"><div class="container footer-bottom"><a class="footer-home-link" href="${pageContext.request.contextPath}/">RythuSahayak</a><span>Administrator area · student demonstration project</span></div></footer>
</body>
</html>
