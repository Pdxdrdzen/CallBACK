package com.callback.backend.application.service.impl;

import com.callback.backend.application.dto.AuthResponse;
import com.callback.backend.application.dto.LoginRequest;
import com.callback.backend.application.dto.RegisterRequest;
import com.callback.backend.application.service.AuthService;
import com.callback.backend.infrastructure.persistence.entity.UserEntity;
import com.callback.backend.infrastructure.persistence.repository.UserJpaRepository;

import com.callback.backend.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userJpaRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Użytkownik o podanym adresie email już istnieje.");
        }


        UserEntity user = UserEntity.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role("STANDARD")
                .build();


        userJpaRepository.save(user);


        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        UserEntity user = userJpaRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Nieprawidłowy adres email lub hasło."));


        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Nieprawidłowy adres email lub hasło.");
        }


        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }
}