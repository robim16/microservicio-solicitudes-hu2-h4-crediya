package co.com.crediya.api.dto;

import java.util.List;

public record SolicitudListResponseDTO(
        List<SolicitudUsuarioResponseDTO> solicitudes,
        long totalRegistros,
        int paginaActual,
        int tamanoPagina
) {}
