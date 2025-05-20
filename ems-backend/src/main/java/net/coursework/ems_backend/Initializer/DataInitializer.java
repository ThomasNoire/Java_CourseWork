package net.coursework.ems_backend.Initializer;

import net.coursework.ems_backend.entity.Role;
import net.coursework.ems_backend.entity.User;
import net.coursework.ems_backend.repository.RoleRepository;
import net.coursework.ems_backend.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public ApplicationRunner dataLoader(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {

            Role adminRole = roleRepository.findByName("ROLE_ADMIN");
            if (adminRole == null) {
                adminRole = roleRepository.save(new Role("ROLE_ADMIN"));
            }

            Role userRole = roleRepository.findByName("ROLE_USER");
            if (userRole == null) {
                userRole = roleRepository.save(new Role("ROLE_USER"));
            }

            if (userRepository.findByEmail("booking.bogdan@gmail.com") == null) {
                User admin = new User();
                admin.setEmail("booking.bogdan@gmail.com");
                admin.setPassword("admin");
                admin.setRoles(Set.of(adminRole));
                userRepository.save(admin);
            }


            if (userRepository.findByEmail("user@example.com") == null) {
                User user = new User();
                user.setEmail("user@example.com");
                user.setPassword("user123");
                user.setRoles(Set.of(userRole));
                userRepository.save(user);
            }
        };
    }
}
