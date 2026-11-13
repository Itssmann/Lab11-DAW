package com.tecsup.dto;
import java.math.BigDecimal;
import jakarta.validation.constraints.*;
public record DetallePedidoDTO(
    @NotNull @Positive Long idProducto,
    @NotNull @Positive @Max(1000000) Integer cantidad,
    @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal precio
) { }
