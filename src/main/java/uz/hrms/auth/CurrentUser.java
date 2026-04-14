package uz.hrms.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record CurrentUser(
        UUID userId,
        UUID employeeId,
        String username,
        String passwordHash,
        boolean active,
        Set<String> roles,
        Set<String> permissions
) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<String> values = new LinkedHashSet<>();
        roles.forEach(role -> values.add("ROLE_" + role));
        values.addAll(permissions);
        return values.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    @Override
    public String getPassword() { return passwordHash; }
    @Override
    public String getUsername() { return username; }
    @Override
    public boolean isAccountNonExpired() { return active; }
    @Override
    public boolean isAccountNonLocked() { return active; }
    @Override
    public boolean isCredentialsNonExpired() { return active; }
    @Override
    public boolean isEnabled() { return active; }
}
