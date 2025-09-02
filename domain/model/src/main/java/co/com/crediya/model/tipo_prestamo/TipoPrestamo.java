package co.com.crediya.model.tipo_prestamo;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class TipoPrestamo {
    private BigInteger id;
    private String nombre;
    private Long montoMinimo;
    private Long montoMaximo;
    private Float tasaInteres;
    private Boolean validacionAutomatica;
}
