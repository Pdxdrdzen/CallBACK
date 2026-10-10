package com.callback.backend.config;

//import com.callback.backend.infrastructure.web.filter.JwtAuthenticationFilter;
//import com.callback.backend.infrastructure.web.filter.RateLimitingFilter;
import com.callback.backend.infrastructure.web.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;


    //    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//    private final RateLimitingFilter rateLimitingFilter;
//
//    public SecurityConfig(
//            JwtAuthenticationFilter jwtAuthenticationFilter,
//            RateLimitingFilter rateLimitingFilter
//    ) {
//        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
//        this.rateLimitingFilter = rateLimitingFilter;
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        return http
//                .csrf(AbstractHttpConfigurer::disable)
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .headers(headers -> headers
//                        .httpStrictTransportSecurity(Customizer.withDefaults())
//                        .contentTypeOptions(Customizer.withDefaults())
//                        .frameOptions(frame -> frame.deny())
//                )
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/v1/auth/**").permitAll()
//                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
//                .build();
//    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        //TODO: delete on prod
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);



        return http.build();
    }

}