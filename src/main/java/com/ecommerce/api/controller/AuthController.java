package com.ecommerce.api.controller;

import com.ecommerce.api.dto.AuthRequest;
import com.ecommerce.api.dto.AuthResponse;
import com.ecommerce.api.dto.UserResponseDTO;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.mappers.UserMapper;
import com.ecommerce.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("api/auth")
@RestController
public class AuthController {
    private final AuthService authService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyInfo(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(userMapper.userToUserResponseDTO(user));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register (@RequestBody AuthRequest request){
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login (@RequestBody AuthRequest request){
        return ResponseEntity.ok(authService.login(request));
    }
}
