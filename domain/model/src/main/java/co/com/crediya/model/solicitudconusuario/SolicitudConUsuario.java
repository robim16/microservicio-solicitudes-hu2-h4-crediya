package co.com.crediya.model.solicitudconusuario;
import co.com.crediya.model.solicitud.vo.SolicitudConDetalles;
import co.com.crediya.model.usuario.Usuario;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SolicitudConUsuario {
    private SolicitudConDetalles solicitud;
    private Usuario usuario;
}
