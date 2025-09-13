package co.com.crediya.usecase.solicitudlistado;

import co.com.crediya.model.solicitudconusuario.SolicitudConUsuario;
import co.com.crediya.usecase.solicitud.SolicitudUseCase;
import co.com.crediya.usecase.user.UserUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;

import java.util.List;

@RequiredArgsConstructor
public class SolicitudListadoUseCase {

    private final SolicitudUseCase solicitudUseCase;
    private final UserUseCase userUseCase;


    public Mono<Tuple2<List<SolicitudConUsuario>, Long>> listarSolicitudes(
            String estado,
            String email,
            String plazo,
            String tipoPrestamo,
            int page,
            int size
    ) {
        Mono<List<SolicitudConUsuario>> solicitudesConUsuario =
                solicitudUseCase.filtrarSolicitud(estado, email, plazo, tipoPrestamo, page, size)
                        .flatMap(solicitud ->
                                userUseCase.getUsuarioByEmail(solicitud.getEmail())
                                        .map(usuario -> new SolicitudConUsuario(solicitud, usuario))
                        )
                        .collectList();

        Mono<Long> totalSolicitudes = solicitudUseCase.contarSolicitudes(estado, email, plazo, tipoPrestamo);

        return Mono.zip(solicitudesConUsuario, totalSolicitudes);
    }
}
