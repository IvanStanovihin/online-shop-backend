package org.online.shop.magnit.dto;

import jakarta.validation.constraints.*;
import org.online.shop.magnit.validation.PasswordBytes;
public record RegisterRequest(
    @NotBlank @Email @Size(max = 255) String email,
    @NotBlank @Size(min = 8, max = 72) @PasswordBytes String password,
    @NotBlank @Size(max = 255) String fullName
) {}
