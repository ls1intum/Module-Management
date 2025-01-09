package modulemanagement.ls1.shared;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthConverter jwtAuthConverter;
    private final OAuth2ResourceServerProperties properties;

    public SecurityConfig(JwtAuthConverter jwtAuthConverter, OAuth2ResourceServerProperties properties) {
        this.jwtAuthConverter = jwtAuthConverter;
        this.properties = properties;
    }

    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(properties.getJwt().getJwkSetUri()).build();
        decoder.setJwtValidator(tokenValidator());
        return decoder;
    }

    @Bean
    public OAuth2TokenValidator<Jwt> tokenValidator() {
        return new DelegatingOAuth2TokenValidator<>(
            new JwtTimestampValidator(),
            token -> {
                String issuer = token.getIssuer().toString();
                if (issuer.equals("http://localhost:8081/realms/module-management") || 
                    issuer.equals("http://keycloak:8080/realms/module-management") ||
		    issuer.equals("http://module-management.ase.cit.tum.de/auth/realms/module-management")) {
                    return OAuth2TokenValidatorResult.success();
                }
                return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Invalid issuer: " + issuer, null));
            }
        );
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/v3/api-docs.yaml", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/**").hasAnyRole("proposal-reviewer", "proposal-submitter")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
                    jwt.jwtAuthenticationConverter(jwtAuthConverter);
                    jwt.decoder(jwtDecoder());
                }));
        return http.build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost", "http://localhost:80", "http://localhost:4200", "https://module-management.ase.cit.tum.de")
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .exposedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
