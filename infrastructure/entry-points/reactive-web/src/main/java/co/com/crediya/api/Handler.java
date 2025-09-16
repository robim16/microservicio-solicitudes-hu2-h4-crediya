package co.com.crediya.api;

import co.com.crediya.api.dto.CreateSolicitudDTO;
import co.com.crediya.api.dto.SolicitudListResponseDTO;
import co.com.crediya.api.dto.SolicitudResponseDTO;
import co.com.crediya.api.dto.SolicitudUsuarioResponseDTO;
import co.com.crediya.api.mapper.SolicitudDTOMapper;
import co.com.crediya.api.mapper.SolicitudMapper;
import co.com.crediya.usecase.solicitud.SolicitudUseCase;
import co.com.crediya.usecase.solicitudlistado.SolicitudListadoUseCase;
import co.com.crediya.usecase.user.UserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.List;


@Component
@RequiredArgsConstructor
public class Handler {
    private  final SolicitudUseCase solicitudUseCase;
    //private  final UserUseCase userUseCase;
    private final SolicitudDTOMapper solicitudDTOMapper;
    private final SolicitudMapper solicitudMapper;
    private final SolicitudListadoUseCase solicitudListadoUseCase;

    @Operation(
            summary = "Crear una nueva solicitud",
            description = "Crea una solicitud a partir de un DTO con información del préstamo",
            security = @SecurityRequirement(name = "bearerAuth"),
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
        String token = request.headers()
                .firstHeader("Authorization")
                .replace("Bearer ", "");

        return request.bodyToMono(CreateSolicitudDTO.class)
                .flatMap(dto -> solicitudUseCase.registrarSolicitud(solicitudDTOMapper.mapToEntity(dto),token))
                .flatMap(savedSolicitud -> {
                    SolicitudResponseDTO solicitudResponseDTO = solicitudDTOMapper.mapToResponseDTO(savedSolicitud);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(solicitudResponseDTO);

                });
    }

    @Operation(
            summary = "Filtrar solicitudes",
            description = "Obtiene un listado paginado de solicitudes filtradas por estado y/o email.",
            security = @SecurityRequirement(name = "bearerAuth"),
            parameters = {
                    @Parameter(
                            in = ParameterIn.QUERY,
                            name = "estado",
                            description = "Estado de la solicitud",
                            schema = @Schema(type = "string")
                    ),
                    @Parameter(
                            in = ParameterIn.QUERY,
                            name = "email",
                            description = "Email del cliente asociado",
                            schema = @Schema(type = "string")
                    ),
                    @Parameter(
                            in = ParameterIn.QUERY,
                            name = "page",
                            description = "Número de página (empieza en 0)",
                            schema = @Schema(type = "integer", defaultValue = "0")
                    ),
                    @Parameter(
                            in = ParameterIn.QUERY,
                            name = "size",
                            description = "Cantidad de registros por página",
                            schema = @Schema(type = "integer", defaultValue = "20")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Listado de solicitudes encontrado",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SolicitudListResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "No autorizado"),
                    @ApiResponse(responseCode = "403", description = "Prohibido")
            }
    )
    public Mono<ServerResponse> listenFilterSolicitud(ServerRequest request) {
        String estado = request.queryParam("estado").orElse(null);
        String email = request.queryParam("email").orElse(null);
        String plazo = request.queryParam("plazo").orElse(null);
        String tipoPrestamo = request.queryParam("tipoPrestamo").orElse(null);
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("20"));

        String token = request.headers()
                .firstHeader("Authorization")
                .replace("Bearer ", "");

        return solicitudListadoUseCase.listarSolicitudes(estado, email, plazo, tipoPrestamo, page, size)
                .flatMap(tuple -> {
                    List<SolicitudUsuarioResponseDTO> solicitudes =
                            tuple.getT1().stream()
                                    .map(solicitudMapper::toDto)
                                    .toList();

                    Long total = tuple.getT2();

                    SolicitudListResponseDTO responseDTO =
                            new SolicitudListResponseDTO(solicitudes, total, page, size);

                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(responseDTO);
                })
                .contextWrite(Context.of("token", token));
    }


}
