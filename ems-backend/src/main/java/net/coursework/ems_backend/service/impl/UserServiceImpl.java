package net.coursework.ems_backend.service.impl;

import net.coursework.ems_backend.entity.Role;
import net.coursework.ems_backend.entity.User;
import net.coursework.ems_backend.repository.RoleRepository;
import net.coursework.ems_backend.repository.UserRepository;
import net.coursework.ems_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User createUser(String email, String password, Set<String> roleNames) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User with email " + email + " already exists");
        }

        User user = new User();
        user.setEmail(email);

        user.setPassword(passwordEncoder.encode(password));

        Set<Role> roles = roleNames.stream()
                .map(roleName -> {
                    String fullRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
                    Role role = roleRepository.findByName(fullRoleName);
                    if (role == null) {
                        throw new RuntimeException("Role " + fullRoleName + " not found");
                    }
                    return role;
                })
                .collect(Collectors.toSet());

        user.setRoles(roles);

        return userRepository.save(user);
    }
}
