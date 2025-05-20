package net.coursework.ems_backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity
//@Profile("prod")
//public class SecurityConfig {
//
//    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//
//    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
//        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(authz -> authz
//                        .requestMatchers("/", "/swagger-ui/**", "/v3/api-docs/**", "/login/**").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/api/employees/**").hasAnyRole("USER", "ADMIN")
//                        .requestMatchers(HttpMethod.POST, "/api/employees/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.PUT, "/api/employees/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasRole("ADMIN")
//                        .anyRequest().authenticated()
//                )
//                .csrf().disable()
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//}

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("prod")
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/swagger-ui/**", "/v3/api-docs/**", "/login/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/employees/**").hasAnyRole("USER", "ADMIN") // Для GET доступ мають USER і ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/employees/**").hasRole("ADMIN") // Для POST доступ має ADMIN
                        .requestMatchers(HttpMethod.PUT, "/api/employees/**").hasRole("ADMIN") // Для PUT доступ має ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasRole("ADMIN") // Для DELETE доступ має ADMIN
                        .anyRequest().authenticated() // Для всіх інших запитів потрібна аутентифікація
                )
                .csrf().disable()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
