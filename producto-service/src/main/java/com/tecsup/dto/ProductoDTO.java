package com.tecsup.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record ProductoDTO(
    @NotBlank @Size(max = 100) String nombre,
    @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal precio,
    @NotNull @PositiveOrZero Integer stock,
    @NotNull @Positive Long categoriaId
) { }
