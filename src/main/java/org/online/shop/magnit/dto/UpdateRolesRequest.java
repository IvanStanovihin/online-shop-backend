package org.online.shop.magnit.dto;

import jakarta.validation.constraints.*;

public record UpdateRolesRequest(@NotEmpty @Size(max = 3) java.util.Set<@NotBlank @Pattern(regexp = "ROLE_USER|ROLE_MANAGER|ROLE_ADMIN") String> roles) {}
