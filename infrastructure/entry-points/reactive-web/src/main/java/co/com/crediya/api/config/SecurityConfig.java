package co.com.crediya.api.config;

import co.com.crediya.api.jwt.JwtAuthenticationManager;
import co.com.crediya.api.jwt.SecurityContextRepository;
import co.com.crediya.model.usuario.security.TokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         TokenService tokenService) {

        JwtAuthenticationManager authManager = new JwtAuthenticationManager(tokenService);
        SecurityContextRepository contextRepository = new SecurityContextRepository(authManager);

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth
                        .pathMatchers(HttpMethod.GET, "/api/v1/solicitudes/**").hasAnyRole("ADMIN", "ASESOR")
                        .pathMatchers(HttpMethod.POST, "/api/v1/solicitudes").hasRole("CLIENTE")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/solicitudes/**").hasAnyRole("ADMIN", "ASESOR")
                        .pathMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .authenticationManager(authManager)
                .securityContextRepository(contextRepository)
                .build();
    }
}
