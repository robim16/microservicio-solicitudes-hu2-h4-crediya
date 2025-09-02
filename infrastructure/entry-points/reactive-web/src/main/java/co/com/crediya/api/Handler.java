package co.com.crediya.api;

import co.com.crediya.api.dto.CreateSolicitudDTO;
import co.com.crediya.api.dto.SolicitudResponseDTO;
import co.com.crediya.api.mapper.SolicitudDTOMapper;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.usecase.solicitud.SolicitudUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {
    private  final SolicitudUseCase solicitudUseCase;
    private final SolicitudDTOMapper solicitudDTOMapper;

    @Operation(
            summary = "Crear un nuevo solicitud",
            description = "Crea una solicitud a partir de un DTO con información del préstamo",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateSolicitudDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Solicitud creada correctamente",
                            content = @Content(schema = @Schema(implementation = SolicitudResponseDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Datos inválidos"
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "El email no existe"
                    )
            }
    )
    public Mono<ServerResponse> listenSaveSolicitud(ServerRequest request) {
        return request.bodyToMono(CreateSolicitudDTO.class)
                .flatMap(dto -> solicitudUseCase.registrarSolicitud(solicitudDTOMapper.mapToEntity(dto)))
                .flatMap(savedSolicitud -> {
                    SolicitudResponseDTO solicitudResponseDTO = solicitudDTOMapper.mapToResponseDTO(savedSolicitud);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(solicitudResponseDTO);

                });
    }

}
