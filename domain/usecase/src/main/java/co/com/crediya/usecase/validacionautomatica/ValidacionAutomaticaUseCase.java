package co.com.crediya.usecase.validacionautomatica;

import co.com.crediya.model.estados.gateways.EstadosRepository;
import co.com.crediya.model.notificacion.Notificacion;
import co.com.crediya.model.notificacion.gateways.NotificacionRepository;
import co.com.crediya.model.prestamos.Prestamos;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.crediya.model.tipo_prestamo.gateways.TipoPrestamoRepository;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;
import co.com.crediya.usecase.solicitud.exceptions.ClientNotFoundException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;


import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ValidacionAutomaticaUseCase  implements IValidacionAutomaticaUseCase{

    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;
    private final NotificacionRepository notificacionRepository;

    @Override
    public Mono<Void> buscarPrestamosActivos(Solicitud solicitudNueva) {
        return solicitudRepository.prestamosActivos(solicitudNueva.getEmail())
                .flatMap(prestamo ->
                        usuarioRepository.getUsuarioByEmail(prestamo.getEmail())
                                .switchIfEmpty(Mono.error(new ClientNotFoundException("Cliente no encontrado")))
                                .map(usuario -> {
                                    prestamo.setSalarioBase(usuario.getSalarioBase());
                                    return prestamo;
                                })
                )
                .collectList()
                .flatMap(prestamos -> {
                    String prestamosJson = prestamos.stream()
                            .map(Prestamos::toJson)
                            .collect(Collectors.joining(",", "[", "]"));

                    return tipoPrestamoRepository.getTipoPrestamoById(solicitudNueva.getIdTipoPrestamo())
                            .flatMap(tipoPrestamo -> {

                                String solicitudJson = solicitudNueva.toJson();

                                String nuevaSolicitudJson = solicitudJson.substring(0, solicitudJson.length() - 1)
                                        + ", \"tasaInteres\": " + tipoPrestamo.getTasaInteres()
                                        + "}";

                                String payload = String.format("{\"prestamos\": %s, \"solicitud\": %s}", prestamosJson, nuevaSolicitudJson);

                                Notificacion notification = Notificacion.builder()
                                        .type("PRESTAMOS_ACTIVOS")
                                        .payload(payload)
                                        .destino("SQS")
                                        .build();

                                return notificacionRepository.enviar(notification, "capacidad-endeudamiento")
                                        .doOnNext(n -> {
                                            System.out.println("Array de prestamos enviado a cola: total=" + prestamos.size());
                                            System.out.println("Payload: " + payload);
                                        })
                                        .then();
                            });
                })

                .onErrorResume(e -> {
                    System.out.println("Error en prestamosActivos: " + e.getMessage());
                    return Mono.empty();
                });
    }
}
