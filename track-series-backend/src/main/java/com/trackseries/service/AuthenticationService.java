package com.trackseries.service;

import com.trackseries.dto.AuthenticationRequest;
import com.trackseries.dto.AuthenticationResponse;
import com.trackseries.dto.RegisterRequest;
import com.trackseries.entity.User;
import com.trackseries.exception.ConflictException;
import com.trackseries.exception.ResourceNotFoundException;
import com.trackseries.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
        private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository repository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService,
                                 AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse register(RegisterRequest request) {
                log.info("Register request received for username='{}'", request.getUsername());

                if (repository.existsByUsername(request.getUsername())) {
                        throw new ConflictException("Username is already taken");
                }
                if (repository.existsByEmail(request.getEmail())) {
                        throw new ConflictException("Email is already in use");
                }

        // Create new user and encode the password before saving to the database
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        repository.save(user);

        // Generate JWT token for the newly registered user
        String jwtToken = jwtService.generateToken(user);
        log.info("User registered successfully, username='{}'", request.getUsername());

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("Login request received for username='{}'", request.getUsername());
        // Spring Security will authenticate the user, throwing an exception if credentials are bad
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // If we reach this line, the user is authenticated. Let's fetch the user and generate a token
        User user = repository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username " + request.getUsername()));

        String jwtToken = jwtService.generateToken(user);
        log.info("User authenticated successfully, username='{}'", request.getUsername());

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
