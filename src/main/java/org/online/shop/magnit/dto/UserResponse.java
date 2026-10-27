package org.online.shop.magnit.dto;

import org.online.shop.magnit.model.User;
import java.util.Set;
import java.util.stream.Collectors;
public record UserResponse(Long id, String email, String fullName, Set<String> roles) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(),
            u.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()));
    }
}
