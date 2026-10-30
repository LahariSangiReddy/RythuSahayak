package com.rythusahayak.servlet;

import com.rythusahayak.dao.ComplaintDAO;
import com.rythusahayak.model.PublicComplaint;
import com.rythusahayak.util.ComplaintReference;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/complaints/track")
public class ComplaintTrackingServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ComplaintTrackingServlet.class.getName());
    private final ComplaintDAO complaintDAO = new ComplaintDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String reference = request.getParameter("reference");
        reference = reference == null ? "" : reference.trim().toUpperCase(Locale.ROOT);
        request.setAttribute("trackingReference", reference);
        if (!ComplaintReference.isValid(reference)) {
            request.setAttribute("trackingError", "Enter a valid complaint reference.");
            request.getRequestDispatcher("/track-complaint.jsp").forward(request, response);
            return;
        }

        try {
            Optional<PublicComplaint> match = complaintDAO.findPublicByReference(reference);
            if (match.isEmpty()) {
                request.setAttribute("trackingError", "No complaint matches that reference.");
            } else {
                request.setAttribute("trackingComplaint", match.get());
            }
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Complaint tracking failed (SQLState {0}, error {1}).",
                    new Object[]{exception.getSQLState(), exception.getErrorCode()});
            request.setAttribute("trackingError",
                    "Complaint tracking is temporarily unavailable. Please try again later.");
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
        request.getRequestDispatcher("/track-complaint.jsp").forward(request, response);
    }
}
