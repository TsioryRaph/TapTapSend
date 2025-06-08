package com.taptapsend.controller;

import com.taptapsend.service.EnvoyerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends BaseServlet {
    private final EnvoyerService envoyerService = new EnvoyerService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        double totalFrais = envoyerService.getRecetteTotaleOperateur();
        request.setAttribute("totalFrais", totalFrais);

        // CORRECTION: Utiliser le bon chemin et la bonne méthode
        // Si vous utilisez un template système, utilisez forwardToJsp avec le chemin correct
        forwardToJsp("Dashboard/dashboard", request, response);
    }
}