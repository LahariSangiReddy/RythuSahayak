<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<header class="site-header">
    <nav class="site-nav container" aria-label="Primary navigation">
        <a class="brand-lockup" href="${pageContext.request.contextPath}/">
            <span class="brand-mark" aria-hidden="true"><i class="bi bi-flower1"></i></span>
            <span class="brand-copy">
                <strong>RythuSahayak</strong>
                <small>
                    <c:choose>
                        <c:when test="${sessionScope.authenticatedUser.role eq 'ADMIN'}">Administrator workspace</c:when>
                        <c:when test="${sessionScope.authenticatedUser.role eq 'FARMER'}">Farmer assistance</c:when>
                        <c:otherwise>Farmer Assistance Portal</c:otherwise>
                    </c:choose>
                </small>
            </span>
        </a>
        <details class="site-nav-menu">
            <summary class="site-nav-toggle"><i class="bi bi-list" aria-hidden="true"></i><span>Menu</span></summary>
            <ul class="site-nav-links">
                <c:choose>
                    <c:when test="${sessionScope.authenticatedUser.role eq 'FARMER'}">
                        <li><a class="site-nav-link<c:if test='${param.active eq "farmer-dashboard"}'> is-active</c:if>" <c:if test="${param.active eq 'farmer-dashboard'}">aria-current="page"</c:if> href="${pageContext.request.contextPath}/farmer/complaints">Dashboard</a></li>
                        <li><a class="site-nav-link<c:if test='${param.active eq "farmer-complaints"}'> is-active</c:if>" <c:if test="${param.active eq 'farmer-complaints'}">aria-current="page"</c:if> href="${pageContext.request.contextPath}/farmer/complaints#recent-complaints">My complaints</a></li>
                        <li><a class="site-nav-link<c:if test='${param.active eq "report"}'> is-active</c:if>" <c:if test="${param.active eq 'report'}">aria-current="page"</c:if> href="${pageContext.request.contextPath}/report-issue.jsp">Report issue</a></li>
                        <li><a class="site-nav-link<c:if test='${param.active eq "tracking"}'> is-active</c:if>" <c:if test="${param.active eq 'tracking'}">aria-current="page"</c:if> href="${pageContext.request.contextPath}/track-complaint.jsp">Track status</a></li>
                        <li><a class="site-nav-link<c:if test='${param.active eq "advisory"}'> is-active</c:if>" <c:if test="${param.active eq 'advisory'}">aria-current="page"</c:if> href="${pageContext.request.contextPath}/advisories">Crop advisory</a></li>
                        <li><a class="site-nav-link<c:if test='${param.active eq "profile"}'> is-active</c:if>" <c:if test="${param.active eq 'profile'}">aria-current="page"</c:if> href="${pageContext.request.contextPath}/farmer/profile">Profile</a></li>
                    </c:when>
                    <c:when test="${sessionScope.authenticatedUser.role eq 'ADMIN'}">
                        <li><a class="site-nav-link<c:if test='${param.active eq "admin-dashboard"}'> is-active</c:if>" <c:if test="${param.active eq 'admin-dashboard'}">aria-current="page"</c:if> href="${pageContext.request.contextPath}/admin/complaints">Admin dashboard</a></li>
                        <li><a class="site-nav-link<c:if test='${param.active eq "admin-complaints"}'> is-active</c:if>" <c:if test="${param.active eq 'admin-complaints'}">aria-current="page"</c:if> href="${pageContext.request.contextPath}/admin/complaints#admin-complaints">Complaint management</a></li>
                        <li><a class="site-nav-link<c:if test='${param.active eq "advisory"}'> is-active</c:if>" <c:if test="${param.active eq 'advisory'}">aria-current="page"</c:if> href="${pageContext.request.contextPath}/advisories">Crop advisory</a></li>
                    </c:when>
                    <c:otherwise>
                        <li><a class="site-nav-link" href="${pageContext.request.contextPath}/">Home</a></li>
                        <li><a class="site-nav-link<c:if test='${param.active eq "report"}'> is-active</c:if>" <c:if test="${param.active eq 'report'}">aria-current="page"</c:if> href="${pageContext.request.contextPath}/report-issue.jsp">Report issue</a></li>
                        <li><a class="site-nav-link<c:if test='${param.active eq "tracking"}'> is-active</c:if>" <c:if test="${param.active eq 'tracking'}">aria-current="page"</c:if> href="${pageContext.request.contextPath}/track-complaint.jsp">Track complaint</a></li>
                        <li><a class="site-nav-link<c:if test='${param.active eq "advisory"}'> is-active</c:if>" <c:if test="${param.active eq 'advisory'}">aria-current="page"</c:if> href="${pageContext.request.contextPath}/advisories">Crop advisory</a></li>
                        <li><a class="site-nav-link<c:if test='${param.active eq "login"}'> is-active</c:if>" <c:if test="${param.active eq 'login'}">aria-current="page"</c:if> href="${pageContext.request.contextPath}/login.jsp">Login</a></li>
                        <li><a class="site-nav-register<c:if test='${param.active eq "register"}'> is-active</c:if>" <c:if test="${param.active eq 'register'}">aria-current="page"</c:if> href="${pageContext.request.contextPath}/register.jsp">Create account</a></li>
                    </c:otherwise>
                </c:choose>
                <c:if test="${not empty sessionScope.authenticatedUser}">
                    <li class="site-nav-account"><span>Signed in as <strong><c:out value="${sessionScope.authenticatedUser.displayName}"/></strong></span></li>
                    <li class="site-nav-logout">
                        <form action="${pageContext.request.contextPath}/logout" method="post">
                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                            <button class="btn btn-outline-green btn-sm" type="submit">Sign out</button>
                        </form>
                    </li>
                </c:if>
            </ul>
        </details>
    </nav>
</header>
