package org.online.shop.magnit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.online.shop.magnit.exception.ApiError;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import java.io.IOException;
@Component
@RequiredArgsConstructor
public class RestSecurityHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final ObjectMapper mapper;
    @Override public void commence(HttpServletRequest r, HttpServletResponse s, AuthenticationException e) throws IOException {
        write(r, s, 401, "Требуется вход. Токен отсутствует, недействителен или истёк");
    }
    @Override public void handle(HttpServletRequest r, HttpServletResponse s, AccessDeniedException e) throws IOException {
        write(r, s, 403, "Недостаточно прав для выполнения операции");
    }
    private void write(HttpServletRequest r, HttpServletResponse s, int status, String message) throws IOException {
        s.setStatus(status);
        s.setContentType("application/json");
        s.setCharacterEncoding("UTF-8");
        mapper.writeValue(s.getOutputStream(), ApiError.of(status, message, r.getRequestURI()));
    }
}
