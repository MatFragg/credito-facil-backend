package com.matfragg.creditofacil.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    
    private String url;           // Secure URL for the image
    private String publicId;      // Cloudinary public ID (for deletion)
    private String originalName;  // Original filename
    private Long size;            // File size in bytes
    private String format;        // File format (jpg, png, etc.)
    private Integer width;        // Image width
    private Integer height;       // Image height
}
