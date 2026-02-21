package com.ecommerce.api.service;

import com.ecommerce.api.dto.AuthRequest;
import com.ecommerce.api.dto.AuthResponse;
import com.ecommerce.api.entity.User;

public interface AuthService {
    AuthResponse login(AuthRequest authRequest);
    AuthResponse register(AuthRequest authRequest);
    AuthResponse refreshToken(String refreshToken);
    void logout(User user);
}
