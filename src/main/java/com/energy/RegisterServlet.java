package com.energy;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// Мы не используем @WebServlet, т.к. конфигурируем через web.xml
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Простая валидация
        if (username == null || username.trim().isEmpty() ||
            password == null || password.length() < 6) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Логин не пустой, пароль от 6 символов\"}");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username.trim());
            stmt.setString(2, PasswordUtil.hashPassword(password));

            stmt.executeUpdate();
            out.print("{\"success\": true}");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print("{\"error\": \"Пользователь уже существует\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Ошибка сервера\"}");
                e.printStackTrace();
            }
        }
    }
}