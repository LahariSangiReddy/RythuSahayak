package com.rythusahayak.servlet;

import com.rythusahayak.util.ComplaintReference;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/complaints/created")
public class ComplaintCreatedServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        Object reference = session == null ? null : session.getAttribute("createdComplaintReference");
        if (!(reference instanceof String value) || !ComplaintReference.isValid(value)) {
            response.sendRedirect(request.getContextPath() + "/report-issue.jsp");
            return;
        }
        session.removeAttribute("createdComplaintReference");
        request.setAttribute("createdReference", value);
        request.getRequestDispatcher("/report-issue.jsp").forward(request, response);
    }
}
