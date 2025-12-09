package com.ai.audio.transcriber.controller;

import com.ai.audio.transcriber.dto.AuthResponse;
import com.ai.audio.transcriber.dto.LoginRequest;
import com.ai.audio.transcriber.dto.RegisterRequest;
import com.ai.audio.transcriber.model.Role;
import com.ai.audio.transcriber.model.User;
import com.ai.audio.transcriber.repository.UserRepository;
import com.ai.audio.transcriber.service.CustomUserDetailsService;
import com.ai.audio.transcriber.service.JwtService; // New import
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager; // New import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // New import
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // New dependency
    private final AuthenticationManager authenticationManager; // New dependency
    private final CustomUserDetailsService userDetailsService; // Dependency needed for UserDetails

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          AuthenticationManager authenticationManager,
                          CustomUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, "Email already in use."));
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        userRepository.save(user);

        // Optional: Log the user in immediately after registration
        // We get the UserDetails from the service
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(token, "Registration successful."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // 1. Authenticate credentials using the AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // 2. If authentication is successful (no exception thrown):
        // Retrieve the UserDetails from the successful authentication object
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 3. Generate the JWT
        String token = jwtService.generateToken(userDetails);

        // 4. Return the token to the client
        return ResponseEntity.ok(new AuthResponse(token, "Login successful"));
    }
}