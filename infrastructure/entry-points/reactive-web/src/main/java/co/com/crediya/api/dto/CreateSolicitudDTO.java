package co.com.crediya.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigInteger;

@Schema(name = "CreateUserDTO", description = "Datos de entrada para crear una solicitud")
public record CreateSolicitudDTO(
        @Schema(description = "Documento de identidad", example = "123456789") Long monto,
        @Schema(description = "Nombre del usuario", example = "Carlos") String plazo,
        @Schema(description = "Correo electrónico", example = "carlos@email.com") String email,

        //@Schema(description = "Apellidos del usuario", example = "Arteaga") BigInteger idEstado,
        @Schema(description = "Apellidos del usuario", example = "Arteaga") BigInteger idTipoPrestamo
) {

}
