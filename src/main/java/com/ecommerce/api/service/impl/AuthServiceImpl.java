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
import com.ecommerce.api.service.EmailService;
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
    private final EmailService emailService;

    @Override
    public AuthResponse login(AuthRequest authRequest) {
        var user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new BusinessException("Credenciales inválidas"));
        
        if (!user.isEnabled()) {
            throw new BusinessException("Debes confirmar tu email antes de iniciar sesión. Revisa tu correo.");
        }
        
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authRequest.getEmail(), authRequest.getPassword()
        ));
        
        var accessToken = jwtService.generateToken(user);
        var refreshToken = createRefreshToken(user);
        
        log.info("Login exitoso para usuario: {}", authRequest.getEmail());

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .build();
    }
    
    @Override
    @Transactional
    public AuthResponse register(AuthRequest authRequest) {
        if (userRepository.existsByEmail(authRequest.getEmail())) {
            throw new BusinessException("El email ya está registrado");
        }
        
        String confirmationToken = UUID.randomUUID().toString();
        
        var user = User.builder()
                .username(authRequest.getEmail().split("@")[0])
                .email(authRequest.getEmail())
                .password(passwordEncoder.encode(authRequest.getPassword()))
                .role(Role.CLIENT)
                .enabled(false)
                .confirmationToken(confirmationToken)
                .build();
        userRepository.save(user);
        
        emailService.sendConfirmationEmail(authRequest.getEmail(), confirmationToken);
        
        log.info("Nuevo usuario registrado: {}. Email de confirmación enviado.", authRequest.getEmail());

        return AuthResponse.builder()
                .message("Usuario registrado. Por favor, revisa tu correo para confirmar tu cuenta.")
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
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        
        if (!jwtService.isRefreshTokenValid(refreshTokenStr, userDetails)) {
            throw new BusinessException("Refresh token inválido");
        }
        
        if (!user.isEnabled()) {
            throw new BusinessException("Debes confirmar tu email antes de iniciar sesión");
        }
        
        refreshTokenRepository.delete(refreshToken);
        
        var newAccessToken = jwtService.generateToken(userDetails);
        var newRefreshToken = createRefreshToken(user);
        
        log.info("Token refrescado para usuario: {}", user.getEmail());
        
        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .email(user.getEmail())
                .build();
    }

    @Override
    @Transactional
    public void logout(User user) {
        refreshTokenRepository.revokeAllByUser(user);
        log.info("Logout - tokens revocados para usuario: {}", user.getEmail());
    }

    @Override
    @Transactional
    public AuthResponse confirmEmail(String token) {
        User user = userRepository.findAll().stream()
                .filter(u -> token.equals(u.getConfirmationToken()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Token de confirmación inválido"));
        
        if (user.isEnabled()) {
            throw new BusinessException("La cuenta ya está confirmada");
        }
        
        user.setEnabled(true);
        user.setConfirmationToken(null);
        userRepository.save(user);
        
        log.info("Email confirmado para usuario: {}", user.getEmail());
        
        var accessToken = jwtService.generateToken(user);
        var refreshToken = createRefreshToken(user);
        
        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .message("Cuenta confirmada exitosamente")
                .build();
    }

    @Override
    @Transactional
    public void resendConfirmationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        
        if (user.isEnabled()) {
            throw new BusinessException("La cuenta ya está confirmada");
        }
        
        String newToken = UUID.randomUUID().toString();
        user.setConfirmationToken(newToken);
        userRepository.save(user);
        
        emailService.sendConfirmationEmail(email, newToken);
        
        log.info("Email de confirmación reenviado a: {}", email);
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
