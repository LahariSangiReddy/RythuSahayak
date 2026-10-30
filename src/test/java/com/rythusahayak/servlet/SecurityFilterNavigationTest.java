package com.rythusahayak.servlet;

import com.rythusahayak.filter.SecurityFilter;
import com.rythusahayak.model.SessionUser;
import com.rythusahayak.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityFilterNavigationTest {
    @Test
    void signedInFarmerCannotRevisitLoginOrRegistrationPages() throws Exception {
        assertSignedInRedirect("/login.jsp", User.Role.FARMER, "/rythusahayak/farmer/complaints");
        assertSignedInRedirect("/register.jsp", User.Role.FARMER, "/rythusahayak/farmer/complaints");
    }

    @Test
    void signedInAdministratorCannotRevisitLoginOrRegistrationPages() throws Exception {
        assertSignedInRedirect("/login.jsp", User.Role.ADMIN, "/rythusahayak/admin/complaints");
        assertSignedInRedirect("/register.jsp", User.Role.ADMIN, "/rythusahayak/admin/complaints");
    }

    @Test
    void signedOutVisitorCanOpenPublicAuthenticationPages() throws Exception {
        for (String path : new String[]{"/login.jsp", "/register.jsp"}) {
            ServletTestSupport.RequestFixture request =
                    ServletTestSupport.request(java.util.Map.of(), "GET", path, null);
            ServletTestSupport.ResponseFixture response = ServletTestSupport.response();
            boolean[] continued = {false};

            new SecurityFilter().doFilter(request.request(), response.response(),
                    ServletTestSupport.chain(() -> continued[0] = true));

            assertTrue(continued[0], path);
            assertNull(response.redirect(), path);
        }
    }

    @Test
    void signedInFarmerDoesNotSeeAdministratorRoute() throws Exception {
        ServletTestSupport.SessionFixture session = session(User.Role.FARMER);
        ServletTestSupport.RequestFixture request = ServletTestSupport.request(
                java.util.Map.of(), "GET", "/admin/complaints", session);
        ServletTestSupport.ResponseFixture response = ServletTestSupport.response();
        boolean[] continued = {false};

        new SecurityFilter().doFilter(request.request(), response.response(),
                ServletTestSupport.chain(() -> continued[0] = true));

        assertFalse(continued[0]);
        assertTrue(response.errorSent());
        assertEquals(403, response.errorStatus());
    }

    private void assertSignedInRedirect(String path, User.Role role, String expected) throws Exception {
        ServletTestSupport.RequestFixture request = ServletTestSupport.request(
                java.util.Map.of(), "GET", path, session(role));
        ServletTestSupport.ResponseFixture response = ServletTestSupport.response();
        boolean[] continued = {false};

        new SecurityFilter().doFilter(request.request(), response.response(),
                ServletTestSupport.chain(() -> continued[0] = true));

        assertFalse(continued[0], path);
        assertEquals(expected, response.redirect(), path);
    }

    private ServletTestSupport.SessionFixture session(User.Role role) {
        ServletTestSupport.SessionFixture session = new ServletTestSupport.SessionFixture();
        session.setAttribute("authenticatedUser", new SessionUser(7, "Test User", role));
        session.setAttribute("csrfToken", "test-token");
        return session;
    }
}
