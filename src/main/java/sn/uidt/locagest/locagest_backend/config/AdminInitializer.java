package sn.uidt.locagest.locagest_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import sn.uidt.locagest.locagest_backend.model.User;
import sn.uidt.locagest.locagest_backend.repository.UserRepository;

@Configuration
public class AdminInitializer {

    @Bean
    CommandLineRunner createAdmin(UserRepository userRepository,
                                  PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("admin@locagest.sn").isEmpty()) {
                User admin = new User();
                admin.setEmail("admin@locagest.sn");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN");
                userRepository.save(admin);
            }
        };
    }
}
