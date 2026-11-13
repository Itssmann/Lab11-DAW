package com.tecsup.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record ClienteDTO(
    @NotBlank @Size(max = 100) String nombre,
    @NotBlank @Size(max = 100) String apellidos,
    @NotBlank @Size(max = 200) String direccion,
    @NotBlank @Pattern(regexp = "[0-9]{9,15}") String telefono,
    @NotBlank @Pattern(regexp = "[0-9]{8}") String nroDni,
    @NotBlank @Pattern(regexp = "ACTIVO|INACTIVO") String estado
) { }
