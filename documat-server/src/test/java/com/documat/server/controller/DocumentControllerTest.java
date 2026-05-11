package com.documat.server.controller;

import com.documat.server.config.JwtUtils;
import com.documat.server.dto.DocumentDto;
import com.documat.server.exception.ResourceNotFoundException;
import com.documat.server.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    /**
     * Minimal security configuration for controller-slice tests:
     * CSRF disabled (stateless JWT API), all endpoints require authentication,
     * method security enabled for @PreAuthorize role checks.
     */
    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    // Required by AuthTokenFilter (auto-detected Filter bean in the web slice)
    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsService userDetailsService;

    private DocumentDto sampleDto;

    @BeforeEach
    void setUp() {
        sampleDto = new DocumentDto();
        sampleDto.setId(1L);
        sampleDto.setTitle("Test Document");
        sampleDto.setDescription("A test document");
        sampleDto.setFileName("test.pdf");
        sampleDto.setFileSize(1024L);
        sampleDto.setMimeType("application/pdf");
        sampleDto.setCategory("Reports");
        sampleDto.setTags("test,report");
        sampleDto.setOwnerUsername("alice");
        sampleDto.setCreatedAt(LocalDateTime.now());
        sampleDto.setUpdatedAt(LocalDateTime.now());
        sampleDto.setVersion(1);
    }

    // ── GET /api/documents ───────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    void getAllDocuments_asUser_returns200WithList() throws Exception {
        when(documentService.getAllDocuments()).thenReturn(List.of(sampleDto));

        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Document"))
                .andExpect(jsonPath("$[0].ownerUsername").value("alice"));
    }

    @Test
    void getAllDocuments_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllDocuments_emptyRepo_returnsEmptyList() throws Exception {
        when(documentService.getAllDocuments()).thenReturn(List.of());

        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // ── GET /api/documents/my ────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void getMyDocuments_asOwner_returns200WithOwnedDocuments() throws Exception {
        when(documentService.getDocumentsByUsername("alice")).thenReturn(List.of(sampleDto));

        mockMvc.perform(get("/api/documents/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ownerUsername").value("alice"));
    }

    // ── GET /api/documents/{id} ──────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    void getDocument_found_returns200WithDto() throws Exception {
        when(documentService.getDocumentById(1L)).thenReturn(sampleDto);

        mockMvc.perform(get("/api/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Document"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getDocument_notFound_returns404() throws Exception {
        when(documentService.getDocumentById(99L))
                .thenThrow(new ResourceNotFoundException("Document not found"));

        mockMvc.perform(get("/api/documents/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/documents/search ────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    void searchDocuments_withKeyword_returns200WithResults() throws Exception {
        when(documentService.searchDocuments("report")).thenReturn(List.of(sampleDto));

        mockMvc.perform(get("/api/documents/search").param("keyword", "report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Document"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchDocuments_noMatches_returnsEmptyList() throws Exception {
        when(documentService.searchDocuments("xyzzy")).thenReturn(List.of());

        mockMvc.perform(get("/api/documents/search").param("keyword", "xyzzy"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // ── POST /api/documents/upload ───────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void uploadDocument_validFile_returns200WithDto() throws Exception {
        when(documentService.uploadDocument(any(), eq("Report"), any(), eq("Finance"), any(), eq("alice")))
                .thenReturn(sampleDto);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "PDF content".getBytes());

        mockMvc.perform(multipart("/api/documents/upload")
                        .file(file)
                        .param("title", "Report")
                        .param("category", "Finance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Document"));
    }

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void uploadDocument_serviceThrowsIOException_returns500() throws Exception {
        when(documentService.uploadDocument(any(), any(), any(), any(), any(), any()))
                .thenThrow(new IOException("Disk full"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "PDF content".getBytes());

        mockMvc.perform(multipart("/api/documents/upload")
                        .file(file)
                        .param("title", "Report")
                        .param("category", "Finance"))
                .andExpect(status().isInternalServerError());
    }

    // ── GET /api/documents/download/{id} ────────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    void downloadDocument_found_returns200WithContent() throws Exception {
        byte[] content = "PDF content".getBytes();
        when(documentService.downloadDocument(1L)).thenReturn(content);
        when(documentService.getDocumentById(1L)).thenReturn(sampleDto);

        mockMvc.perform(get("/api/documents/download/1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("test.pdf")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void downloadDocument_ioError_returns500() throws Exception {
        when(documentService.downloadDocument(99L))
                .thenThrow(new IOException("File not found"));

        mockMvc.perform(get("/api/documents/download/99"))
                .andExpect(status().isInternalServerError());
    }

    // ── DELETE /api/documents/{id} ───────────────────────────────────────────

    @Test
    @WithMockUser(roles = "MANAGER")
    void deleteDocument_asManager_returns200() throws Exception {
        mockMvc.perform(delete("/api/documents/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDocument_asAdmin_returns200() throws Exception {
        mockMvc.perform(delete("/api/documents/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteDocument_asUser_returns403() throws Exception {
        mockMvc.perform(delete("/api/documents/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteDocument_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/documents/1"))
                .andExpect(status().isUnauthorized());
    }
}
