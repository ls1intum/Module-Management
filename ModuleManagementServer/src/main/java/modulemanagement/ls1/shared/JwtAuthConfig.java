package modulemanagement.ls1.shared;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "module-management.keycloak")
public class JwtAuthConfig {
    @NotBlank
    private String clientId;
}
