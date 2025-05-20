
package net.coursework.ems_backend.controller;

import net.coursework.ems_backend.entity.User;
import net.coursework.ems_backend.security.JwtTokenUtil;
import net.coursework.ems_backend.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    public AuthController(JwtTokenUtil jwtTokenUtil, UserService userService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!user.getPassword().equals(password)) { // Тут для навчання, у продакшені треба bcrypt
            throw new RuntimeException("Invalid credentials");
        }

        String role = user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName().replace("ROLE_", ""))
                .orElse("USER");

        return jwtTokenUtil.generateToken(email, role);
    }

    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}

