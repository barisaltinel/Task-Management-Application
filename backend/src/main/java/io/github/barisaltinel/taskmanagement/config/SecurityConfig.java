package io.github.barisaltinel.taskmanagement.config;

import io.github.barisaltinel.taskmanagement.model.User;
import io.github.barisaltinel.taskmanagement.security.BearerTokenAuthenticationFilter;
import io.github.barisaltinel.taskmanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
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
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/", "/api", "/api/public/app-info").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/projects/**").hasAnyRole("PROJECT_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/projects/**").hasAnyRole("PROJECT_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/projects/**").hasAnyRole("PROJECT_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/projects/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/tasks/**").hasAnyRole("PROJECT_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/tasks/**").hasAnyRole("PROJECT_MANAGER", "TEAM_LEADER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/tasks/**").hasAnyRole("TEAM_MEMBER", "TEAM_LEADER", "PROJECT_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/attachments/**").hasAnyRole("TEAM_MEMBER", "TEAM_LEADER", "PROJECT_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/attachments/**").hasAnyRole("TEAM_MEMBER", "TEAM_LEADER", "PROJECT_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(bearerTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
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


