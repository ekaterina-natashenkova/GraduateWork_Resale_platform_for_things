package ru.skypro.homework.filter;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class BasicAuthCorsFilter extends OncePerRequestFilter {

    @Override
    @Order(Ordered.HIGHEST_PRECEDENCE)
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ВРЕМЕННОЕ РЕШЕНИЕ - разрешаем ВСЕ origins
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers",
                "Authorization, Content-Type, Accept, X-Requested-With, remember-me, Origin, X-Auth-Token");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Expose-Headers", "Authorization");

        // Для preflight запросов (OPTIONS) сразу отвечаем OK
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return; // НЕ продолжаем цепочку фильтров для OPTIONS
        }

        filterChain.doFilter(request, response);
    }
}
