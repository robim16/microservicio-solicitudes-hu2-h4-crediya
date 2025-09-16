package co.com.crediya.usecase.user;

import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class UserUseCaseTest {

    private UsuarioRepository usuarioRepository;
    private UserUseCase userUseCase;

    @BeforeEach
    void setUp() {
        usuarioRepository = Mockito.mock(UsuarioRepository.class);
        userUseCase = new UserUseCase(usuarioRepository);
    }

    @Test
    void getUsuarioByEmail_deberiaRetornarUsuarioCuandoExiste() {
        String email = "test@correo.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setNombre("Carlos");
        usuario.setSalarioBase((long) 5000.0);

        when(usuarioRepository.getUsuarioByEmail(email))
                .thenReturn(Mono.just(usuario));

        Mono<Usuario> result = userUseCase.getUsuarioByEmail(email);

        StepVerifier.create(result)
                .expectNextMatches(u ->
                        u.getEmail().equals(email) &&
                                u.getNombre().equals("Carlos") &&
                                u.getSalarioBase() == 5000.0)
                .verifyComplete();

        verify(usuarioRepository, times(1)).getUsuarioByEmail(email);
    }

    @Test
    void getUsuarioByEmail_deberiaRetornarVacioCuandoNoExiste() {

        String email = "notfound@correo.com";
        when(usuarioRepository.getUsuarioByEmail(email))
                .thenReturn(Mono.empty());

        Mono<Usuario> result = userUseCase.getUsuarioByEmail(email);

        StepVerifier.create(result)
                .verifyComplete();

        verify(usuarioRepository, times(1)).getUsuarioByEmail(email);
    }
}
