package co.com.crediya.r2dbc.entity;

import org.springframework.data.annotation.Id;
import lombok.*;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigInteger;

@Table("tipo_prestamos")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TipoPrestamoEntity {

    @Id
    private BigInteger id;

    private String nombre;
    private Long montoMinimo;
    private Long montoMaximo;
    private Float tasaInteres;
    private Boolean validacionAutomatica;
}
