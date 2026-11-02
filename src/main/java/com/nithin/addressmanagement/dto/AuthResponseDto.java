package com.nithin.addressmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponseDto {

    private String token;
    private String username;
    private String role;
}
