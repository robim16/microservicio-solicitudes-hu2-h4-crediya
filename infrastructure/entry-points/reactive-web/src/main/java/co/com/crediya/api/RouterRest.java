package co.com.crediya.api;

import co.com.crediya.api.config.SolicitudPath;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final SolicitudPath solicitudPath;
    private final Handler solicitudHandler;
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/solicitudes",
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    method = { RequestMethod.POST },
                    beanClass = Handler.class,
                    beanMethod = "listenSaveSolicitud"

            ),
            @RouterOperation(
                    path = "/api/v1/solicitudes",
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    method = { RequestMethod.GET },
                    beanClass = Handler.class,
                    beanMethod = "listenFilterSolicitud"

            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST(solicitudPath.getSolicitudes()), solicitudHandler::listenSaveSolicitud)
                .andRoute(GET(solicitudPath.getSolicitudes()), solicitudHandler::listenFilterSolicitud);
    }
}
