package co.com.crediya.model.usuario.security;

public interface TokenService {
    AuthenticatedUser validateToken(String token);

}
