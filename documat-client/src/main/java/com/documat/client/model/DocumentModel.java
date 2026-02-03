package com.documat.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentModel {
    private Long id;
    private String title;
    private String description;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private String category;
    private String tags;
    private String ownerUsername;
    private String createdAt;
    private String updatedAt;
    private Integer version;
}
