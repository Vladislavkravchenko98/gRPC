package com.example.grpcuser.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CarInputCommand(
        @NotBlank @Size(max = 64) String vin,
        @NotBlank @Size(max = 128) String brand,
        @NotBlank @Size(max = 128) String model,
        @Min(1900) @Max(2100) int productionYear
) {
}
