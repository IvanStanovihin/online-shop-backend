package org.online.shop.magnit.service;

import lombok.RequiredArgsConstructor;
import org.online.shop.magnit.dto.*;
import org.online.shop.magnit.model.User;
import org.online.shop.magnit.repository.*;
import org.online.shop.magnit.exception.ApiException;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwt;
    @Transactional
    public void register(RegisterRequest r) {
        if (users.existsByEmail(r.email())) throw ApiException.conflict("Email уже зарегистрирован");
        var role = roles.findByName("ROLE_USER").orElseThrow(() -> new IllegalStateException("Default role missing"));
        var user = new User(); user.setEmail(r.email()); user.setPassword(encoder.encode(r.password()));
        user.setFullName(r.fullName().trim()); user.getRoles().add(role); users.save(user);
    }
    public String login(LoginRequest r) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(r.email(), r.password()));
        return jwt.generateToken(users.findByEmail(r.email()).orElseThrow(() -> new BadCredentialsException("Invalid credentials")));
    }
}
