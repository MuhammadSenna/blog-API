package com.example.blog.security;

import com.example.blog.entity.User;
import com.example.blog.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetailsService implementation for loading user-specific data.
 * 
 * Validates Requirements:
 * - 2.1: Load user by unique identifier (username or email)
 * - 2.2: Query by username field
 * - 2.3: Query by email field
 * - 2.4: Access password field for authentication
 * - 2.5: Handle user not found scenarios
 * - 2.6: Map user to Spring Security UserDetails
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads user by username or email.
     * 
     * @param usernameOrEmail the username or email to search for
     * @return UserDetails object containing user information
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Try to find by username first, then by email
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "User not found with username or email: " + usernameOrEmail)));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                getAuthorities()
        );
    }

    /**
     * Returns the authorities (roles) for the user.
     * Currently returns default ROLE_USER for all users.
     * 
     * @return collection of granted authorities
     */
    private Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
