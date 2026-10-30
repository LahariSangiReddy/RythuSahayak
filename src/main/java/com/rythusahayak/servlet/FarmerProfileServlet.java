package com.rythusahayak.servlet;

import com.rythusahayak.dao.UserDAO;
import com.rythusahayak.model.FarmerProfile;
import com.rythusahayak.model.SessionUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/farmer/profile")
public class FarmerProfileServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(FarmerProfileServlet.class.getName());
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        SessionUser sessionUser = (SessionUser) request.getSession(false).getAttribute("authenticatedUser");
        try {
            Optional<FarmerProfile> profile = userDAO.findFarmerProfile(sessionUser.id());
            if (profile.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Farmer account not found.");
                return;
            }
            request.setAttribute("profile", profile.get());
            request.setAttribute("displayName", sessionUser.displayName());
            request.getRequestDispatcher("/farmer/profile.jsp").forward(request, response);
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Farmer profile lookup failed (SQLState {0}, error {1}).",
                    new Object[]{exception.getSQLState(), exception.getErrorCode()});
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Your profile is temporarily unavailable.");
        }
    }
}
