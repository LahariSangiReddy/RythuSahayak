<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Submit a crop issue report to the RythuSahayak demonstration portal.">
    <title>Report a Crop Problem | RythuSahayak</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Manrope:wght@500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet">
</head>
<body data-context-path="${pageContext.request.contextPath}">
<a class="skip-link" href="#main-content">Skip to content</a>
<jsp:include page="/WEB-INF/jspf/navigation.jsp"><jsp:param name="active" value="report"/></jsp:include>

<main id="main-content" class="page-main">
    <div class="container content-narrow">
        <div class="page-intro"><a class="back-link" href="${pageContext.request.contextPath}/"><i class="bi bi-arrow-left" aria-hidden="true"></i> Back to home</a><span class="eyebrow mt-4">We’re here to help you get started</span><h1>Tell us about your crop concern</h1><p>Your report is saved to the project database and receives a reference that you can use to check its status.</p></div>
        <div class="alert alert-demo" role="note"><i class="bi bi-info-circle" aria-hidden="true"></i><span>This project is a student demonstration, not an official government portal. Submitting a report does not guarantee a response or agricultural advice.</span></div>
        <c:if test="${not empty reportError}">
            <div class="alert alert-danger" role="alert" aria-live="assertive"><c:out value="${reportError}"/></div>
        </c:if>
        <c:if test="${not empty createdReference}">
            <section id="report-success" class="success-panel" aria-labelledby="success-title">
                <span class="success-icon"><i class="bi bi-check2" aria-hidden="true"></i></span>
                <p class="eyebrow">Report saved</p><h2 id="success-title">Your reference is ready</h2>
                <p>Your complaint was saved successfully. Keep this reference to check its status.</p>
                <div class="reference-box"><span>COMPLAINT REFERENCE</span><strong><c:out value="${createdReference}"/></strong></div>
                <a class="btn btn-primary-green" href="${pageContext.request.contextPath}/complaints/track?reference=<c:out value='${createdReference}'/>">Track this complaint <i class="bi bi-arrow-right ms-1" aria-hidden="true"></i></a>
            </section>
        </c:if>

        <form id="report-form" class="form-panel" action="${pageContext.request.contextPath}/complaints/create" method="post" novalidate>
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
            <div class="form-panel-heading"><div><span class="section-kicker">CROP ISSUE REPORT</span><h2>Your observations</h2></div><span class="required-legend"><span aria-hidden="true">*</span> Required</span></div>
            <div class="row g-4">
                <div class="col-md-6"><label class="form-label" for="farmer-name">Farmer name <span class="required-mark" aria-hidden="true">*</span></label><input class="form-control" id="farmer-name" name="farmerName" type="text" value="<c:out value='${formValues.farmerName}'/>" required minlength="2" maxlength="80" autocomplete="name" aria-describedby="farmer-name-error"><div class="invalid-feedback" id="farmer-name-error">Enter a name between 2 and 80 characters.</div></div>
                <div class="col-md-6"><label class="form-label" for="mobile-number">Mobile number <span class="required-mark" aria-hidden="true">*</span></label><div class="input-group"><span class="input-group-text">+91</span><input class="form-control" id="mobile-number" name="mobile" type="tel" value="<c:out value='${formValues.mobile}'/>" inputmode="numeric" autocomplete="tel-national" required minlength="10" maxlength="10" pattern="[6-9][0-9]{9}" aria-describedby="mobile-help mobile-error"></div><div class="form-text" id="mobile-help">Use a 10-digit Indian mobile number.</div><div class="invalid-feedback" id="mobile-error">Enter a valid 10-digit number starting with 6, 7, 8 or 9.</div></div>
                <div class="col-md-6"><label class="form-label" for="village">Village <span class="required-mark" aria-hidden="true">*</span></label><input class="form-control" id="village" name="village" type="text" value="<c:out value='${formValues.village}'/>" required minlength="2" maxlength="80" autocomplete="address-level3" aria-describedby="village-error"><div class="invalid-feedback" id="village-error">Enter a village name between 2 and 80 characters.</div></div>
                <div class="col-md-6"><label class="form-label" for="district">District <span class="required-mark" aria-hidden="true">*</span></label><input class="form-control" id="district" name="district" type="text" value="<c:out value='${formValues.district}'/>" required minlength="2" maxlength="80" autocomplete="address-level2" aria-describedby="district-error"><div class="invalid-feedback" id="district-error">Enter a district between 2 and 80 characters.</div></div>
                <div class="col-md-6"><label class="form-label" for="crop">Crop <span class="required-mark" aria-hidden="true">*</span></label><select class="form-select" id="crop" name="crop" required aria-describedby="crop-error"><option value="">Choose a crop</option><option value="Rice" <c:if test="${formValues.crop eq 'Rice'}">selected</c:if>>Rice</option><option value="Cotton" <c:if test="${formValues.crop eq 'Cotton'}">selected</c:if>>Cotton</option><option value="Tomato" <c:if test="${formValues.crop eq 'Tomato'}">selected</c:if>>Tomato</option><option value="Chilli" <c:if test="${formValues.crop eq 'Chilli'}">selected</c:if>>Chilli</option><option value="Maize" <c:if test="${formValues.crop eq 'Maize'}">selected</c:if>>Maize</option><option value="Groundnut" <c:if test="${formValues.crop eq 'Groundnut'}">selected</c:if>>Groundnut</option><option value="Other" <c:if test="${formValues.crop eq 'Other'}">selected</c:if>>Other</option></select><div class="invalid-feedback" id="crop-error">Choose the crop related to your concern.</div></div>
                <div class="col-md-6"><label class="form-label" for="category">Problem category <span class="required-mark" aria-hidden="true">*</span></label><select class="form-select" id="category" name="category" required aria-describedby="category-error"><option value="">Choose a category</option><option value="Crop Disease" <c:if test="${formValues.category eq 'Crop Disease'}">selected</c:if>>Crop Disease</option><option value="Pest Concern" <c:if test="${formValues.category eq 'Pest Concern'}">selected</c:if>>Pest Concern</option><option value="Irrigation" <c:if test="${formValues.category eq 'Irrigation'}">selected</c:if>>Irrigation</option><option value="Soil/Nutrition" <c:if test="${formValues.category eq 'Soil/Nutrition'}">selected</c:if>>Soil/Nutrition</option><option value="Weather Damage" <c:if test="${formValues.category eq 'Weather Damage'}">selected</c:if>>Weather Damage</option><option value="Other" <c:if test="${formValues.category eq 'Other'}">selected</c:if>>Other</option></select><div class="invalid-feedback" id="category-error">Choose a problem category.</div></div>
                <div class="col-12"><label class="form-label" for="description">What have you observed? <span class="required-mark" aria-hidden="true">*</span></label><textarea class="form-control" id="description" name="description" rows="5" required minlength="20" maxlength="1000" aria-describedby="description-help description-count description-error" placeholder="Describe the signs you noticed, which part of the crop is affected, and when it began."><c:out value="${formValues.description}"/></textarea><div class="d-flex justify-content-between gap-3"><div class="form-text" id="description-help">Please avoid including sensitive personal information in your description.</div><div class="form-text character-count" id="description-count" aria-live="polite">0 / 1000</div></div><div class="invalid-feedback" id="description-error">Describe the issue in at least 20 characters.</div></div>
                <div class="col-md-6"><label class="form-label" for="observed-date">Date observed <span class="required-mark" aria-hidden="true">*</span></label><input class="form-control" id="observed-date" name="observedDate" type="date" value="<c:out value='${formValues.observedDate}'/>" required aria-describedby="observed-date-error"><div class="invalid-feedback" id="observed-date-error">Choose a valid date that is not in the future.</div></div>
            </div>
            <div class="form-actions"><button class="btn btn-primary-green btn-lg" id="report-submit" type="submit"><span class="button-label">Submit complaint</span><span class="button-loading d-none"><span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>Submitting…</span></button><button class="btn btn-outline-secondary btn-lg" id="report-reset" type="reset">Reset form</button></div>
            <p class="form-footnote"><i class="bi bi-shield-lock" aria-hidden="true"></i> Mobile details are private and are not displayed in public complaint tracking.</p>
        </form>
    </div>
</main>
<footer class="site-footer compact-footer"><div class="container footer-bottom"><a class="footer-home-link" href="${pageContext.request.contextPath}/">RythuSahayak</a><span>Student demonstration project · not an official government portal</span></div></footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/app.js" defer></script>
</body>
</html>
