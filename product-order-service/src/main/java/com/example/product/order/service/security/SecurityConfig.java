package com.example.product.order.service.security;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService() {

        UserDetails admin = User.withUsername("inventory_admin").password("password").roles("INVENTORY_ADMIN").build();
        UserDetails sales = User.withUsername("sales").password("password").roles("SALES").build();
        UserDetails viewer = User.withUsername("viewer").password("password").roles("VIEWER").build();

        return new InMemoryUserDetailsManager(admin, sales, viewer);

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable());

        http.authorizeHttpRequests(auth -> auth


                .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("INVENTORY_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("INVENTORY_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("INVENTORY_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/products/**")
                .hasAnyRole("INVENTORY_ADMIN", "SALES", "VIEWER")


                .requestMatchers(HttpMethod.POST, "/api/orders/**").hasRole("SALES")
                .requestMatchers(HttpMethod.PUT, "/api/orders/**").hasRole("SALES")
                .requestMatchers(HttpMethod.DELETE, "/api/orders/**").hasRole("SALES")
                .requestMatchers(HttpMethod.GET, "/api/orders/**")
                .hasAnyRole("INVENTORY_ADMIN", "SALES", "VIEWER").anyRequest().authenticated())
                .httpBasic(httpbasic ->httpbasic.realmName("productService"));



        return http.build();
    }

}