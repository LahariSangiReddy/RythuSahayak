<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         import="com.rythusahayak.model.SessionUser,com.rythusahayak.model.User" %>
<%
    Object authenticatedUser = session.getAttribute("authenticatedUser");
    String destination = "/login.jsp";
    if (authenticatedUser instanceof SessionUser) {
        SessionUser user = (SessionUser) authenticatedUser;
        destination = user.role() == User.Role.ADMIN ? "/admin/complaints" : "/farmer/complaints";
    }
    response.sendRedirect(request.getContextPath() + destination);
%>
