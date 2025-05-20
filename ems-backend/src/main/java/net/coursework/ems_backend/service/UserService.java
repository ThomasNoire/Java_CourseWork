package net.coursework.ems_backend.service;

import net.coursework.ems_backend.entity.User;
import java.util.Optional;

public interface UserService {
    Optional<User> findByEmail(String email);
}
