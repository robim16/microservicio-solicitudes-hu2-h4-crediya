package co.com.crediya.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "routes.paths")
public class SolicitudPath {
    private String solicitudes;
    private String estadoSolicitud;
}
