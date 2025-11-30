package com.hrms.api.config;

import com.hrms.core.model.entity.User;
import com.hrms.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationListener<ContextRefreshedEvent> {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            var admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRoles("ROLE_ADMIN");
            userRepository.save(admin);
        }
        
        if (userRepository.findByUsername("user").isEmpty()) {
            var user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user"));
            user.setRoles("ROLE_USER");
            userRepository.save(user);
        }
    }
}

