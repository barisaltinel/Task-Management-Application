package io.github.barisaltinel.taskmanagement.config;

import io.github.barisaltinel.taskmanagement.model.User;
import io.github.barisaltinel.taskmanagement.repository.UserRepository;
import io.github.barisaltinel.taskmanagement.security.BearerTokenAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(BootstrapAdminProperties.class)
public class SecurityConfig {

    private final Environment environment;
    private final boolean h2ConsoleEnabled;

    public SecurityConfig(
            Environment environment,
            @Value("${spring.h2.console.enabled:false}") boolean h2ConsoleEnabled
    ) {
        this.environment = environment;
        this.h2ConsoleEnabled = h2ConsoleEnabled;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> {
                    if (isH2ConsoleAllowed()) {
                        headers.frameOptions(frame -> frame.sameOrigin());
                    }
                })
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required\",\"path\":\""
                                            + request.getRequestURI()
                                            + "\",\"loginEndpoint\":\"/api/auth/login\"}"
                            );
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access denied\",\"path\":\""
                                            + request.getRequestURI()
                                            + "\"}"
                            );
                        })
                )
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.GET, "/", "/api", "/api/public/app-info").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui", "/swagger-ui.html", "/swagger-ui/**")
                            .permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll();

                    if (isH2ConsoleAllowed()) {
                        auth.requestMatchers("/h2-console/**").permitAll();
                    } else {
                        auth.requestMatchers("/h2-console/**").denyAll();
                    }

                    auth.requestMatchers(HttpMethod.GET, "/api/projects/**").hasAnyRole("PROJECT_MANAGER", "ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/projects/**").hasAnyRole("PROJECT_MANAGER", "ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/projects/**").hasAnyRole("PROJECT_MANAGER", "ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/projects/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/tasks/**").hasAnyRole("PROJECT_MANAGER", "ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/tasks/**").hasAnyRole("PROJECT_MANAGER", "TEAM_LEADER", "ADMIN");
                    auth.requestMatchers(HttpMethod.PATCH, "/api/tasks/**")
                            .hasAnyRole("TEAM_MEMBER", "TEAM_LEADER", "PROJECT_MANAGER", "ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/attachments/**")
                            .hasAnyRole("TEAM_MEMBER", "TEAM_LEADER", "PROJECT_MANAGER", "ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/attachments/**")
                            .hasAnyRole("TEAM_MEMBER", "TEAM_LEADER", "PROJECT_MANAGER", "ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/users/**").authenticated();
                    auth.requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/users/**").authenticated();
                    auth.requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN");
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(bearerTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private boolean isH2ConsoleAllowed() {
        return h2ConsoleEnabled && !environment.acceptsProfiles(Profiles.of("prod"));
    }

    @Bean
    public CommandLineRunner bootstrapAdminUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            BootstrapAdminProperties bootstrapAdminProperties
    ) {
        return args -> {
            String adminEmail = bootstrapAdminProperties.getEmail();
            String adminPassword = bootstrapAdminProperties.getPassword();

            if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
                return;
            }
            if (userRepository.findByEmailIgnoreCaseAndDeletedFalse(adminEmail).isPresent()) {
                return;
            }

            User admin = new User();
            admin.setName(bootstrapAdminProperties.getName());
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole("ADMIN");
            admin.setDeleted(false);
            userRepository.save(admin);
        };
    }
}
