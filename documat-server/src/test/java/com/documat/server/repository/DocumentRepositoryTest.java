package com.documat.server.repository;

import com.documat.server.entity.Document;
import com.documat.server.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DocumentRepositoryTest {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = new User();
        alice.setUsername("alice");
        alice.setPassword("$2a$10$hashed");
        alice.setEmail("alice@example.com");
        alice.setEnabled(true);
        userRepository.save(alice);

        bob = new User();
        bob.setUsername("bob");
        bob.setPassword("$2a$10$hashed2");
        bob.setEmail("bob@example.com");
        bob.setEnabled(true);
        userRepository.save(bob);

        Document d1 = new Document();
        d1.setTitle("Annual Report");
        d1.setDescription("Year-end financial summary");
        d1.setFileName("annual.pdf");
        d1.setFilePath("/tmp/annual.pdf");
        d1.setFileSize(1024L);
        d1.setMimeType("application/pdf");
        d1.setCategory("Finance");
        d1.setTags("finance,annual");
        d1.setOwner(alice);
        documentRepository.save(d1);

        Document d2 = new Document();
        d2.setTitle("Meeting Notes");
        d2.setDescription("Q1 planning notes");
        d2.setFileName("notes.txt");
        d2.setFilePath("/tmp/notes.txt");
        d2.setFileSize(512L);
        d2.setMimeType("text/plain");
        d2.setCategory("HR");
        d2.setTags("meeting,planning");
        d2.setOwner(alice);
        documentRepository.save(d2);

        Document d3 = new Document();
        d3.setTitle("Bob's Invoice");
        d3.setDescription("Invoice for services");
        d3.setFileName("invoice.pdf");
        d3.setFilePath("/tmp/invoice.pdf");
        d3.setFileSize(2048L);
        d3.setMimeType("application/pdf");
        d3.setCategory("Finance");
        d3.setTags("invoice");
        d3.setOwner(bob);
        documentRepository.save(d3);
    }

    @Test
    void findByOwner_alice_returnsTwoDocuments() {
        List<Document> docs = documentRepository.findByOwner(alice);
        assertThat(docs).hasSize(2);
        assertThat(docs).allMatch(d -> d.getOwner().getUsername().equals("alice"));
    }

    @Test
    void findByOwner_bob_returnsOneDocument() {
        List<Document> docs = documentRepository.findByOwner(bob);
        assertThat(docs).hasSize(1);
        assertThat(docs.get(0).getTitle()).isEqualTo("Bob's Invoice");
    }

    @Test
    void findByCategory_finance_returnsTwoDocuments() {
        List<Document> docs = documentRepository.findByCategory("Finance");
        assertThat(docs).hasSize(2);
    }

    @Test
    void findByCategory_hr_returnsOneDocument() {
        List<Document> docs = documentRepository.findByCategory("HR");
        assertThat(docs).hasSize(1);
        assertThat(docs.get(0).getTitle()).isEqualTo("Meeting Notes");
    }

    @Test
    void searchDocuments_byTitleKeyword_returnsMatchingDocuments() {
        List<Document> docs = documentRepository.searchDocuments("report");
        assertThat(docs).hasSize(1);
        assertThat(docs.get(0).getTitle()).isEqualTo("Annual Report");
    }

    @Test
    void searchDocuments_byDescriptionKeyword_returnsMatchingDocuments() {
        List<Document> docs = documentRepository.searchDocuments("planning");
        assertThat(docs).hasSize(1);
        assertThat(docs.get(0).getTitle()).isEqualTo("Meeting Notes");
    }

    @Test
    void searchDocuments_byTagKeyword_returnsMatchingDocuments() {
        List<Document> docs = documentRepository.searchDocuments("invoice");
        assertThat(docs).hasSize(1);
        assertThat(docs.get(0).getOwner().getUsername()).isEqualTo("bob");
    }

    @Test
    void searchDocuments_caseInsensitive_returnsResults() {
        List<Document> docs = documentRepository.searchDocuments("ANNUAL");
        assertThat(docs).hasSize(1);
    }

    @Test
    void searchDocuments_noMatch_returnsEmptyList() {
        List<Document> docs = documentRepository.searchDocuments("xyzzy");
        assertThat(docs).isEmpty();
    }

    @Test
    void save_newDocument_persistsAndAssignsTimestamps() {
        Document d = new Document();
        d.setTitle("New Doc");
        d.setFileName("new.pdf");
        d.setFilePath("/tmp/new.pdf");
        d.setMimeType("application/pdf");
        d.setCategory("General");
        d.setOwner(alice);

        Document saved = documentRepository.save(d);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
