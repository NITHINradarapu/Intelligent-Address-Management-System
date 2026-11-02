package com.nithin.addressmanagement.controller;

import com.nithin.addressmanagement.dto.AuthResponseDto;
import com.nithin.addressmanagement.dto.LoginRequestDto;
import com.nithin.addressmanagement.dto.RegisterRequestDto;
import com.nithin.addressmanagement.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDto register(@Valid @RequestBody RegisterRequestDto requestDto) {
        return authService.register(requestDto);
    }

    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto requestDto) {
        return authService.login(requestDto);
    }
}
