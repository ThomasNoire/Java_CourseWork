package net.coursework.ems_backend.service;

import net.coursework.ems_backend.entity.User;
import java.util.Optional;
import java.util.Set;

public interface UserService {
    Optional<User> findByEmail(String email);

    User createUser(String email, String password, Set<String> roleNames);
}