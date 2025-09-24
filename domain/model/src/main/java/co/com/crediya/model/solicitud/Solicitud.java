package co.com.crediya.model.solicitud;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Solicitud {
    private BigInteger id;
    private Long monto;
    private String plazo;
    private String email;
    private BigInteger idEstado;
    private BigInteger idTipoPrestamo;

    public String toJson() {
        return String.format(
                "{" +
                        "\"id\":%s," +
                        "\"monto\":%d," +
                        "\"plazo\":\"%s\"," +
                        "\"email\":\"%s\"," +
                        "\"idEstado\":%s," +
                        "\"idTipoPrestamo\":%s" +
                        "}",
                id,
                monto,
                plazo,
                email,
                idEstado,
                idTipoPrestamo
        );
    }

}
