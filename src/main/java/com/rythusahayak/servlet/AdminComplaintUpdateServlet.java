package com.rythusahayak.servlet;

import com.rythusahayak.dao.ComplaintDAO;
import com.rythusahayak.model.Complaint;
import com.rythusahayak.model.SessionUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/admin/complaints/update")
public class AdminComplaintUpdateServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AdminComplaintUpdateServlet.class.getName());
    private final ComplaintDAO complaintDAO = new ComplaintDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        long complaintId;
        Complaint.Status status;
        String remark = value(request, "publicRemark");
        try {
            complaintId = Long.parseLong(request.getParameter("complaintId"));
            status = Complaint.Status.valueOf(request.getParameter("status"));
        } catch (IllegalArgumentException | NullPointerException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid complaint update.");
            return;
        }
        if (complaintId < 1 || remark.length() < 3 || remark.length() > 500
                || remark.codePoints().anyMatch(Character::isISOControl)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Enter a valid status and public remark.");
            return;
        }

        Object value = request.getSession(false).getAttribute("authenticatedUser");
        if (!(value instanceof SessionUser administrator)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        try {
            if (!complaintDAO.updateStatus(complaintId, administrator.id(), status, remark)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Complaint not found.");
                return;
            }
            request.getSession(false).setAttribute("adminFlash", "Complaint status and public remark were updated.");
            response.sendRedirect(request.getContextPath() + "/admin/complaints");
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Administrator status update failed (SQLState {0}, error {1}).",
                    new Object[]{exception.getSQLState(), exception.getErrorCode()});
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "The update could not be saved. No success was recorded; please try again.");
        }
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }
}
