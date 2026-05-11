package com.documat.server.service;

import com.documat.server.entity.Role;
import com.documat.server.entity.User;
import com.documat.server.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setId(1L);
        role.setName(Role.RoleName.ROLE_USER);

        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("alice");
        sampleUser.setPassword("$2a$10$hashedpassword");
        sampleUser.setEmail("alice@example.com");
        sampleUser.setEnabled(true);
        sampleUser.setRoles(Set.of(role));
    }

    @Test
    void loadUserByUsername_existingUser_returnsUserDetails() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(sampleUser));

        UserDetails details = userDetailsService.loadUserByUsername("alice");

        assertThat(details.getUsername()).isEqualTo("alice");
        assertThat(details.getPassword()).isEqualTo("$2a$10$hashedpassword");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
    }

    @Test
    void loadUserByUsername_unknownUser_throwsUsernameNotFoundException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ghost");
    }

    @Test
    void loadUserByUsername_disabledUser_returnsDisabledUserDetails() {
        sampleUser.setEnabled(false);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(sampleUser));

        UserDetails details = userDetailsService.loadUserByUsername("alice");

        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsername_adminRole_hasAdminAuthority() {
        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName(Role.RoleName.ROLE_ADMIN);
        sampleUser.setRoles(Set.of(adminRole));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(sampleUser));

        UserDetails details = userDetailsService.loadUserByUsername("alice");

        assertThat(details.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
