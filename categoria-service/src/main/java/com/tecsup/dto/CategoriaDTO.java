package com.tecsup.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record CategoriaDTO(
    @NotBlank @Size(max = 100) String nombre,
    @NotBlank @Pattern(regexp = "ACTIVO|INACTIVO") String estado
) { }
