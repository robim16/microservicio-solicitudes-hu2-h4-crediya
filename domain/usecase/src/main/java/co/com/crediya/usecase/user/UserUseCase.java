package co.com.crediya.usecase.user;

import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserUseCase implements IUserUseCase {
    private final UsuarioRepository usuarioRepository;
    @Override
    public Mono<Usuario> getUsuarioByEmail(String email, String token) {
        return usuarioRepository.getUsuarioByEmail(email, token);
    }
}
