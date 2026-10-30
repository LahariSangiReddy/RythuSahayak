<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Look up a crop complaint status using its reference number.">
    <title>Track a Complaint | RythuSahayak</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Manrope:wght@500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body data-context-path="${pageContext.request.contextPath}">
<a class="skip-link" href="#main-content">Skip to content</a>
<jsp:include page="/WEB-INF/jspf/navigation.jsp"><jsp:param name="active" value="tracking"/></jsp:include>

<main id="main-content" class="page-main">
    <div class="container content-narrow">
        <div class="page-intro"><a class="back-link" href="${pageContext.request.contextPath}/"><i class="bi bi-arrow-left" aria-hidden="true"></i> Back to home</a><span class="eyebrow mt-4">A little clarity goes a long way</span><h1>Track your complaint</h1><p>Enter the reference number you received after submitting your report.</p></div>
        <div class="alert alert-demo" role="note"><i class="bi bi-info-circle" aria-hidden="true"></i><span>This student project provides a demonstration complaint-tracking service. Public results do not include mobile numbers or full problem descriptions.</span></div>
        <section class="lookup-panel" aria-labelledby="lookup-title">
            <form id="tracking-form" action="${pageContext.request.contextPath}/complaints/track" method="get">
                <label class="form-label" for="tracking-reference" id="lookup-title">Complaint reference</label>
                <div class="lookup-controls"><input class="form-control form-control-lg" id="tracking-reference" name="reference" type="text" value="<c:out value='${trackingReference}'/>" placeholder="e.g. RYT-20261029-1234567890ABCDEF" maxlength="29" autocomplete="off" aria-describedby="reference-help reference-error" required pattern="RYT-[0-9]{8}-[A-Fa-f0-9]{16}"><button class="btn btn-primary-green btn-lg" type="submit"><i class="bi bi-search me-2" aria-hidden="true"></i>Track complaint</button></div>
                <div class="form-text" id="reference-help">Format: RYT-YYYYMMDD-16-character reference suffix.</div>
                <div class="invalid-feedback" id="reference-error">Enter a reference in the format RYT-YYYYMMDD-16-character suffix.</div>
            </form>
        </section>
        <c:if test="${not empty trackingError}">
            <div class="alert alert-warning mt-4" role="status" aria-live="polite"><c:out value="${trackingError}"/></div>
        </c:if>
        <c:if test="${not empty trackingComplaint}">
            <section id="tracking-result" class="tracking-result mt-4" aria-live="polite" aria-labelledby="result-title">
                <div class="result-heading"><div><span class="section-kicker">COMPLAINT</span><h2 id="result-title"><c:out value="${trackingComplaint.referenceNumber}"/></h2><p>Submitted <c:out value="${trackingComplaint.createdAt}"/></p></div>
                    <span class="status-badge"><i class="bi bi-circle-fill" aria-hidden="true"></i><c:out value="${trackingComplaint.status}"/></span>
                </div>
                <div class="complaint-details">
                    <div class="detail-item"><span>Crop</span><strong><c:out value="${trackingComplaint.crop}"/></strong></div>
                    <div class="detail-item"><span>Problem category</span><strong><c:out value="${trackingComplaint.category}"/></strong></div>
                    <div class="detail-item"><span>Date observed</span><strong><c:out value="${trackingComplaint.observedDate}"/></strong></div>
                    <div class="detail-item"><span>Current status</span><strong><c:out value="${trackingComplaint.status}"/></strong></div>
                </div>
                <div class="public-remark"><strong>Latest public remark</strong><c:out value="${trackingComplaint.publicRemark}"/></div>
                <h3 class="timeline-heading">Status timeline</h3>
                <ol class="status-timeline list-unstyled">
                    <li class="timeline-stage is-complete">Submitted</li>
                    <li class="timeline-stage <c:if test='${trackingComplaint.status eq "UNDER_REVIEW" or trackingComplaint.status eq "ACTION_RECORDED" or trackingComplaint.status eq "RESOLVED"}'>is-complete</c:if>">Under Review</li>
                    <li class="timeline-stage <c:if test='${trackingComplaint.status eq "ACTION_RECORDED" or trackingComplaint.status eq "RESOLVED"}'>is-complete</c:if>">Action Recorded</li>
                    <li class="timeline-stage <c:if test='${trackingComplaint.status eq "RESOLVED"}'>is-complete</c:if>">Resolved</li>
                </ol>
            </section>
        </c:if>
        <c:if test="${empty trackingComplaint and empty trackingError}">
            <section id="tracking-empty" class="empty-state mt-4" aria-labelledby="empty-title"><span class="empty-icon"><i class="bi bi-clipboard2-search" aria-hidden="true"></i></span><h2 id="empty-title">Enter your complaint reference</h2><p>We’ll look up the latest status saved for that reference.</p><a class="text-link" href="${pageContext.request.contextPath}/report-issue.jsp">Submit a crop complaint <i class="bi bi-arrow-right" aria-hidden="true"></i></a></section>
        </c:if>
    </div>
</main>
<footer class="site-footer compact-footer"><div class="container footer-bottom"><a class="footer-home-link" href="${pageContext.request.contextPath}/">RythuSahayak</a><span>Student demonstration project · not an official government portal</span></div></footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/app.js" defer></script>
</body>
</html>
