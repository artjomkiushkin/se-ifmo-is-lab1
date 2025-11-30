package com.hrms.service.service;

import com.hrms.core.security.JwtRequest;
import com.hrms.core.security.JwtResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtService jwtService;
    
    public JwtResponse login(JwtRequest request) {
        var userOptional = userService.findByUsername(request.getUsername());
        
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Пользователь не найден");
        }
        
        var user = userOptional.get();
        
        if (!userService.checkPassword(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Неверный пароль");
        }
        
        var token = jwtService.generateToken(user.getUsername());
        return JwtResponse.builder().token(token).build();
    }
    
    public JwtResponse register(JwtRequest request) {
        var existingUser = userService.findByUsername(request.getUsername());
        
        if (existingUser.isPresent()) {
            throw new RuntimeException("Пользователь уже существует");
        }
        
        userService.createUser(request.getUsername(), request.getPassword(), "ROLE_USER");
        return login(request);
    }
}

