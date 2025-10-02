package com.energy;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplianceServlet extends HttpServlet {

    private static final String[] VALID_APPLIANCES = {
        "lighting", "fridge", "washing_machine", "tv", "computer", "ac"
    };

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"Не авторизован\"}");
            return;
        }

        Integer userId = (Integer) session.getAttribute("userId");
        String name = request.getParameter("name");
        String powerStr = request.getParameter("power_w");
        String hoursStr = request.getParameter("hours_per_day");

        if (name == null || powerStr == null || hoursStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Все поля обязательны\"}");
            return;
        }

        if (!isValidAppliance(name)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Недопустимый прибор\"}");
            return;
        }

        int power;
        double hours;
        try {
            power = Integer.parseInt(powerStr);
            hours = Double.parseDouble(hoursStr);
            if (power <= 0 || hours < 0 || hours > 24) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Некорректные значения мощности или времени\"}");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            String sql = "INSERT INTO appliances (user_id, name, power_w, hours_per_day) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, userId);
            stmt.setString(2, name);
            stmt.setInt(3, power);
            stmt.setDouble(4, hours);
            stmt.executeUpdate();
            out.print("{\"success\": true}");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Ошибка сохранения\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"Не авторизован\"}");
            return;
        }

        Integer userId = (Integer) session.getAttribute("userId");

        List<String> appliances = new ArrayList<>();
        double tariff = 5.5;
        try (Connection conn = Database.getConnection()) {
            // Загружаем тариф
            String tariffSql = "SELECT rate_rub_per_kwh FROM tariffs WHERE user_id = ?";
            PreparedStatement tariffStmt = conn.prepareStatement(tariffSql);
            tariffStmt.setInt(1, userId);
            ResultSet tariffRs = tariffStmt.executeQuery();
            if (tariffRs.next()) {
                tariff = tariffRs.getDouble("rate_rub_per_kwh");
            }

            // Загружаем приборы (включая id!)
            String sql = "SELECT id, name, power_w, hours_per_day FROM appliances WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                // Экранируем кавычки для безопасности JSON
                if (name != null) {
                    name = name.replace("\\", "\\\\").replace("\"", "\\\"");
                }
                int power = rs.getInt("power_w");
                double hours = rs.getDouble("hours_per_day");
                String record = String.format(
                    java.util.Locale.US,
                    "{\"id\":%d,\"name\":\"%s\",\"power_w\":%d,\"hours_per_day\":%.2f}",
                    id, name, power, hours
                );
                appliances.add(record);
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Ошибка загрузки данных\"}");
            e.printStackTrace();
            return;
        }

        String json = String.format(
            java.util.Locale.US,
            "{\"tariff\":%.2f,\"appliances\":[%s]}",
            tariff, String.join(",", appliances)
        );
        out.print(json);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"Не авторизован\"}");
            return;
        }

        Integer userId = (Integer) session.getAttribute("userId");
        String idStr = request.getParameter("id");

        if (idStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"ID не указан\"}");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            try (Connection conn = Database.getConnection()) {
                String sql = "DELETE FROM appliances WHERE id = ? AND user_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, id);
                stmt.setInt(2, userId);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    out.print("{\"success\": true}");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\": \"Прибор не найден или не принадлежит вам\"}");
                }
            }
        } catch (NumberFormatException | SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Ошибка удаления\"}");
            e.printStackTrace();
        }
    }

    private boolean isValidAppliance(String appliance) {
        for (String valid : VALID_APPLIANCES) {
            if (valid.equals(appliance)) return true;
        }
        return false;
    }
}