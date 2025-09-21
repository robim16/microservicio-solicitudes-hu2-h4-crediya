package co.com.crediya.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigInteger;

@Table("estados")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EstadosEntity {

    @Id
    private BigInteger id;
    private String nombre;
    private String descripcion;

}
