package com.rythusahayak.servlet;

import com.rythusahayak.dao.UserDAO;
import com.rythusahayak.model.User;
import com.rythusahayak.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(RegisterServlet.class.getName());
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[6-9][0-9]{9}$");
    private final UserDAO userDAO;

    public RegisterServlet() {
        this(new UserDAO());
    }

    RegisterServlet(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String fullName = value(request, "fullName");
        String email = value(request, "email").toLowerCase(Locale.ROOT);
        String mobile = value(request, "mobile");
        String password = request.getParameter("password");
        String confirmation = request.getParameter("confirmPassword");
        request.setAttribute("formFullName", fullName);
        request.setAttribute("formEmail", email);
        request.setAttribute("formMobile", mobile);

        String error = validate(fullName, email, mobile, password, confirmation);
        if (error != null) {
            request.setAttribute("registerError", error);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        try {
            userDAO.createUser(fullName, email, mobile.isEmpty() ? null : mobile,
                    PasswordUtil.hash(password), User.Role.FARMER);
            request.getSession(true).setAttribute("registrationSuccess",
                    "Your farmer account is ready. Sign in to continue.");
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        } catch (SQLException exception) {
            if (exception.getErrorCode() == 1062) {
                request.setAttribute("registerError", "An account with that email address already exists.");
                response.setStatus(HttpServletResponse.SC_CONFLICT);
            } else {
                LOGGER.log(Level.SEVERE, "Farmer registration failed (SQLState {0}, error {1}).",
                        new Object[]{exception.getSQLState(), exception.getErrorCode()});
                request.setAttribute("registerError", "Registration is temporarily unavailable. Please try again later.");
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            }
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }

    private String validate(String fullName, String email, String mobile, String password, String confirmation) {
        if (fullName.length() < 2 || fullName.length() > 80 || containsControlCharacter(fullName)) {
            return "Enter a name between 2 and 80 characters.";
        }
        if (email.length() > 254 || !EMAIL_PATTERN.matcher(email).matches()) {
            return "Enter a valid email address.";
        }
        if (!mobile.isEmpty() && !MOBILE_PATTERN.matcher(mobile).matches()) {
            return "Enter a valid 10-digit Indian mobile number or leave the field blank.";
        }
        if (password == null || password.isBlank() || password.length() < 12 || password.length() > 72) {
            return "Choose a non-blank password between 12 and 72 characters.";
        }
        if (!password.equals(confirmation)) {
            return "The passwords do not match.";
        }
        return null;
    }

    private boolean containsControlCharacter(String value) {
        return value.codePoints().anyMatch(Character::isISOControl);
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }
}
