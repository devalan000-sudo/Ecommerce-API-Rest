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
        if(userRepository.findByUsername("admin").isEmpty()){
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
                    userRepository.save(admin);
            System.out.println("==== Usuario Admin creado exitosamente =====");

        }

        if (userRepository.findByUsername("client").isEmpty()){
            User user = User.builder()
                    .username("client")
                    .password(passwordEncoder.encode("client123"))
                    .role(Role.CLIENT)
                    .build();
            userRepository.save(user);
            System.out.println("===== Usuario Cliente creado exitosamente =====");
        }
    }
}
