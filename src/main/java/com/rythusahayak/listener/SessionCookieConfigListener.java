package com.rythusahayak.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.SessionCookieConfig;

public class SessionCookieConfigListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        SessionCookieConfig cookieConfig = event.getServletContext().getSessionCookieConfig();
        cookieConfig.setAttribute("SameSite", "Lax");
    }
}
