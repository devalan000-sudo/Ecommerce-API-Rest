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

    @Operation(summary = "Registrar nuevo usuario", description = "Crea una nueva cuenta de usuario (requiere confirmación por email)")
    @ApiResponse(responseCode = "200", description = "Usuario registrado, revisa el correo para confirmar", 
                 content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "400", description = "El email ya está registrado", content = @Content)
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register (@RequestBody AuthRequest request){
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y retorna el token JWT (requiere email confirmado)")
    @ApiResponse(responseCode = "200", description = "Login exitoso", 
                 content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas o email no confirmado", content = @Content)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login (@RequestBody AuthRequest request){
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Confirmar email", description = "Confirma el email del usuario con el token")
    @ApiResponse(responseCode = "200", description = "Email confirmado exitosamente", 
                 content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "400", description = "Token inválido o expirado", content = @Content)
    @GetMapping("/confirm")
    public ResponseEntity<AuthResponse> confirmEmail (@RequestParam String token){
        return ResponseEntity.ok(authService.confirmEmail(token));
    }

    @Operation(summary = "Reenviar correo de confirmación", description = "Reenvía el correo de confirmación al email proporcionado")
    @ApiResponse(responseCode = "200", description = "Correo reenviado", content = @Content)
    @ApiResponse(responseCode = "400", description = "Usuario no encontrado o ya confirmado", content = @Content)
    @PostMapping("/resend-confirmation")
    public ResponseEntity<AuthResponse> resendConfirmation (@RequestBody ResendConfirmationRequest request){
        authService.resendConfirmationEmail(request.getEmail());
        return ResponseEntity.ok(AuthResponse.builder()
                .message("Correo de confirmación reenviado")
                .build());
    }

    @Operation(summary = "Refrescar token", description = "Obtiene un nuevo access token usando el refresh token")
    @ApiResponse(responseCode = "200", description = "Token refrescado exitosamente", 
                 content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado", content = @Content)
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken (@RequestBody RefreshTokenRequest request){
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @Operation(summary = "Cerrar sesión", description = "Invalida el refresh token del usuario")
    @ApiResponse(responseCode = "200", description = "Logout exitoso")
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout (@AuthenticationPrincipal User user){
        authService.logout(user);
        return ResponseEntity.ok().build();
    }
}
