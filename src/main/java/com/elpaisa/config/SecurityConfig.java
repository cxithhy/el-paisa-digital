package com.elpaisa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * RNF01 (Seguridad): acceso restringido por roles (Administrador, Cajero, Mozo)
 * y contraseñas cifradas con BCrypt.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/img/**", "/login").permitAll()
                .requestMatchers("/insumos/**", "/productos/**").hasRole("ADMIN")
                .requestMatchers("/reportes/**").hasAnyRole("ADMIN", "CAJERO")
                .requestMatchers("/ventas/**").hasAnyRole("ADMIN", "CAJERO", "MOZO")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll())
            .csrf(AbstractHttpConfigurer::disable); // simplificado para el avance; en produccion se habilita con tokens en los forms

        return http.build();
    }
}
