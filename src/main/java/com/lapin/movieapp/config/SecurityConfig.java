package com.lapin.movieapp.config;

import com.lapin.movieapp.filter.JWTFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity //to make our app run with our security configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService; //to verify username and password
    private final JWTFilter jwtFilter;

    private final String[] WHITELIST;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService, JWTFilter jwtFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtFilter = jwtFilter;

        WHITELIST = new String[]
                {
                        "/api/users/register",
                        "/api/users/login"
                };
    }

    @Bean //we say to Spring: don't go for default, this is the security chain you have to go for
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(CsrfConfigurer::disable) //disable csrf token protection
                .authorizeHttpRequests(request -> request
                        .requestMatchers(WHITELIST).permitAll()
                        .anyRequest().authenticated()) //enforce authentication for all incoming requests
                .formLogin(Customizer.withDefaults()) //enable the spring security's login page
                //.httpBasic(Customizer.withDefaults()) //enable http basic authentication with default settings, I make use of this config in postman
                //.logout(Customizer.withDefaults()) //what are you doing?
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //set session to stateless
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class) //add the JWT filter before the UsernamePasswordAuthenticationFilter
                .build();
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    /*
        The AuthenticationManager returned by this method becomes a managed Spring bean.
        This means any other class that needs an AuthenticationManager can have it injected by Spring.
        The @Autowired annotation in UserService tells Spring to inject the returned AuthenticationManager bean into the service.

        This function is called only ONCE, when Spring starts:
        It finds the @Bean-annotated method authenticationManager() and calls it once to get the AuthenticationManager instance.
        It then injects the pre-existing AuthenticationManager bean to any @Autowired-annotated field.
    */
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); //enable CORS requests from any origin
        configuration.setAllowedMethods(List.of("*")); //for any method
        configuration.setAllowedHeaders(List.of("*")); //for any header
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
