package com.documat.server.repository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Basic test to ensure the repository layer is configured correctly.
 * Note: Full integration tests require MySQL database.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RoleRepositoryTest {

    @Test
    void contextLoads() {
        // This test verifies that the repository layer loads successfully
        // Full integration tests require MySQL
    }
}
