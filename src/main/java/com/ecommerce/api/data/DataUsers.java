package com.ecommerce.api.data;

import com.ecommerce.api.entity.User;
import com.ecommerce.api.entity.enums.Role;
import com.ecommerce.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataUsers implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if(!userRepository.existsByUsername("admin")){
            User admin = User.builder()
                    .username("admin")
                    .email("admin@ecommerce.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            System.out.println("==== Usuario Admin creado: admin@ecommerce.com / admin123 =====");
        }

        if (!userRepository.existsByUsername("client")){
            User user = User.builder()
                    .username("client")
                    .email("client@ecommerce.com")
                    .password(passwordEncoder.encode("client123"))
                    .role(Role.CLIENT)
                    .enabled(true)
                    .build();
            userRepository.save(user);
            System.out.println("===== Usuario Cliente creado: client@ecommerce.com / client123 =====");
        }
    }
}
