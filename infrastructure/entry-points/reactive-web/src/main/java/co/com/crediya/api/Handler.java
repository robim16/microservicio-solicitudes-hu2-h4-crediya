package co.com.crediya.api;

import co.com.crediya.api.dto.*;
import co.com.crediya.api.mapper.SolicitudDTOMapper;
import co.com.crediya.api.mapper.SolicitudMapper;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.usecase.solicitud.SolicitudUseCase;
import co.com.crediya.usecase.solicitudesaprobadas.SolicitudesAprobadasUseCase;
import co.com.crediya.usecase.solicitudlistado.SolicitudListadoUseCase;
import co.com.crediya.usecase.tipoprestamo.TipoPrestamoUseCase;
import co.com.crediya.usecase.user.UserUseCase;
import co.com.crediya.usecase.validacionautomatica.ValidacionAutomaticaUseCase;
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

import java.math.BigInteger;
import java.util.List;


@Component
@RequiredArgsConstructor
public class Handler {
    private  final SolicitudUseCase solicitudUseCase;

    private final SolicitudDTOMapper solicitudDTOMapper;
    private final SolicitudMapper solicitudMapper;
    private final SolicitudListadoUseCase solicitudListadoUseCase;
    private final ValidacionAutomaticaUseCase validacionAutomaticaUseCase;
    private final TipoPrestamoUseCase tipoPrestamoUseCase;




    @Operation(
            summary = "Crear una nueva solicitud",
            description = "Crea una solicitud a partir de un DTO con información del préstamo",
            security = @SecurityRequirement(name = "BearerAuth"),
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
                .flatMap(dto -> solicitudUseCase.registrarSolicitud(
                        solicitudDTOMapper.mapToEntity(dto), token)
                )
                .flatMap(savedSolicitud ->
                        tipoPrestamoUseCase.getTipoPrestamoById(savedSolicitud.getIdTipoPrestamo())
                                .flatMap(tipoPrestamo -> {
                                    if (Boolean.TRUE.equals(tipoPrestamo.getValidacionAutomatica())) {
                                        return validacionAutomaticaUseCase.buscarPrestamosActivos(savedSolicitud)
                                                .thenReturn(savedSolicitud)
                                                .doOnNext(s -> System.out.println("Solicitud validada automáticamente: " + s.getId()))
                                                .onErrorResume(ex -> Mono.error(new RuntimeException(
                                                        "Error en validación automática para solicitud " + savedSolicitud.getId(), ex
                                                )));
                                    }
                                    return Mono.just(savedSolicitud)
                                            .doOnNext(s -> System.out.println("Solicitud registrada sin validación automática: " + s.getId()));
                                })
                )
                .map(savedSolicitud -> solicitudDTOMapper.mapToResponseDTO(savedSolicitud))
                .flatMap(solicitudResponseDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(solicitudResponseDTO)
                )
                .contextWrite(Context.of("token", token));
    }


    @Operation(
            summary = "Filtrar solicitudes",
            description = "Obtiene un listado paginado de solicitudes filtradas por estado y/o email.",
            security = @SecurityRequirement(name = "BearerAuth"),
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

    @Operation(
            summary = "Editar el estado de las solicitudes",
            description = "Edita el estado de las solicitudes y notifica vía email.",
            security = @SecurityRequirement(name = "BearerAuth"),
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = EstadoSolicitudDTO.class)
                    )
            ),
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Identificador de la solicitud",
                            required = true,
                            example = "123",
                            in = ParameterIn.PATH
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Solicitud actualizada",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SolicitudResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "No autorizado"),
                    @ApiResponse(responseCode = "403", description = "Prohibido")
            }
    )
    public Mono<ServerResponse> listenEditStatus(ServerRequest serverRequest) {
        BigInteger id = BigInteger.valueOf(Integer.parseInt(serverRequest.pathVariable("id")));
        return serverRequest.bodyToMono(EstadoSolicitudDTO.class)
                .flatMap(dto -> solicitudUseCase.editarEstado(id, dto.idEstado()))
                .flatMap(updatedSolicitud -> ServerResponse.ok().bodyValue(updatedSolicitud))
                .onErrorResume(e -> ServerResponse.badRequest().bodyValue(e.getMessage()));
    }
}
