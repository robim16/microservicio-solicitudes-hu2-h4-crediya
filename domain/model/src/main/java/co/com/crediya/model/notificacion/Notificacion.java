package co.com.crediya.model.notificacion;
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
public class Notificacion {
    private String id;
    private String type;
    private Object payload;
    private String destino;
}
