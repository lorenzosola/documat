package com.documat.server.service;

import com.documat.server.dto.DocumentDto;
import com.documat.server.entity.Document;
import com.documat.server.entity.Role;
import com.documat.server.entity.User;
import com.documat.server.exception.ResourceNotFoundException;
import com.documat.server.repository.DocumentRepository;
import com.documat.server.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DocumentService documentService;

    private User owner;
    private Document sampleDocument;

    @BeforeEach
    void setUp() throws Exception {
        // Point upload dir to a real temp directory so file I/O succeeds
        String tmpDir = Files.createTempDirectory("documat-test-").toString();
        ReflectionTestUtils.setField(documentService, "uploadDir", tmpDir);

        Role role = new Role();
        role.setId(1L);
        role.setName(Role.RoleName.ROLE_USER);

        owner = new User();
        owner.setId(1L);
        owner.setUsername("alice");
        owner.setEmail("alice@example.com");
        owner.setEnabled(true);
        owner.setRoles(Set.of(role));

        sampleDocument = new Document();
        sampleDocument.setId(1L);
        sampleDocument.setTitle("Test Doc");
        sampleDocument.setDescription("desc");
        sampleDocument.setFileName("test.pdf");
        sampleDocument.setFilePath(tmpDir + "/test.pdf");
        sampleDocument.setFileSize(100L);
        sampleDocument.setMimeType("application/pdf");
        sampleDocument.setCategory("Reports");
        sampleDocument.setTags("tag1,tag2");
        sampleDocument.setOwner(owner);
        sampleDocument.setVersion(1);
        sampleDocument.setCreatedAt(LocalDateTime.now());
        sampleDocument.setUpdatedAt(LocalDateTime.now());
    }

    // ── getAllDocuments ──────────────────────────────────────────────────────

    @Test
    void getAllDocuments_returnsAllMappedDtos() {
        when(documentRepository.findAll()).thenReturn(List.of(sampleDocument));

        List<DocumentDto> result = documentService.getAllDocuments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Doc");
        assertThat(result.get(0).getOwnerUsername()).isEqualTo("alice");
    }

    @Test
    void getAllDocuments_emptyRepo_returnsEmptyList() {
        when(documentRepository.findAll()).thenReturn(List.of());

        assertThat(documentService.getAllDocuments()).isEmpty();
    }

    // ── getDocumentById ──────────────────────────────────────────────────────

    @Test
    void getDocumentById_found_returnsDto() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(sampleDocument));

        DocumentDto dto = documentService.getDocumentById(1L);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getMimeType()).isEqualTo("application/pdf");
    }

    @Test
    void getDocumentById_notFound_throwsResourceNotFoundException() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getDocumentById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Document not found");
    }

    // ── getDocumentsByUsername ───────────────────────────────────────────────

    @Test
    void getDocumentsByUsername_found_returnsDtos() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(owner));
        when(documentRepository.findByOwner(owner)).thenReturn(List.of(sampleDocument));

        List<DocumentDto> result = documentService.getDocumentsByUsername("alice");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOwnerUsername()).isEqualTo("alice");
    }

    @Test
    void getDocumentsByUsername_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getDocumentsByUsername("ghost"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── searchDocuments ──────────────────────────────────────────────────────

    @Test
    void searchDocuments_matchingKeyword_returnsResults() {
        when(documentRepository.searchDocuments("test")).thenReturn(List.of(sampleDocument));

        List<DocumentDto> result = documentService.searchDocuments("test");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Doc");
    }

    @Test
    void searchDocuments_noMatches_returnsEmptyList() {
        when(documentRepository.searchDocuments("xyz")).thenReturn(List.of());

        assertThat(documentService.searchDocuments("xyz")).isEmpty();
    }

    // ── deleteDocument ───────────────────────────────────────────────────────

    @Test
    void deleteDocument_found_deletesFromRepository() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(sampleDocument));

        documentService.deleteDocument(1L);

        verify(documentRepository).delete(sampleDocument);
    }

    @Test
    void deleteDocument_notFound_throwsResourceNotFoundException() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.deleteDocument(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(documentRepository, never()).delete(any());
    }

    // ── uploadDocument – file type validation ─────────────────────────────────

    @Test
    void uploadDocument_allowedMimeType_savesDocument() throws IOException {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(owner));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> {
            Document d = inv.getArgument(0);
            d.setId(1L);
            d.setCreatedAt(LocalDateTime.now());
            d.setUpdatedAt(LocalDateTime.now());
            return d;
        });

        MockMultipartFile file = new MockMultipartFile(
                "file", "report.pdf", "application/pdf", "PDF content".getBytes());

        DocumentDto dto = documentService.uploadDocument(
                file, "Report", "description", "Finance", "tag1", "alice");

        assertThat(dto.getTitle()).isEqualTo("Report");
        assertThat(dto.getMimeType()).isEqualTo("application/pdf");
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    void uploadDocument_disallowedMimeType_throwsIllegalArgumentException() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(owner));

        MockMultipartFile file = new MockMultipartFile(
                "file", "script.exe", "application/octet-stream", "binary".getBytes());

        assertThatThrownBy(() ->
                documentService.uploadDocument(file, "Title", null, "cat", null, "alice"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File type not allowed");
    }

    @Test
    void uploadDocument_emptyFilename_throwsIllegalArgumentException() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(owner));

        MockMultipartFile file = new MockMultipartFile(
                "file", "", "application/pdf", "content".getBytes());

        assertThatThrownBy(() ->
                documentService.uploadDocument(file, "Title", null, "cat", null, "alice"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Filename cannot be empty");
    }

    @Test
    void uploadDocument_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "content".getBytes());

        assertThatThrownBy(() ->
                documentService.uploadDocument(file, "Title", null, "cat", null, "ghost"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── downloadDocument ─────────────────────────────────────────────────────

    @Test
    void downloadDocument_notFound_throwsResourceNotFoundException() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.downloadDocument(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
