package com.rythusahayak.servlet;

import com.rythusahayak.dao.ComplaintDAO;
import com.rythusahayak.model.Complaint;
import com.rythusahayak.model.SessionUser;
import com.rythusahayak.model.User;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComplaintCreateServletTest {
    private final ComplaintCreateServlet servlet = new ComplaintCreateServlet();

    @Test
    void acceptsValidComplaintInput() {
        assertNull(servlet.validate(validValues()));
    }

    @Test
    void rejectsMissingRequiredFields() {
        Map<String, String> values = validValues();
        values.put("farmerName", "");

        assertNotNull(servlet.validate(values));
    }

    @Test
    void rejectsInvalidAndFutureDates() {
        Map<String, String> values = validValues();
        values.put("observedDate", "2026-02-30");
        assertNotNull(servlet.validate(values));

        values.put("observedDate", LocalDate.now().plusDays(1).toString());
        assertNotNull(servlet.validate(values));
    }

    @Test
    void rejectsInvalidMobileAndUnlistedCropValues() {
        Map<String, String> values = validValues();
        values.put("mobile", "1234567890");
        assertNotNull(servlet.validate(values));

        values = validValues();
        values.put("crop", "Unlisted crop");
        assertNotNull(servlet.validate(values));
    }

    @Test
    void authenticatedComplaintUsesSessionFarmerAndShowsCreatedReference() throws Exception {
        AtomicReference<Long> insertedUserId = new AtomicReference<>();
        ComplaintDAO complaintDAO = new ComplaintDAO() {
            @Override
            public Complaint createComplaint(String farmerName, String mobile, String village, String district,
                                             String crop, String category, String description,
                                             LocalDate observedDate, Long userId) {
                insertedUserId.set(userId);
                return createdComplaint(userId);
            }
        };
        ServletTestSupport.SessionFixture session = new ServletTestSupport.SessionFixture();
        session.httpSession().setAttribute("authenticatedUser",
                new SessionUser(73, "Ravi Kumar", User.Role.FARMER));
        Map<String, String> parameters = validValues();
        parameters.put("userId", "999");
        ServletTestSupport.RequestFixture request = ServletTestSupport.request(
                parameters, "POST", "/complaints/create", session);
        ServletTestSupport.ResponseFixture response = ServletTestSupport.response();

        new ComplaintCreateServlet(complaintDAO).doPost(request.request(), response.response());

        assertEquals(73L, insertedUserId.get());
        assertEquals("/rythusahayak/farmer/complaints", response.redirect());
        assertEquals("RYT-20261029-1234567890ABCDEF", session.attribute("farmerCreatedReference"));
    }

    @Test
    void guestComplaintShowsConfirmationOnlyAfterDaoSuccess() throws Exception {
        ComplaintDAO complaintDAO = new ComplaintDAO() {
            @Override
            public Complaint createComplaint(String farmerName, String mobile, String village, String district,
                                             String crop, String category, String description,
                                             LocalDate observedDate, Long userId) {
                assertNull(userId);
                return createdComplaint(null);
            }
        };
        ServletTestSupport.RequestFixture request = ServletTestSupport.request(
                validValues(), "POST", "/complaints/create", null);
        ServletTestSupport.ResponseFixture response = ServletTestSupport.response();

        new ComplaintCreateServlet(complaintDAO).doPost(request.request(), response.response());

        assertEquals("/rythusahayak/complaints/created", response.redirect());
        assertEquals("RYT-20261029-1234567890ABCDEF",
                request.session().attribute("createdComplaintReference"));
    }

    @Test
    void databaseFailureDoesNotCreateReferenceOrShowSuccess() throws Exception {
        ComplaintDAO complaintDAO = new ComplaintDAO() {
            @Override
            public Complaint createComplaint(String farmerName, String mobile, String village, String district,
                                             String crop, String category, String description,
                                             LocalDate observedDate, Long userId) throws SQLException {
                throw new SQLException("database unavailable", "08001", 0);
            }
        };
        ServletTestSupport.SessionFixture session = new ServletTestSupport.SessionFixture();
        ServletTestSupport.RequestFixture request = ServletTestSupport.request(
                validValues(), "POST", "/complaints/create", session);
        ServletTestSupport.ResponseFixture response = ServletTestSupport.response();

        new ComplaintCreateServlet(complaintDAO).doPost(request.request(), response.response());

        assertEquals(503, response.status());
        assertEquals("/report-issue.jsp", request.forwardedPath());
        assertNotNull(request.attribute("reportError"));
        assertTrue(request.attribute("reportError").contains("No reference was created"));
        assertNull(session.attribute("farmerCreatedReference"));
        assertNull(session.attribute("createdComplaintReference"));
    }

    private Map<String, String> validValues() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("farmerName", "Ravi Kumar");
        values.put("mobile", "9876543210");
        values.put("village", "Greenfield");
        values.put("district", "Warangal");
        values.put("crop", "Rice");
        values.put("category", "Crop Disease");
        values.put("description", "Several leaves have unusual spots.");
        values.put("observedDate", LocalDate.now().minusDays(1).toString());
        return values;
    }

    private Complaint createdComplaint(Long userId) {
        return new Complaint(1, "RYT-20261029-1234567890ABCDEF", userId, "Ravi Kumar",
                "9876543210", "Greenfield", "Warangal", "Rice", "Crop Disease",
                "Several leaves have unusual spots.", LocalDate.now().minusDays(1),
                Complaint.Status.PENDING, Instant.now(), Instant.now());
    }
}
