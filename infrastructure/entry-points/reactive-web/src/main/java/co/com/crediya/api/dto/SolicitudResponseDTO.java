package co.com.crediya.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigInteger;

@Schema(name = "CreateUserDTO", description = "Respuesta con la información de la solicitud")
public record SolicitudResponseDTO(
        @Schema(description = "Monto del prestamo", example = "14000000") Long monto,
        @Schema(description = "Plazo del prestamo", example = "3 años") String plazo,
        @Schema(description = "Correo electrónico", example = "carlos@email.com") String email,
        @Schema(description = "Tipo de prestamo", example = "2") BigInteger idTipoPrestamo,
        @Schema(description = "Tasa interes", example = "2") String tasaInteres
) {
}
