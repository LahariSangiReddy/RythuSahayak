package com.rythusahayak.servlet;

import com.rythusahayak.dao.UserDAO;
import com.rythusahayak.filter.SecurityFilter;
import com.rythusahayak.model.SessionUser;
import com.rythusahayak.model.User;
import com.rythusahayak.util.PasswordUtil;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationServletTest {
    private static final String PASSWORD = "a-safe-test-password";

    @Test
    void registrationHashesPasswordAndRedirectsToLogin() throws IOException, ServletException {
        AtomicReference<String> storedPasswordHash = new AtomicReference<>();
        AtomicReference<User.Role> storedRole = new AtomicReference<>();
        UserDAO userDAO = new UserDAO() {
            @Override
            public long createUser(String fullName, String email, String mobile,
                                   String passwordHash, User.Role role) {
                storedPasswordHash.set(passwordHash);
                storedRole.set(role);
                return 42;
            }
        };
        ServletTestSupport.RequestFixture request = ServletTestSupport.request(
                registrationParameters(PASSWORD, PASSWORD), "POST", "/register", null);
        ServletTestSupport.ResponseFixture response = ServletTestSupport.response();

        new RegisterServlet(userDAO).doPost(request.request(), response.response());

        assertEquals("/rythusahayak/login.jsp", response.redirect());
        assertEquals(User.Role.FARMER, storedRole.get());
        assertNotNull(storedPasswordHash.get());
        assertFalse(PASSWORD.equals(storedPasswordHash.get()));
        assertTrue(PasswordUtil.verify(PASSWORD, storedPasswordHash.get()));
        assertNotNull(request.session().attribute("registrationSuccess"));
    }

    @Test
    void registrationRejectsMismatchedPasswordsWithoutInserting() throws IOException, ServletException {
        AtomicInteger insertCalls = new AtomicInteger();
        UserDAO userDAO = new UserDAO() {
            @Override
            public long createUser(String fullName, String email, String mobile,
                                   String passwordHash, User.Role role) {
                insertCalls.incrementAndGet();
                return 42;
            }
        };
        ServletTestSupport.RequestFixture request = ServletTestSupport.request(
                registrationParameters(PASSWORD, "different-password"), "POST", "/register", null);
        ServletTestSupport.ResponseFixture response = ServletTestSupport.response();

        new RegisterServlet(userDAO).doPost(request.request(), response.response());

        assertEquals(0, insertCalls.get());
        assertEquals(400, response.status());
        assertEquals("/register.jsp", request.forwardedPath());
        assertEquals("The passwords do not match.", request.attribute("registerError"));
    }

    @Test
    void registrationShowsFriendlyDuplicateEmailError() throws IOException, ServletException {
        UserDAO userDAO = new UserDAO() {
            @Override
            public long createUser(String fullName, String email, String mobile,
                                   String passwordHash, User.Role role) throws SQLException {
                throw new SQLException("duplicate key", "23000", 1062);
            }
        };
        ServletTestSupport.RequestFixture request = ServletTestSupport.request(
                registrationParameters(PASSWORD, PASSWORD), "POST", "/register", null);
        ServletTestSupport.ResponseFixture response = ServletTestSupport.response();

        new RegisterServlet(userDAO).doPost(request.request(), response.response());

        assertEquals(409, response.status());
        assertEquals("/register.jsp", request.forwardedPath());
        assertEquals("An account with that email address already exists.", request.attribute("registerError"));
    }

    @Test
    void successfulLoginInvalidatesOldSessionAndCreatesFarmerSession() throws IOException, ServletException {
        User farmer = user(PasswordUtil.hash(PASSWORD));
        UserDAO userDAO = new UserDAO() {
            @Override
            public Optional<User> findByEmail(String email) {
                assertEquals("farmer@example.test", email);
                return Optional.of(farmer);
            }
        };
        ServletTestSupport.SessionFixture oldSession = new ServletTestSupport.SessionFixture();
        oldSession.httpSession().setAttribute("csrfToken", "old-token");
        ServletTestSupport.RequestFixture request = ServletTestSupport.request(
                Map.of("email", "Farmer@Example.Test", "password", PASSWORD), "POST", "/login", oldSession);
        ServletTestSupport.ResponseFixture response = ServletTestSupport.response();

        new LoginServlet(userDAO).doPost(request.request(), response.response());

        assertTrue(oldSession.invalidated());
        assertEquals("/rythusahayak/farmer/complaints", response.redirect());
        assertNotNull(request.session().attribute("authenticatedUser"));
        assertNotNull(request.session().attribute("csrfToken"));
    }

    @Test
    void invalidLoginReturnsGenericMessage() throws IOException, ServletException {
        UserDAO userDAO = new UserDAO() {
            @Override
            public Optional<User> findByEmail(String email) {
                return Optional.empty();
            }
        };
        ServletTestSupport.RequestFixture request = ServletTestSupport.request(
                Map.of("email", "farmer@example.test", "password", PASSWORD), "POST", "/login", null);
        ServletTestSupport.ResponseFixture response = ServletTestSupport.response();

        new LoginServlet(userDAO).doPost(request.request(), response.response());

        assertEquals(401, response.status());
        assertEquals("/login.jsp", request.forwardedPath());
        assertEquals("Email or password is incorrect.", request.attribute("loginError"));
        assertNull(response.redirect());
    }

    @Test
    void unauthenticatedFarmerRouteRedirectsToLogin() throws IOException, ServletException {
        ServletTestSupport.RequestFixture request = ServletTestSupport.request(
                Map.of(), "GET", "/farmer/complaints", null);
        ServletTestSupport.ResponseFixture response = ServletTestSupport.response();
        AtomicInteger chainCalls = new AtomicInteger();

        new SecurityFilter().doFilter(request.request(), response.response(),
                ServletTestSupport.chain(chainCalls::incrementAndGet));

        assertEquals("/rythusahayak/login.jsp", response.redirect());
        assertEquals(0, chainCalls.get());
    }

    @Test
    void farmerCannotOpenAdministratorRoutes() throws IOException, ServletException {
        ServletTestSupport.SessionFixture session = new ServletTestSupport.SessionFixture();
        session.httpSession().setAttribute("authenticatedUser",
                new SessionUser(7, "Ravi Kumar", User.Role.FARMER));
        ServletTestSupport.RequestFixture request = ServletTestSupport.request(
                Map.of(), "GET", "/admin/complaints", session);
        ServletTestSupport.ResponseFixture response = ServletTestSupport.response();
        AtomicInteger chainCalls = new AtomicInteger();

        new SecurityFilter().doFilter(request.request(), response.response(),
                ServletTestSupport.chain(chainCalls::incrementAndGet));

        assertTrue(response.errorSent());
        assertEquals(403, response.errorStatus());
        assertEquals(0, chainCalls.get());
    }

    @Test
    void postWithoutCsrfTokenIsRejected() throws IOException, ServletException {
        ServletTestSupport.RequestFixture request = ServletTestSupport.request(
                Map.of(), "POST", "/complaints/create", new ServletTestSupport.SessionFixture());
        ServletTestSupport.ResponseFixture response = ServletTestSupport.response();
        AtomicInteger chainCalls = new AtomicInteger();

        new SecurityFilter().doFilter(request.request(), response.response(),
                ServletTestSupport.chain(chainCalls::incrementAndGet));

        assertTrue(response.errorSent());
        assertEquals(403, response.errorStatus());
        assertEquals(0, chainCalls.get());
    }

    @Test
    void logoutInvalidatesSessionAndReturnsToApplicationRoot() throws IOException {
        ServletTestSupport.SessionFixture session = new ServletTestSupport.SessionFixture();
        session.httpSession().setAttribute("authenticatedUser",
                new SessionUser(7, "Ravi Kumar", User.Role.FARMER));
        ServletTestSupport.RequestFixture request = ServletTestSupport.request(
                Map.of("csrfToken", "test-token"), "POST", "/logout", session);
        ServletTestSupport.ResponseFixture response = ServletTestSupport.response();

        new LogoutServlet().doPost(request.request(), response.response());

        assertTrue(session.invalidated());
        assertEquals("/rythusahayak/", response.redirect());
    }

    private Map<String, String> registrationParameters(String password, String confirmation) {
        return Map.of(
                "fullName", "Ravi Kumar",
                "email", "farmer@example.test",
                "mobile", "9876543210",
                "password", password,
                "confirmPassword", confirmation);
    }

    private User user(String passwordHash) {
        return new User(7, "Ravi Kumar", "farmer@example.test", "9876543210",
                passwordHash, User.Role.FARMER, Instant.now(), true);
    }
}
