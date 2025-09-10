package co.com.crediya.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.BigInteger;

public record SolicitudUsuarioResponseDTO(
        BigInteger id,
        Long monto,
        String plazo,
        String email,
        BigInteger idEstado,
        BigInteger idTipoPrestamo,
        BigDecimal tasaInteres,
        BigDecimal cuotaMensual,
        String nombreUsuario,
        Long salarioBase

) {}
