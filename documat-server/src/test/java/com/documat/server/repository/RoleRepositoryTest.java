package com.documat.server.repository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class RoleRepositoryTest {

    @Test
    void contextLoads() {
        // This test verifies that the repository layer loads successfully
        // Full integration tests require MySQL
    }
}
