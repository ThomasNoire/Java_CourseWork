package net.coursework.ems_backend.service.impl;

import net.coursework.ems_backend.entity.User;
import net.coursework.ems_backend.repository.UserRepository;
import net.coursework.ems_backend.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
