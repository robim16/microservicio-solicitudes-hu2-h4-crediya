package co.com.crediya.api.mapper;

import co.com.crediya.api.dto.CreateSolicitudDTO;
import co.com.crediya.api.dto.SolicitudResponseDTO;
import co.com.crediya.model.solicitud.Solicitud;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SolicitudDTOMapper {
    Solicitud mapToEntity(CreateSolicitudDTO solicitudDTO);
    CreateSolicitudDTO mapToDTO(Solicitud solicitud);
    SolicitudResponseDTO mapToResponseDTO(Solicitud solicitud);
}
