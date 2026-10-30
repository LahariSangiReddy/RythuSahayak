package com.rythusahayak.servlet;

import com.rythusahayak.dao.UserDAO;
import com.rythusahayak.model.SessionUser;
import com.rythusahayak.model.User;
import com.rythusahayak.util.CsrfToken;
import com.rythusahayak.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final UserDAO userDAO;

    public LoginServlet() {
        this(new UserDAO());
    }

    LoginServlet(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String email = value(request, "email").toLowerCase(Locale.ROOT);
        String password = request.getParameter("password");
        if (email.length() > 254 || !EMAIL_PATTERN.matcher(email).matches()
                || password == null || password.isBlank() || password.length() > 72) {
            loginFailure(request, response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Email or password is incorrect.");
            return;
        }

        try {
            Optional<User> candidate = userDAO.findByEmail(email);
            if (candidate.isEmpty() || !candidate.get().active()
                    || !PasswordUtil.verify(password, candidate.get().passwordHash())) {
                loginFailure(request, response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Email or password is incorrect.");
                return;
            }

            User user = candidate.get();
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession session = request.getSession(true);
            session.setMaxInactiveInterval(30 * 60);
            session.setAttribute("authenticatedUser",
                    new SessionUser(user.id(), user.fullName(), user.role()));
            session.setAttribute("csrfToken", CsrfToken.generate());
            String destination = user.role() == User.Role.ADMIN
                    ? "/admin/complaints" : "/farmer/complaints";
            response.sendRedirect(request.getContextPath() + destination);
        } catch (SQLException exception) {
            logDatabaseFailure("Login", exception);
            loginFailure(request, response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Sign-in is temporarily unavailable. Please try again later.");
        }
    }

    private void loginFailure(HttpServletRequest request, HttpServletResponse response, int status,
                              String message) throws ServletException, IOException {
        request.setAttribute("loginError", message);
        request.setAttribute("loginEmail", value(request, "email"));
        response.setStatus(status);
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private void logDatabaseFailure(String operation, SQLException exception) {
        LOGGER.log(Level.SEVERE, "{0} failed (SQLState {1}, error {2}).",
                new Object[]{operation, exception.getSQLState(), exception.getErrorCode()});
    }
}
