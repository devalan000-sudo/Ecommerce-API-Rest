package com.ecommerce.api.service.impl;

import com.ecommerce.api.dto.AuthRequest;
import com.ecommerce.api.dto.AuthResponse;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.entity.enums.Role;
import com.ecommerce.api.repository.UserRepository;
import com.ecommerce.api.security.JwtService;
import com.ecommerce.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse login(AuthRequest authRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authRequest.getUsername(), authRequest.getPassword()
        ));

        var user = userRepository.findByUsername(authRequest.getUsername())
                .orElseThrow();
        var jwtToke = jwtService.generateToken(user);
        
        log.info("Login exitoso para usuario: {}", authRequest.getUsername());

        AuthResponse  authResponse = new AuthResponse();
        authResponse.setToken(jwtToke);
        return authResponse;
    }
    
    @Override
    public AuthResponse register(AuthRequest authRequest) {
            var user = User.builder()
                    .username(authRequest.getUsername())
                    .password(passwordEncoder.encode(authRequest.getPassword()))
                    .role(Role.CLIENT)
                    .build();
            userRepository.save(user);
            
            log.info("Nuevo usuario registrado: {}", authRequest.getUsername());

            var jwtToken = jwtService.generateToken(user);
            AuthResponse authResponse = new AuthResponse();
            authResponse.setToken(jwtToken);
            return authResponse;
        }
    }

