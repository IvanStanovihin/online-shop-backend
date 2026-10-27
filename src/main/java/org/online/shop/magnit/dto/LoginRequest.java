package org.online.shop.magnit.dto;

import jakarta.validation.constraints.*;
import org.online.shop.magnit.validation.PasswordBytes;
public record LoginRequest(
    @NotBlank @Email @Size(max = 255) String email,
    @NotBlank @Size(max = 72) @PasswordBytes String password
) {}
