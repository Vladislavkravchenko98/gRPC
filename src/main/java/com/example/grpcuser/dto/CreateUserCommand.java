package com.example.grpcuser.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserCommand(
        @NotBlank @Size(max = 64) String externalId,
        @NotBlank @Size(max = 128) String firstName,
        @NotBlank @Size(max = 128) String lastName,
        @NotBlank @Email @Size(max = 255) String email
) {
}
