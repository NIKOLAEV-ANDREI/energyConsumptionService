package com.energy;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TariffServlet extends HttpServlet {

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
        String tariffStr = request.getParameter("rate");

        if (tariffStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Тариф не указан\"}");
            return;
        }

        double tariff;
        try {
            tariff = Double.parseDouble(tariffStr);
            if (tariff <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Некорректный тариф\"}");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            // Вставка или обновление
            String sql = "INSERT INTO tariffs (user_id, rate_rub_per_kwh) VALUES (?, ?) " +
                         "ON DUPLICATE KEY UPDATE rate_rub_per_kwh = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setDouble(2, tariff);
            stmt.setDouble(3, tariff);
            stmt.executeUpdate();
            out.print("{\"success\": true}");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Ошибка сохранения тарифа\"}");
            e.printStackTrace();
        }
    }
}