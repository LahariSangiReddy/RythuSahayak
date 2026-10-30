package com.rythusahayak.servlet;

import com.rythusahayak.dao.ComplaintDAO;
import com.rythusahayak.model.Complaint;
import com.rythusahayak.model.SessionUser;
import com.rythusahayak.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@WebServlet("/complaints/create")
public class ComplaintCreateServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ComplaintCreateServlet.class.getName());
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[6-9][0-9]{9}$");
    private static final List<String> CROPS =
            List.of("Rice", "Cotton", "Tomato", "Chilli", "Maize", "Groundnut", "Other");
    private static final List<String> CATEGORIES =
            List.of("Crop Disease", "Pest Concern", "Irrigation", "Soil/Nutrition", "Weather Damage", "Other");
    private final ComplaintDAO complaintDAO;

    public ComplaintCreateServlet() {
        this(new ComplaintDAO());
    }

    ComplaintCreateServlet(ComplaintDAO complaintDAO) {
        this.complaintDAO = complaintDAO;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Map<String, String> values = new LinkedHashMap<>();
        for (String name : List.of("farmerName", "mobile", "village", "district",
                "crop", "category", "description", "observedDate")) {
            String value = request.getParameter(name);
            values.put(name, value == null ? "" : value.trim());
        }
        request.setAttribute("formValues", values);

        String validationMessage = validate(values);
        if (validationMessage != null) {
            request.setAttribute("reportError", validationMessage);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.getRequestDispatcher("/report-issue.jsp").forward(request, response);
            return;
        }

        try {
            LocalDate observedDate = LocalDate.parse(values.get("observedDate"));
            SessionUser sessionUser = getSessionUser(request);
            Long userId = sessionUser != null && sessionUser.role() == User.Role.FARMER
                    ? sessionUser.id() : null;
            Complaint complaint = complaintDAO.createComplaint(
                    values.get("farmerName"), values.get("mobile"), values.get("village"),
                    values.get("district"), values.get("crop"), values.get("category"),
                    values.get("description"), observedDate, userId);
            if (userId != null) {
                request.getSession(true).setAttribute("farmerCreatedReference", complaint.referenceNumber());
                response.sendRedirect(request.getContextPath() + "/farmer/complaints");
            } else {
                request.getSession(true).setAttribute("createdComplaintReference", complaint.referenceNumber());
                response.sendRedirect(request.getContextPath() + "/complaints/created");
            }
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Complaint creation failed (SQLState {0}, error {1}).",
                    new Object[]{exception.getSQLState(), exception.getErrorCode()});
            request.setAttribute("reportError",
                    "Your report could not be saved. No reference was created; please try again later.");
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            request.getRequestDispatcher("/report-issue.jsp").forward(request, response);
        }
    }

    String validate(Map<String, String> values) {
        if (!validLength(values.get("farmerName"), 2, 80)
                || containsControlCharacter(values.get("farmerName"))) {
            return "Enter a farmer name between 2 and 80 characters.";
        }
        if (!MOBILE_PATTERN.matcher(values.get("mobile")).matches()) {
            return "Enter a valid 10-digit Indian mobile number starting with 6, 7, 8 or 9.";
        }
        if (!validLength(values.get("village"), 2, 80)
                || containsControlCharacter(values.get("village"))) {
            return "Enter a village name between 2 and 80 characters.";
        }
        if (!validLength(values.get("district"), 2, 80)
                || containsControlCharacter(values.get("district"))) {
            return "Enter a district between 2 and 80 characters.";
        }
        if (!CROPS.contains(values.get("crop"))) {
            return "Choose one of the listed crops.";
        }
        if (!CATEGORIES.contains(values.get("category"))) {
            return "Choose one of the listed problem categories.";
        }
        if (!validLength(values.get("description"), 20, 1000)) {
            return "Describe the issue in 20 to 1000 characters.";
        }
        try {
            LocalDate observed = LocalDate.parse(values.get("observedDate"));
            if (observed.isAfter(LocalDate.now())) {
                return "The observed date cannot be in the future.";
            }
        } catch (DateTimeParseException exception) {
            return "Enter a valid date when the issue was observed.";
        }
        return null;
    }

    private boolean validLength(String value, int minimum, int maximum) {
        return value != null && value.length() >= minimum && value.length() <= maximum;
    }

    private boolean containsControlCharacter(String value) {
        return value.codePoints().anyMatch(Character::isISOControl);
    }

    private SessionUser getSessionUser(HttpServletRequest request) {
        Object value = request.getSession(false) == null
                ? null : request.getSession(false).getAttribute("authenticatedUser");
        return value instanceof SessionUser user ? user : null;
    }
}
