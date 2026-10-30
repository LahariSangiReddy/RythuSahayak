<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Search illustrative crop observation notes from the RythuSahayak XML catalogue.">
    <title>Crop Advisory | RythuSahayak</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Manrope:wght@500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body>
<a class="skip-link" href="#main-content">Skip to content</a>
<jsp:include page="/WEB-INF/jspf/navigation.jsp"><jsp:param name="active" value="advisory"/></jsp:include>

<main id="main-content" class="page-main">
    <section class="advisory-hero"><div class="container">
        <a class="back-link" href="${pageContext.request.contextPath}/"><i class="bi bi-arrow-left" aria-hidden="true"></i> Back to home</a>
        <div class="advisory-heading"><div><span class="eyebrow">Small observations can be useful</span><h1>Crop learning notes</h1><p>Browse general, illustrative observations. These notes are for learning only — not diagnosis or official agricultural advice.</p></div><span class="advisory-hero-icon" aria-hidden="true"><i class="bi bi-journal-bookmark"></i></span></div>
        <div class="alert alert-demo advisory-disclaimer" role="note"><i class="bi bi-shield-check" aria-hidden="true"></i><span><strong>Demonstration catalogue — not a diagnosis or official advisory.</strong> Actual crop treatment should be based on verified local conditions and guidance from a qualified agricultural officer or local extension service.</span></div>
    </div></section>
    <section class="container advisory-content" aria-labelledby="advisory-list-title">
        <form id="advisory-filters" class="filter-panel" action="${pageContext.request.contextPath}/advisories" method="get">
            <div class="filter-search"><label class="form-label" for="advisory-search">Search title or description</label><div class="search-input-wrap"><i class="bi bi-search" aria-hidden="true"></i><input id="advisory-search" name="query" class="form-control" type="search" value="<c:out value='${advisoryQuery}'/>" placeholder="Try leaf marks, uneven growth…" autocomplete="off"></div></div>
            <div class="filter-category"><label class="form-label" for="advisory-crop">Filter by crop</label><select id="advisory-crop" name="crop" class="form-select"><option value="">All crops</option><c:forEach items="${advisoryCrops}" var="crop"><option value="<c:out value='${crop}'/>" <c:if test="${selectedCrop eq crop}">selected</c:if>><c:out value="${crop}"/></option></c:forEach></select></div>
            <div class="filter-category"><label class="form-label" for="advisory-category">Filter by issue</label><select id="advisory-category" name="category" class="form-select"><option value="">All issues</option><c:forEach items="${advisoryCategories}" var="category"><option value="<c:out value='${category}'/>" <c:if test="${selectedCategory eq category}">selected</c:if>><c:out value="${category}"/></option></c:forEach></select></div>
            <div class="advisory-filter-actions"><button class="btn btn-primary-green" type="submit">Apply filters</button><button class="btn btn-outline-green clear-advisory-filters" id="clear-advisory-filters" type="button">Clear filters</button></div>
        </form>
        <c:if test="${not empty advisoryError}"><div class="alert alert-warning mt-4" role="alert"><c:out value="${advisoryError}"/></div></c:if>
        <div class="advisory-results-heading"><h2 id="advisory-list-title">Learning notes <span class="demo-pill">Illustrative</span></h2><p id="advisory-count" aria-live="polite">Showing <c:out value="${advisoryCount}"/> sample notes</p></div>
        <div class="row g-4 advisory-grid" id="advisory-grid">
            <c:forEach items="${advisories}" var="advisory">
                <div class="col-md-6 advisory-card" data-crop="<c:out value='${advisory.crop}'/>" data-category="<c:out value='${advisory.category}'/>">
                    <article class="advisory-card-inner">
                        <div class="advisory-card-top"><span class="crop-chip"><c:out value="${advisory.crop}"/></span><span class="topic-chip topic-health"><i class="bi bi-journal-check" aria-hidden="true"></i><c:out value="${advisory.category}"/></span></div>
                        <h3><c:out value="${advisory.title}"/></h3>
                        <p><c:out value="${advisory.description}"/></p>
                        <div class="observation-box"><strong><i class="bi bi-lightbulb" aria-hidden="true"></i> General learning note</strong><span><c:out value="${advisory.recommendation}"/></span></div>
                        <p class="expert-note"><i class="bi bi-person-check" aria-hidden="true"></i><span>Growth stage: <c:out value="${advisory.growthStage}"/> · Language: <c:out value="${advisory.language}"/></span></p>
                        <span class="illustrative-label">Reviewed <c:out value="${advisory.lastReviewed}"/> · <c:out value="${advisory.source}"/></span>
                    </article>
                </div>
            </c:forEach>
        </div>
        <div id="advisory-empty" class="empty-state advisory-empty" <c:if test="${not empty advisories}">hidden</c:if>><span class="empty-icon"><i class="bi bi-search" aria-hidden="true"></i></span><h2>No notes match those filters</h2><p>Try a different search, crop, or issue category.</p><button class="btn btn-outline-green clear-advisory-filters" type="button">Clear filters</button></div>
    </section>
</main>
<footer class="site-footer compact-footer"><div class="container footer-bottom"><a class="footer-home-link" href="${pageContext.request.contextPath}/">RythuSahayak</a><span>Student demonstration project · not an official government portal</span></div></footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/app.js" defer></script>
<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/jquery-features.js" defer></script>
</body>
</html>
