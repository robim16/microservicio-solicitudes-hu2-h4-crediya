package co.com.crediya.jwttokenserviceadapter;

import co.com.crediya.model.usuario.security.AuthenticatedUser;
import co.com.crediya.model.usuario.security.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.List;

@Component
public class JwtTokenServiceAdapter implements TokenService {
    private final PublicKey publicKey;

    public JwtTokenServiceAdapter(JwtProperties properties) {
        try {
            this.publicKey = KeyLoader.loadPublicKey(properties.getKeys().getPublicKey());
        } catch (Exception e) {
            throw new RuntimeException("Error loading JWT public key", e);
        }
    }

    @Override
    public AuthenticatedUser validateToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String email = claims.getSubject();
        String role = claims.get("role", String.class);

        return new AuthenticatedUser(email, role);
    }

}

