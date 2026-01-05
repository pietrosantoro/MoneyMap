package com.pietrosantoro.auth;

import com.pietrosantoro.database.entity.UserEntity;
import com.pietrosantoro.database.UserRepository;
import com.pietrosantoro.dto.AuthResponse;
import com.pietrosantoro.dto.LoginRequest;
import com.pietrosantoro.dto.RegisterRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthService(UserRepository repo,
                       PasswordEncoder encoder,
                       AuthenticationManager authManager,
                       JwtService jwtService) {
        this.repo = repo;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest req) {
        if (repo.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        UserEntity u = new UserEntity();
        u.setFirstName(req.getFirstName());
        u.setLastName(req.getLastName());
        u.setEmail(req.getEmail());
        u.setPasswordHash(encoder.encode(req.getPassword()));

        repo.save(u);
    }

    public AuthResponse login(LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        String token = jwtService.generateToken(req.getEmail());
        return new AuthResponse(token, jwtService.getExpirationSeconds());
    }
}
