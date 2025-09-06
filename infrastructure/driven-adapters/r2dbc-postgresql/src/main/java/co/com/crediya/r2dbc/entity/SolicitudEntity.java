package co.com.crediya.r2dbc.entity;

import org.springframework.data.annotation.Id;
import lombok.*;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigInteger;

@Table("solicitudes")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SolicitudEntity {

    @Id
    private BigInteger id;
    private Long monto;
    private String plazo;
    private String email;
    private BigInteger idEstado;
    private BigInteger idTipoPrestamo;
}
