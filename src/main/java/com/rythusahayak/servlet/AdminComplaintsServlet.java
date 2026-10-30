package com.rythusahayak.servlet;

import com.rythusahayak.dao.ComplaintDAO;
import com.rythusahayak.model.Complaint;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/admin/complaints")
public class AdminComplaintsServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AdminComplaintsServlet.class.getName());
    private final ComplaintDAO complaintDAO = new ComplaintDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Complaint.Status status = null;
        String selectedStatus = request.getParameter("status");
        if (selectedStatus != null && !selectedStatus.isBlank()) {
            try {
                status = Complaint.Status.valueOf(selectedStatus);
            } catch (IllegalArgumentException exception) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid status filter.");
                return;
            }
        }

        HttpSession session = request.getSession(false);
        Object flash = session.getAttribute("adminFlash");
        if (flash instanceof String message) {
            request.setAttribute("adminFlash", message);
            session.removeAttribute("adminFlash");
        }
        request.setAttribute("selectedStatus", status == null ? "" : status.name());
        try {
            List<Complaint> complaints = complaintDAO.findAll(status, 100, 0);
            request.setAttribute("complaints", complaints);
            request.setAttribute("complaintSummary", complaintDAO.summarizeAll());
            request.getRequestDispatcher("/admin/dashboard.jsp").forward(request, response);
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Administrator complaint list failed (SQLState {0}, error {1}).",
                    new Object[]{exception.getSQLState(), exception.getErrorCode()});
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "The administrator complaint list is temporarily unavailable.");
        }
    }
}
