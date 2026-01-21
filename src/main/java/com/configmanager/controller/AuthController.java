package com.configmanager.controller;

import com.configmanager.dto.ErrorResponseDTO;
import com.configmanager.dto.LoginRequestDTO;
import com.configmanager.dto.LoginResponseDTO;
import com.configmanager.entity.User;
import com.configmanager.mapper.DTOMapper;
import com.configmanager.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private DTOMapper dtoMapper;

    @Value("${app.jwt.secret}")
    private String jwtSecret;


    private final long jwtExpirationMs = 86400000; // 1 gün

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsernameOrEmail());
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(loginRequest.getUsernameOrEmail());
        }
        if (userOpt.isEmpty()) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Kullanıcı bulunamadı"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        User user = userOpt.get();
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Şifre hatalı"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        String token = generateJwtToken(user);
        LoginResponseDTO response = dtoMapper.toLoginResponseDTO(user, token);
        return ResponseEntity.ok(response);
    }

    private String generateJwtToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret.getBytes(StandardCharsets.UTF_8))
                .compact();
    }
}
