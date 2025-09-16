package co.com.crediya.api.mapper;

import co.com.crediya.api.dto.SolicitudUsuarioResponseDTO;
import co.com.crediya.model.solicitudconusuario.SolicitudConUsuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SolicitudMapper {
    @Mapping(target = "id", source = "solicitud.id")
    @Mapping(target = "monto", source = "solicitud.monto")
    @Mapping(target = "plazo", source = "solicitud.plazo")
    @Mapping(target = "email", source = "solicitud.email")
    @Mapping(target = "idEstado", source = "solicitud.idEstado")
    @Mapping(target = "idTipoPrestamo", source = "solicitud.idTipoPrestamo")
    @Mapping(target = "tasaInteres", source = "solicitud.tasaInteres")
    @Mapping(target = "cuotaMensual", source = "solicitud.cuotaMensual")
    @Mapping(target = "nombreUsuario", source = "usuario.nombre")
    @Mapping(target = "salarioBase", source = "usuario.salarioBase")
    SolicitudUsuarioResponseDTO toDto(SolicitudConUsuario source);
}
