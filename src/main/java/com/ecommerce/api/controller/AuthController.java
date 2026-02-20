package com.ecommerce.api.controller;

import com.ecommerce.api.dto.AuthRequest;
import com.ecommerce.api.dto.AuthResponse;
import com.ecommerce.api.dto.UserResponseDTO;
import com.ecommerce.api.entity.User;
import com.ecommerce.api.mappers.UserMapper;
import com.ecommerce.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticación", description = "Endpoints para registro y login de usuarios")
@RequiredArgsConstructor
@RequestMapping("api/auth")
@RestController
public class AuthController {
    private final AuthService authService;
    private final UserMapper userMapper;

    @Operation(summary = "Obtener información del usuario actual", description = "Retorna los datos del usuario autenticado")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyInfo(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(userMapper.userToUserResponseDTO(user));
    }

    @Operation(summary = "Registrar nuevo usuario", description = "Crea una nueva cuenta de usuario")
    @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente", 
                 content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register (@RequestBody AuthRequest request){
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y retorna el token JWT")
    @ApiResponse(responseCode = "200", description = "Login exitoso", 
                 content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas", content = @Content)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login (@RequestBody AuthRequest request){
        return ResponseEntity.ok(authService.login(request));
    }
}
