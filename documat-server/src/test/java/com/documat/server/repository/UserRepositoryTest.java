package com.documat.server.repository;

import com.documat.server.entity.Role;
import com.documat.server.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User alice;

    @BeforeEach
    void setUp() {
        // Ensure the ROLE_USER entry exists
        if (roleRepository.findByName(Role.RoleName.ROLE_USER).isEmpty()) {
            Role r = new Role();
            r.setName(Role.RoleName.ROLE_USER);
            roleRepository.save(r);
        }

        alice = new User();
        alice.setUsername("alice");
        alice.setPassword("$2a$10$hashed");
        alice.setEmail("alice@example.com");
        alice.setEnabled(true);
        userRepository.save(alice);
    }

    @Test
    void findByUsername_existingUser_returnsUser() {
        Optional<User> found = userRepository.findByUsername("alice");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void findByUsername_unknownUser_returnsEmpty() {
        assertThat(userRepository.findByUsername("ghost")).isEmpty();
    }

    @Test
    void existsByUsername_existingUser_returnsTrue() {
        assertThat(userRepository.existsByUsername("alice")).isTrue();
    }

    @Test
    void existsByUsername_unknownUser_returnsFalse() {
        assertThat(userRepository.existsByUsername("nobody")).isFalse();
    }

    @Test
    void existsByEmail_existingEmail_returnsTrue() {
        assertThat(userRepository.existsByEmail("alice@example.com")).isTrue();
    }

    @Test
    void existsByEmail_unknownEmail_returnsFalse() {
        assertThat(userRepository.existsByEmail("other@example.com")).isFalse();
    }

    @Test
    void save_newUser_assignsId() {
        User bob = new User();
        bob.setUsername("bob");
        bob.setPassword("$2a$10$hashed2");
        bob.setEmail("bob@example.com");
        bob.setEnabled(true);

        User saved = userRepository.save(bob);

        assertThat(saved.getId()).isNotNull();
    }
}
