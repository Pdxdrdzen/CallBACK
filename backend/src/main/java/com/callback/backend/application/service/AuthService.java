package com.callback.backend.application.service;

import com.callback.backend.application.dto.AuthResponse;
import com.callback.backend.application.dto.LoginRequest;
import com.callback.backend.application.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}