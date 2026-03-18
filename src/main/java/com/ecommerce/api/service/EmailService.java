package com.ecommerce.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    public void sendConfirmationEmail(String toEmail, String confirmationToken) {
        try {
            String confirmUrl = "http://localhost:8080/api/auth/confirm?token=" + confirmationToken;
            
            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .button { 
                            display: inline-block; 
                            padding: 12px 24px; 
                            background-color: #2E86AB; 
                            color: white; 
                            text-decoration: none; 
                            border-radius: 5px; 
                            margin: 20px 0;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>Confirma tu cuenta</h1>
                        <p>Gracias por registrarte. Para activar tu cuenta, haz clic en el botón de abajo:</p>
                        <a href="%s" class="button">Confirmar mi cuenta</a>
                        <p>O copia y pega este enlace en tu navegador:</p>
                        <p>%s</p>
                        <p>Este enlace expira en 24 horas.</p>
                        <hr>
                        <p style="color: #666; font-size: 12px;">Si no creaste esta cuenta, puedes ignorar este correo.</p>
                    </div>
                </body>
                </html>
                """.formatted(confirmUrl, confirmUrl);

            Map<String, Object> emailRequest = new HashMap<>();
            emailRequest.put("from", "E-Commerce API <onboarding@resend.dev>");
            emailRequest.put("to", toEmail);
            emailRequest.put("subject", "Confirma tu cuenta - E-Commerce API");
            emailRequest.put("html", htmlContent);

            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + resendApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(emailRequest, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                RESEND_API_URL, 
                request, 
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Email de confirmación enviado a: {}", toEmail);
            } else {
                log.error("Error al enviar email: {}", response.getBody());
            }

        } catch (Exception e) {
            log.error("Error al enviar email de confirmación: {}", e.getMessage());
            throw new RuntimeException("Error al enviar el correo de confirmación", e);
        }
    }
}
