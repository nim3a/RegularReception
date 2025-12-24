package com.daryaftmanazam.daryaftcore.security;

import com.daryaftmanazam.daryaftcore.model.User;
import com.daryaftmanazam.daryaftcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom UserDetailsService implementation for loading user-specific data
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("کاربر یافت نشد: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .accountExpired(false)
                .accountLocked(!user.getEnabled())
                .credentialsExpired(false)
                .disabled(!user.getEnabled())
                .build();
    }
}
