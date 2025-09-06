package co.com.crediya.api.jwt;

import co.com.crediya.model.usuario.security.AuthenticatedUser;
import co.com.crediya.model.usuario.security.TokenService;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;

import java.util.List;


public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final TokenService tokenService;

    public JwtAuthenticationManager(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();

        try {
            AuthenticatedUser user = tokenService.validateToken(authToken);

            return Mono.just(
                    new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
                    )
            );
        } catch (Exception e) {
            return Mono.empty();
        }
    }
}
