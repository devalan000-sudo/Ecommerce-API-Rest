package com.ecommerce.api.service.impl;

import com.ecommerce.api.dto.AuthRequest;
import com.ecommerce.api.dto.AuthResponse;
import com.ecommerce.api.entity.RefreshToken;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.entity.enums.Role;
import com.ecommerce.api.exception.BusinessException;
import com.ecommerce.api.repository.RefreshTokenRepository;
import com.ecommerce.api.repository.UserRepository;
import com.ecommerce.api.security.JwtService;
import com.ecommerce.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Override
    public AuthResponse login(AuthRequest authRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authRequest.getUsername(), authRequest.getPassword()
        ));

        var user = userRepository.findByUsername(authRequest.getUsername())
                .orElseThrow();
        
        var accessToken = jwtService.generateToken(user);
        var refreshToken = createRefreshToken(user);
        
        log.info("Login exitoso para usuario: {}", authRequest.getUsername());

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .build();
    }
    
    @Override
    @Transactional
    public AuthResponse register(AuthRequest authRequest) {
        if (userRepository.findByUsername(authRequest.getUsername()).isPresent()) {
            throw new BusinessException("El usuario ya existe");
        }
        
        var user = User.builder()
                .username(authRequest.getUsername())
                .password(passwordEncoder.encode(authRequest.getPassword()))
                .role(Role.CLIENT)
                .build();
        userRepository.save(user);
        
        var accessToken = jwtService.generateToken(user);
        var refreshToken = createRefreshToken(user);
        
        log.info("Nuevo usuario registrado: {}", authRequest.getUsername());

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new BusinessException("Refresh token no válido"));
        
        if (!refreshToken.isValid()) {
            throw new BusinessException("Refresh token expirado o revocado");
        }
        
        User user = refreshToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        
        if (!jwtService.isRefreshTokenValid(refreshTokenStr, userDetails)) {
            throw new BusinessException("Refresh token inválido");
        }
        
        refreshTokenRepository.delete(refreshToken);
        
        var newAccessToken = jwtService.generateToken(userDetails);
        var newRefreshToken = createRefreshToken(user);
        
        log.info("Token refrescado para usuario: {}", user.getUsername());
        
        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .username(user.getUsername())
                .build();
    }

    @Override
    @Transactional
    public void logout(User user) {
        refreshTokenRepository.revokeAllByUser(user);
        log.info("Logout - tokens revocados para usuario: {}", user.getUsername());
    }

    private String createRefreshToken(User user) {
        refreshTokenRepository.revokeAllByUser(user);
        
        String tokenStr = UUID.randomUUID().toString();
        
        RefreshToken refreshToken = new RefreshToken(
                tokenStr,
                user,
                LocalDateTime.now().plusDays(7)
        );
        
        refreshTokenRepository.save(refreshToken);
        
        return tokenStr;
    }
}
