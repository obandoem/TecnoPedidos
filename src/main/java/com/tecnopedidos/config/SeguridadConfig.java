package com.tecnopedidos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Autenticacion y autorizacion por roles (HU-01)
@Configuration
public class SeguridadConfig {

    // Las contrasenas se guardan con BCrypt: {bcrypt}$2a$... (HU-11 esc. 2)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                // Publicas (incluye la consulta del pedido con codigo, HU-13)
                .requestMatchers("/login", "/error", "/acceso-denegado", "/seguimiento-publico").permitAll()

                // Tecnico: solo su bandeja y sus avances (HU-09, HU-25)
                .requestMatchers("/tecnico/**").hasRole("TECNICO")

                // Administrador: catalogo, categorias y movimientos (HU-04, HU-05, HU-21, HU-23)
                .requestMatchers("/categorias/**", "/inventario/movimientos/**").hasRole("ADMIN")
                .requestMatchers("/productos/nuevo", "/productos/guardar", "/productos/editar/**",
                                 "/productos/desactivar/**", "/productos/activar/**").hasRole("ADMIN")

                // Administrador: asignar tecnico y cancelar pedidos (HU-08, HU-24)
                .requestMatchers(HttpMethod.POST, "/pedidos/*/asignar", "/pedidos/*/cancelar").hasRole("ADMIN")

                // Administrador y vendedor: consulta de inventario, clientes y pedidos
                .requestMatchers("/productos/**", "/inventario/**", "/clientes/**", "/pedidos/**")
                    .hasAnyRole("ADMIN", "VENDEDOR")

                .anyRequest().authenticated())

            // Formulario de inicio de sesion propio (P01)
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("correo")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll())

            // Cierre de sesion (HU-01 esc. 3)
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll())

            // Acceso a una funcion no permitida para el rol
            .exceptionHandling(ex -> ex.accessDeniedPage("/acceso-denegado"));

        return http.build();
    }
}