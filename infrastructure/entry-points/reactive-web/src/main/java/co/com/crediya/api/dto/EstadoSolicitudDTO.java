package co.com.crediya.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigInteger;

@Schema(name = "EstadoSolicitudDTO", description = "Datos de entrada para editar estado de una solicitud")
public record EstadoSolicitudDTO(
        @Schema(description = "Nuevo estado", example = "2") BigInteger idEstado) {
}
