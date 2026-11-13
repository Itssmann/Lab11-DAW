package com.tecsup.exception;
import java.util.Map;
import java.util.LinkedHashMap;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> noEncontrado(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("codigo", 404, "mensaje", ex.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("codigo", 400, "mensaje", "Datos inválidos", "errores", errores));
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, HandlerMethodValidationException.class})
    public ResponseEntity<?> formato(Exception ex) {
        return ResponseEntity.badRequest().body(Map.of("codigo", 400, "mensaje", "JSON, tipo de dato o parámetro inválido"));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> negocio(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("codigo", 400, "mensaje", ex.getMessage()));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> conflicto(DataIntegrityViolationException ex) {
        return ResponseEntity.status(409).body(Map.of("codigo", 409, "mensaje", "La operación entra en conflicto con los datos existentes"));
    }
}
