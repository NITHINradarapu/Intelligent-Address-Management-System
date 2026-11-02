package com.nithin.addressmanagement.service;

import com.nithin.addressmanagement.dto.AuthResponseDto;
import com.nithin.addressmanagement.dto.LoginRequestDto;
import com.nithin.addressmanagement.dto.RegisterRequestDto;
import com.nithin.addressmanagement.entity.Role;
import com.nithin.addressmanagement.entity.User;
import com.nithin.addressmanagement.repository.UserRepository;
import com.nithin.addressmanagement.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponseDto register(RegisterRequestDto requestDto) {

        // Check if username already taken
        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already taken
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create the user entity
        User user = new User();
        user.setUsername(requestDto.getUsername());
        user.setEmail(requestDto.getEmail());

        // Hash the password before saving — never store plain text passwords
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));

        // Default role is USER. First registered user could be made ADMIN manually.
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        // After registration, automatically log the user in and return a token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDto.getUsername(),
                        requestDto.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponseDto(token, user.getUsername(), user.getRole().name());
    }

    public AuthResponseDto login(LoginRequestDto requestDto) {

        // AuthenticationManager validates username + password.
        // If invalid, it throws BadCredentialsException automatically.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDto.getUsername(),
                        requestDto.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        User user = userRepository.findByUsername(requestDto.getUsername())
                .orElseThrow();

        return new AuthResponseDto(token, user.getUsername(), user.getRole().name());
    }
}
