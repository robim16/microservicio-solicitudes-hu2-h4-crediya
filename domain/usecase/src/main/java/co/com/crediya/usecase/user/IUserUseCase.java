package co.com.crediya.usecase.user;

import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.usuario.Usuario;
import reactor.core.publisher.Mono;

public interface IUserUseCase {
    Mono<Usuario> getUsuarioByEmail(String email, String token);
}
