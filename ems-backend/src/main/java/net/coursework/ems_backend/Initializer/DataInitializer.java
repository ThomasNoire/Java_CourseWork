
package net.coursework.ems_backend.Initializer;

import net.coursework.ems_backend.entity.Role;
import net.coursework.ems_backend.entity.User;
import net.coursework.ems_backend.repository.RoleRepository;
import net.coursework.ems_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner dataLoader(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            System.out.println("Initializing data...");

            Role adminRole = roleRepository.findByName("ROLE_ADMIN");
            if (adminRole == null) {
                adminRole = new Role("ROLE_ADMIN");
                adminRole = roleRepository.save(adminRole);
                System.out.println("Created ROLE_ADMIN");
            }

            Role userRole = roleRepository.findByName("ROLE_USER");
            if (userRole == null) {
                userRole = new Role("ROLE_USER");
                userRole = roleRepository.save(userRole);
                System.out.println("Created ROLE_USER");
            }
            User admin = userRepository.findUserByEmail("admin@example.com");
            if (admin == null) {
                admin = new User();
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRoles(Set.of(adminRole));
                userRepository.save(admin);
                System.out.println("Created admin user with encoded password");
            }

            User user = userRepository.findUserByEmail("user@example.com");
            if (user == null) {
                user = new User();
                user.setEmail("user@example.com");

                user.setPassword(passwordEncoder.encode("user123"));
                user.setRoles(Set.of(userRole));
                userRepository.save(user);
                System.out.println("Created regular user with encoded password");
            }

            System.out.println("Data initialization completed");
        };
    }
}