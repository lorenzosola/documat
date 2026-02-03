package com.documat.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private Long id;
    private String title;
    private String description;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private String category;
    private String tags;
    private String ownerUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
}
