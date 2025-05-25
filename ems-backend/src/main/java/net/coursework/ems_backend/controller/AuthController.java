
package net.coursework.ems_backend.controller;

import net.coursework.ems_backend.entity.User;
import net.coursework.ems_backend.security.JwtTokenUtil;
import net.coursework.ems_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class AuthController {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(JwtTokenUtil jwtTokenUtil, UserService userService, PasswordEncoder passwordEncoder) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String email = loginRequest.getEmail();
            String password = loginRequest.getPassword();

            System.out.println("Login attempt for: " + email);

            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));


            if (!passwordEncoder.matches(password, user.getPassword())) {
                System.out.println("Password doesn't match for user: " + email);
                throw new RuntimeException("Invalid credentials");
            }

            String role = user.getRoles().stream()
                    .findFirst()
                    .map(r -> r.getName().replace("ROLE_", ""))
                    .orElse("USER");

            String token = jwtTokenUtil.generateToken(email, role);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("email", email);
            response.put("role", role);

            System.out.println("Login successful for: " + email + " with role: " + role);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> response = new HashMap<>();
            response.put("error", "Authentication failed: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            System.out.println("Trying to register user: " + registerRequest.getEmail());

            if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
                throw new RuntimeException("Email cannot be empty");
            }

            if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().length() < 4) {
                throw new RuntimeException("Password must be at least 4 characters long");
            }

            User user = userService.createUser(
                    registerRequest.getEmail(),
                    registerRequest.getPassword(),
                    Collections.singleton("USER")
            );

            String token = jwtTokenUtil.generateToken(user.getEmail(), "USER");

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("email", user.getEmail());
            response.put("token", token);

            System.out.println("User registered successfully: " + user.getEmail());

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {

            System.err.println("Registration failed: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> response = new HashMap<>();
            response.put("error", "Registration failed: " + e.getMessage());

            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterResponse {
        private String message;
        private String email;
        private String token;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}