package com.ecommerce.api.service;

import com.ecommerce.api.dto.AuthRequest;
import com.ecommerce.api.dto.AuthResponse;

public interface AuthService {
    AuthResponse login(AuthRequest authRequest);
    AuthResponse register(AuthRequest authRequest);
}
