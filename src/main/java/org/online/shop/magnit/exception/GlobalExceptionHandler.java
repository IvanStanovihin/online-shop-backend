package org.online.shop.magnit.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import java.time.Instant;
import java.util.LinkedHashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> api(ApiException e, HttpServletRequest r) {
        return error(e.getStatus().value(), e.getMessage(), r);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException e, HttpServletRequest r) {
        var fields = new LinkedHashMap<String, String>();
        e.getBindingResult().getFieldErrors().forEach(f -> fields.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ApiError(Instant.now(), 400, "Проверьте введённые данные", r.getRequestURI(), fields));
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
                       ConstraintViolationException.class, HandlerMethodValidationException.class,
                       org.springframework.web.bind.MissingServletRequestParameterException.class})
    ResponseEntity<ApiError> badRequest(Exception e, HttpServletRequest r) {
        return error(400, "Некорректный формат или значение параметров запроса", r);
    }
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> unauthorized(Exception e, HttpServletRequest r) {
        return error(401, "Неверные учётные данные или требуется вход", r);
    }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> forbidden(Exception e, HttpServletRequest r) {
        return error(403, "Недостаточно прав для выполнения операции", r);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> integrity(Exception e, HttpServletRequest r) {
        return error(409, "Запись уже существует или используется другими данными", r);
    }
    @ExceptionHandler(ConcurrencyFailureException.class)
    ResponseEntity<ApiError> concurrent(Exception e, HttpServletRequest r) {
        return error(409, "Данные изменились одновременно с запросом. Повторите операцию", r);
    }
    private ResponseEntity<ApiError> error(int status, String message, HttpServletRequest r) {
        return ResponseEntity.status(status).body(ApiError.of(status, message, r.getRequestURI()));
    }
}
