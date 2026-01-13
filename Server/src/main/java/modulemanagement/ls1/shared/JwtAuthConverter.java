package modulemanagement.ls1.shared;

import modulemanagement.ls1.models.User;
import modulemanagement.ls1.services.AuthenticationService;
import org.springframework.core.convert.converter.Converter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final AuthenticationService authenticationService;

    public JwtAuthConverter(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    @Nullable
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        User user = authenticationService.getAuthenticatedUser(jwt);

        Collection<GrantedAuthority> authorities = new ArrayList<>();

        // Convert UserRole enum to Spring Security role format
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities, jwt.getClaim("preferred_username"));

        // Store the User object in the authentication token's details for later
        // retrieval
        token.setDetails(user);

        return token;
    }

}
