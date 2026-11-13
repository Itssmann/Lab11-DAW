package com.tecsup.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
public record PedidoDTO(
    @NotNull LocalDate fecha,
    @NotNull @Positive Long idCliente,
    @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal total,
    @NotEmpty @Size(max = 100) List<@NotNull @Valid DetallePedidoDTO> detalles
) { }
