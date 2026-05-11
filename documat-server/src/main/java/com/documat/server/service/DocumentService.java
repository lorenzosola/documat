package com.documat.server.service;

import com.documat.server.dto.DocumentDto;
import com.documat.server.entity.Document;
import com.documat.server.entity.User;
import com.documat.server.exception.ResourceNotFoundException;
import com.documat.server.repository.DocumentRepository;
import com.documat.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${documat.upload.dir:./uploads}")
    private String uploadDir;

    @Transactional
    public DocumentDto uploadDocument(MultipartFile file, String title, String description, 
                                      String category, String tags, String username) throws IOException {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate file type
        validateFileType(file);

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename, sanitizing the extension to prevent path traversal
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            String rawExt = originalFilename.substring(originalFilename.lastIndexOf("."));
            // Only allow alphanumeric extensions (e.g. .pdf, .docx) to prevent path injection
            if (rawExt.matches("\\.[a-zA-Z0-9]+")) {
                fileExtension = rawExt;
            }
        }
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Resolve and normalize the destination; verify it is inside the upload directory
        Path filePath = uploadPath.resolve(uniqueFilename).normalize();
        if (!filePath.startsWith(uploadPath)) {
            throw new IllegalArgumentException("Invalid file path");
        }
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Document document = new Document();
        document.setTitle(title);
        document.setDescription(description);
        document.setFileName(originalFilename);
        document.setFilePath(filePath.toString());
        document.setFileSize(file.getSize());
        document.setMimeType(file.getContentType());
        document.setCategory(category);
        document.setTags(tags);
        document.setOwner(owner);

        document = documentRepository.save(document);
        return convertToDto(document);
    }

    private void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }
        
        // Allowed MIME types for documents
        List<String> allowedMimeTypes = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
            "image/jpeg",
            "image/png",
            "image/gif"
        );
        
        if (contentType == null || !allowedMimeTypes.contains(contentType)) {
            throw new IllegalArgumentException("File type not allowed: " + contentType);
        }
    }

    public List<DocumentDto> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<DocumentDto> getDocumentsByUsername(String username) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return documentRepository.findByOwner(owner).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public DocumentDto getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        return convertToDto(document);
    }

    public List<DocumentDto> searchDocuments(String keyword) {
        return documentRepository.searchDocuments(keyword).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        
        // Delete physical file
        try {
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but continue with database deletion
            System.err.println("Failed to delete file: " + document.getFilePath() + " - " + e.getMessage());
        }
        
        documentRepository.delete(document);
    }

    public byte[] downloadDocument(Long id) throws IOException {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        
        Path filePath = Paths.get(document.getFilePath());
        return Files.readAllBytes(filePath);
    }

    private DocumentDto convertToDto(Document document) {
        DocumentDto dto = new DocumentDto();
        dto.setId(document.getId());
        dto.setTitle(document.getTitle());
        dto.setDescription(document.getDescription());
        dto.setFileName(document.getFileName());
        dto.setFileSize(document.getFileSize());
        dto.setMimeType(document.getMimeType());
        dto.setCategory(document.getCategory());
        dto.setTags(document.getTags());
        dto.setOwnerUsername(document.getOwner().getUsername());
        dto.setCreatedAt(document.getCreatedAt());
        dto.setUpdatedAt(document.getUpdatedAt());
        dto.setVersion(document.getVersion());
        return dto;
    }
}
