package com.rythusahayak.servlet;

import com.rythusahayak.model.CropAdvisory;
import com.rythusahayak.service.AdvisoryCatalogException;
import com.rythusahayak.service.XmlAdvisoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/advisories")
public class AdvisoriesServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AdvisoriesServlet.class.getName());
    private static final List<String> CROPS =
            List.of("Paddy", "Cotton", "Chilli", "Tomato", "Maize", "Groundnut");
    private static final List<String> CATEGORIES =
            List.of("pest", "disease", "irrigation", "nutrient", "general");
    private final XmlAdvisoryService advisoryService = new XmlAdvisoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String crop = value(request, "crop");
        String category = value(request, "category");
        String query = value(request, "query");
        request.setAttribute("selectedCrop", crop);
        request.setAttribute("selectedCategory", category);
        request.setAttribute("advisoryQuery", query);
        request.setAttribute("advisoryCrops", CROPS);
        request.setAttribute("advisoryCategories", CATEGORIES);

        try {
            List<CropAdvisory> advisories = advisoryService.findAdvisories(crop, category, query);
            request.setAttribute("advisories", advisories);
            request.setAttribute("advisoryCount", advisories.size());
            request.getRequestDispatcher("/advisory-view.jsp").forward(request, response);
        } catch (AdvisoryCatalogException exception) {
            LOGGER.log(Level.SEVERE, "Unable to load the XML crop advisory catalogue.", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            request.setAttribute("advisories", List.of());
            request.setAttribute("advisoryCount", 0);
            request.setAttribute("advisoryError",
                    "Crop learning notes are temporarily unavailable. Please try again later.");
            request.getRequestDispatcher("/advisory-view.jsp").forward(request, response);
        }
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }
}
