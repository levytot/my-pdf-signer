package com.example.pdfsignerpro.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignatureFieldLocation {
    
    @NotNull(message = "Page number is required")
    @Min(value = 1, message = "Page number must be at least 1")
    private int page;
    
    @NotNull(message = "X coordinate is required")
    private float x;
    
    @NotNull(message = "Y coordinate is required")
    private float y;
    
    @NotNull(message = "Width is required")
    @Min(value = 1, message = "Width must be positive")
    private float width;
    
    @NotNull(message = "Height is required")
    @Min(value = 1, message = "Height must be positive")
    private float height;
}