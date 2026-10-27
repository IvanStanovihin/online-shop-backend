package org.online.shop.magnit.service;

import lombok.RequiredArgsConstructor;
import org.online.shop.magnit.dto.*;
import org.online.shop.magnit.model.Role;
import org.online.shop.magnit.repository.*;
import org.online.shop.magnit.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional
public class UserService {
    private final UserRepository users;
    private final RoleRepository roles;
    @Transactional(readOnly = true)
    public UserResponse me(String email) {
        return UserResponse.from(users.findByEmail(email).orElseThrow(() -> ApiException.notFound("Пользователь не найден")));
    }
    @Transactional(readOnly = true)
    public List<UserResponse> list() { return users.findAll().stream().map(UserResponse::from).toList(); }
    @Transactional(readOnly = true)
    public List<Role> roles() { return roles.findAll(); }
    public UserResponse updateRoles(Long id, UpdateRolesRequest request, String actor) {
        roles.lockAdminRole().orElseThrow();
        var user = users.findById(id).orElseThrow(() -> ApiException.notFound("Пользователь не найден"));
        if (user.getEmail().equals(actor) && !request.roles().contains("ROLE_ADMIN"))
            throw ApiException.conflict("Нельзя снять роль администратора у самого себя");
        if (!request.roles().contains("ROLE_ADMIN") && user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"))
                && users.countByRolesName("ROLE_ADMIN") <= 1)
            throw ApiException.conflict("Нельзя удалить последнего администратора");
        var assigned = new HashSet<Role>();
        for (var name : request.roles()) assigned.add(roles.findByName(name).orElseThrow(() -> ApiException.notFound("Роль не найдена")));
        user.setRoles(assigned);
        return UserResponse.from(user);
    }
}
