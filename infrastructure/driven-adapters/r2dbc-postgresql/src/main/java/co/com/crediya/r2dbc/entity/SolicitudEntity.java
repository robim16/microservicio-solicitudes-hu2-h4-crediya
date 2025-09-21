package co.com.crediya.r2dbc.entity;

import org.springframework.data.annotation.Id;
import lombok.*;
import org.springframework.data.relational.core.mapping.Column;
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
    @Column("monto")
    private Long monto;

    @Column("plazo")
    private String plazo;

    @Column("email")
    private String email;

    @Column("id_estado")
    private BigInteger idEstado;

    @Column("id_tipo_prestamo")
    private BigInteger idTipoPrestamo;

    //private String tasaInteres;
}
