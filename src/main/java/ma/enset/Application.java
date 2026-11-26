package ma.enset;
import java.util.List;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
public class Application {
    public static void main(String[] args) { SpringApplication.run(Application.class, args); }

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        var roles = new JwtAuthenticationConverter();
        roles.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realm = jwt.getClaimAsMap("realm_access");
            if (realm == null || !(realm.get("roles") instanceof List<?> values)) return List.of();
            return values.stream().filter(String.class::isInstance).map(String.class::cast)
                .<GrantedAuthority>map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();
        });
        return http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(r -> r.requestMatchers("/public").permitAll()
                .requestMatchers("/admin").hasRole("gestionnaire")
                .requestMatchers("/me").hasAnyRole("lecteur", "gestionnaire").anyRequest().denyAll())
            .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(roles)))
            .build();
    }

    @GetMapping("/public")
    Map<String, String> info() { return Map.of("service", "Keycloak TP"); }
    @GetMapping("/me")
    Map<String, String> user() { return Map.of("access", "lecture"); }
    @GetMapping("/admin")
    Map<String, String> admin() { return Map.of("access", "gestion"); }
}

