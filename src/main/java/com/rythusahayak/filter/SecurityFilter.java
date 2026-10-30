package com.rythusahayak.filter;

import com.rythusahayak.model.SessionUser;
import com.rythusahayak.model.User;
import com.rythusahayak.util.CsrfToken;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class SecurityFilter implements Filter {
    private static final String SESSION_USER = "authenticatedUser";
    private static final String SESSION_CSRF = "csrfToken";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpRequest.setCharacterEncoding(StandardCharsets.UTF_8.name());
        httpResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "DENY");
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        httpResponse.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");

        String requestPath = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        if (requestPath.startsWith("/admin/") || requestPath.startsWith("/farmer/")
                || requestPath.equals("/login.jsp") || requestPath.equals("/register.jsp")) {
            httpResponse.setHeader("Cache-Control", "no-store");
        }

        HttpServletResponse securedResponse = new SessionCookieResponse(httpResponse, httpRequest.isSecure());
        if ("GET".equalsIgnoreCase(httpRequest.getMethod()) && isJspPage(requestPath)) {
            HttpSession session = httpRequest.getSession(true);
            if (session.getAttribute(SESSION_CSRF) == null) {
                session.setAttribute(SESSION_CSRF, CsrfToken.generate());
            }
        }

        if ("POST".equalsIgnoreCase(httpRequest.getMethod()) && !validCsrfToken(httpRequest)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "The form expired. Reload the page and try again.");
            return;
        }

        if ("GET".equalsIgnoreCase(httpRequest.getMethod())
                && (requestPath.equals("/login.jsp") || requestPath.equals("/register.jsp"))) {
            SessionUser user = currentUser(httpRequest);
            if (user != null) {
                String destination = user.role() == User.Role.ADMIN
                        ? "/admin/complaints" : "/farmer/complaints";
                httpResponse.sendRedirect(httpRequest.getContextPath() + destination);
                return;
            }
        }

        if (requestPath.startsWith("/admin/") && !hasRole(httpRequest, User.Role.ADMIN)) {
            SessionUser user = currentUser(httpRequest);
            if (user != null) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            } else {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp?required=admin");
            }
            return;
        }

        if (requestPath.startsWith("/farmer/") && !hasRole(httpRequest, User.Role.FARMER)) {
            SessionUser user = currentUser(httpRequest);
            if (user != null) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            } else {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp");
            }
            return;
        }

        chain.doFilter(httpRequest, securedResponse);
    }

    private boolean isJspPage(String requestPath) {
        return requestPath.endsWith(".jsp") || requestPath.equals("/login")
                || requestPath.equals("/register") || requestPath.equals("/admin/complaints")
                || requestPath.equals("/farmer/complaints");
    }

    private boolean validCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object expected = session.getAttribute(SESSION_CSRF);
        String supplied = request.getParameter(SESSION_CSRF);
        return expected instanceof String expectedToken && supplied != null
                && MessageDigest.isEqual(expectedToken.getBytes(StandardCharsets.UTF_8),
                        supplied.getBytes(StandardCharsets.UTF_8));
    }

    private boolean hasRole(HttpServletRequest request, User.Role role) {
        SessionUser user = currentUser(request);
        return user != null && user.role() == role;
    }

    private SessionUser currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object user = session == null ? null : session.getAttribute(SESSION_USER);
        return user instanceof SessionUser sessionUser ? sessionUser : null;
    }

    @Override
    public void destroy() {
    }

    private static final class SessionCookieResponse extends HttpServletResponseWrapper {
        private final boolean secureRequest;

        private SessionCookieResponse(HttpServletResponse response, boolean secureRequest) {
            super(response);
            this.secureRequest = secureRequest;
        }

        @Override
        public void addCookie(Cookie cookie) {
            if ("JSESSIONID".equalsIgnoreCase(cookie.getName())) {
                cookie.setHttpOnly(true);
                cookie.setAttribute("SameSite", "Lax");
                if (secureRequest) {
                    cookie.setSecure(true);
                }
            }
            super.addCookie(cookie);
        }

        @Override
        public void addHeader(String name, String value) {
            super.addHeader(name, secureSessionCookie(name, value));
        }

        @Override
        public void setHeader(String name, String value) {
            super.setHeader(name, secureSessionCookie(name, value));
        }

        private String secureSessionCookie(String name, String value) {
            if (!"Set-Cookie".equalsIgnoreCase(name) || value == null
                    || !value.regionMatches(true, 0, "JSESSIONID=", 0, "JSESSIONID=".length())) {
                return value;
            }
            String secured = value;
            if (!secured.toLowerCase(java.util.Locale.ROOT).contains("; httponly")) {
                secured += "; HttpOnly";
            }
            if (!secured.toLowerCase(java.util.Locale.ROOT).contains("; samesite=")) {
                secured += "; SameSite=Lax";
            }
            if (secureRequest && !secured.toLowerCase(java.util.Locale.ROOT).contains("; secure")) {
                secured += "; Secure";
            }
            return secured;
        }
    }
}
