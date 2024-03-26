package com.booleanuk.api.security;

import com.booleanuk.api.security.jwt.AuthEntryPointJwt;
import com.booleanuk.api.security.jwt.AuthTokenFilter;
import com.booleanuk.api.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(this.userDetailsService);
        authProvider.setPasswordEncoder(this.passwordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .requiresChannel((requiresChannel) -> requiresChannel.requestMatchers((request) -> request.getHeader("X-Forwarded-Proto") != null)
                        .requiresSecure())
                .csrf((csrf) -> csrf.disable())
                .exceptionHandling((exception) -> exception.authenticationEntryPoint(this.unauthorizedHandler))
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/auth/**").permitAll() // Allows unauthenticated access to authentication endpoints
                        .requestMatchers(HttpMethod.GET, "/posts/**", "/posts/*/reviews/**").permitAll() // Allow read access to anyone
                        .requestMatchers(HttpMethod.POST, "/posts/**", "/posts/*/reviews/**").authenticated() // Authenticated users can create posts and reviews
                        .requestMatchers(HttpMethod.PUT, "/posts/**", "/posts/*/reviews/**").authenticated() // Authenticated users can update their posts and reviews
                        .requestMatchers(HttpMethod.DELETE, "/posts/**", "/posts/*/reviews/**").authenticated() // Authenticated users can delete their posts and reviews
                        .requestMatchers(HttpMethod.GET, "/users/**").permitAll() // Authenticated users can view user details
                        .requestMatchers(HttpMethod.PUT, "/users/**").authenticated() // Authenticated users can update user details
                        .requestMatchers(HttpMethod.DELETE, "/users/**").authenticated() // Authenticated users can delete users
                        .anyRequest().authenticated()
                );


        // Heroku stuff

        http.authenticationProvider(this.authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
