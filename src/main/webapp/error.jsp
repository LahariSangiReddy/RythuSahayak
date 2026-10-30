<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Request could not be completed | RythuSahayak</title><link href="${pageContext.request.contextPath}/assets/css/style.css" rel="stylesheet"><link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"><link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet"></head>
<body><jsp:include page="/WEB-INF/jspf/navigation.jsp"/><main id="main-content" class="page-main"><div class="container content-narrow"><section class="empty-state"><span class="empty-icon" aria-hidden="true">!</span><h1>We could not complete that request</h1><p>Your request was not completed. Please return to the portal and try again. No database or server details are shown here.</p><a class="btn btn-primary-green" href="${pageContext.request.contextPath}/">Return to RythuSahayak</a></section></div></main></body>
</html>
