package com.rythusahayak.servlet;

import com.rythusahayak.dao.ComplaintDAO;
import com.rythusahayak.model.Complaint;
import com.rythusahayak.model.ComplaintSummary;
import com.rythusahayak.model.SessionUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/farmer/complaints")
public class FarmerComplaintsServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(FarmerComplaintsServlet.class.getName());
    private final ComplaintDAO complaintDAO = new ComplaintDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        SessionUser user = (SessionUser) request.getSession(false).getAttribute("authenticatedUser");
        request.setAttribute("displayName", user.displayName());
        try {
            List<Complaint> complaints = complaintDAO.findByUserId(user.id(), 5);
            ComplaintSummary summary = complaintDAO.summarizeByUserId(user.id());
            request.setAttribute("complaints", complaints);
            request.setAttribute("complaintSummary", summary);
            Object createdReference = request.getSession(false).getAttribute("farmerCreatedReference");
            if (createdReference instanceof String reference) {
                request.setAttribute("createdReference", reference);
                request.getSession(false).removeAttribute("farmerCreatedReference");
            }
            request.getRequestDispatcher("/farmer/dashboard.jsp").forward(request, response);
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Farmer complaint list failed (SQLState {0}, error {1}).",
                    new Object[]{exception.getSQLState(), exception.getErrorCode()});
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Your complaint list is temporarily unavailable.");
        }
    }
}
