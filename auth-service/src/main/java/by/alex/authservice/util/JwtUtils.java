package by.alex.authservice.util;

import by.alex.authservice.service.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JwtUtils {

    private final JwtService jwtService;

    public JwtUtils(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    // Генерация JWT-токена
    public String generateJwtToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtService.generateToken(userDetails);
    }

    // Извлечение имени пользователя из токена
    public String getUserNameFromJwtToken(String token) {
        return jwtService.extractUsername(token);
    }

    // Валидация JWT-токена
    public boolean validateJwtToken(String authToken, UserDetails userDetails) {
        return jwtService.isTokenValid(authToken, userDetails);
    }

    public Map<String, Object> validateToken(String authToken) {
        return jwtService.validateToken(authToken);
    }
}