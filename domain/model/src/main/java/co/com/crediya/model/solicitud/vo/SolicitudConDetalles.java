package co.com.crediya.model.solicitud.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudConDetalles {

    private BigInteger id;
    private Long monto;
    private String plazo;
    private String email;
    private BigInteger idEstado;
    private BigInteger idTipoPrestamo;

    private BigDecimal tasaInteres;
    private BigDecimal cuotaMensual;

}
