package com.uep.pillar.security;

import com.uep.pillar.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * UserDetailsService implementation for loading user details from the database.
 * Used by Spring Security for authentication.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userService.findByEmail(email)
                .map(user -> {
                    // Convert role to Spring Security authorities
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (user.getRole() != null) {
                        // NOTE: Role formatting differences across components:
                        // - Spring Security expects role-based GrantedAuthority values to be prefixed with "ROLE_"
                        //   (e.g., ROLE_ADMIN, ROLE_EDITOR), so we add the prefix here.
                        // - JwtTokenProvider.generateToken stores the role in JWT claims *without* the "ROLE_" prefix
                        //   (e.g., just "ADMIN", "EDITOR" from Role.getName()).
                        // - When consuming JWT claims, ensure proper prefix handling when mapping to Spring Security authorities.
                        String roleName = "ROLE_" + user.getRole().getName();
                        authorities.add(new SimpleGrantedAuthority(roleName));
                    }

                    return User.builder()
                            .username(user.getEmail())
                            .password(user.getPassword())
                            .authorities(authorities)
                            .accountExpired(false)
                            .accountLocked(false)
                            .credentialsExpired(false)
                            .disabled(false)
                            .build();
                })
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });
    }
}
