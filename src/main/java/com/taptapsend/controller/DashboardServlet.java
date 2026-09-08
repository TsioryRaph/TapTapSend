package com.taptapsend.controller;

import com.google.gson.Gson;
import com.taptapsend.model.Envoyer;
import com.taptapsend.service.ClientService;
import com.taptapsend.service.EnvoyerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet("/dashboard")
public class DashboardServlet extends BaseServlet {
    private final EnvoyerService envoyerService = new EnvoyerService();
    private final ClientService clientService = new ClientService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        double totalFrais = envoyerService.getRecetteTotaleOperateur();
        List<?> clients = clientService.getAllClients();
        List<Envoyer> envois = envoyerService.getAllEnvois();

        request.setAttribute("totalFrais", totalFrais);
        request.setAttribute("totalClients", clients.size());
        request.setAttribute("totalEnvois", envois.size());

        int nb = Math.min(5, envois.size());
        request.setAttribute("dernierEnvois", envois.subList(Math.max(0, envois.size() - nb), envois.size()));

        request.setAttribute("chartDataJson", buildLast7DaysChartJson(envois));

        forwardToJsp("Dashboard/dashboard", request, response);
    }

    private String buildLast7DaysChartJson(List<Envoyer> envois) {
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat labelFormat = new SimpleDateFormat("dd/MM");

        LinkedHashMap<String, Integer> countByDay = new LinkedHashMap<>();
        LinkedHashMap<String, String> labelByDay = new LinkedHashMap<>();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -6);
        for (int i = 0; i < 7; i++) {
            String key = keyFormat.format(cal.getTime());
            countByDay.put(key, 0);
            labelByDay.put(key, labelFormat.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        for (Envoyer envoi : envois) {
            if (envoi.getDisplayDate() == null) continue;
            String key = keyFormat.format(envoi.getDisplayDate());
            if (countByDay.containsKey(key)) {
                countByDay.put(key, countByDay.get(key) + 1);
            }
        }

        List<String> labels = new ArrayList<>(labelByDay.values());
        List<Integer> counts = new ArrayList<>(countByDay.values());

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", labels);
        chartData.put("counts", counts);

        return new Gson().toJson(chartData);
    }
}