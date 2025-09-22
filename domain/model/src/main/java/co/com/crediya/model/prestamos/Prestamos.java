package co.com.crediya.model.prestamos;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Prestamos {
    private BigInteger id;
    private Long monto;
    private String plazo;
    private String email;
    private BigInteger idEstado;
    private BigInteger idTipoPrestamo;
    private BigDecimal tasaInteres;
    private Long salarioBase;
}
