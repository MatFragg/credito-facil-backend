package com.matfragg.creditofacil.api.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.matfragg.creditofacil.api.dto.response.UploadResponse;
import com.matfragg.creditofacil.api.exception.StorageException;
import com.matfragg.creditofacil.api.service.StorageService;
import com.matfragg.creditofacil.api.util.FileValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements StorageService {

    private final Cloudinary cloudinary;

    @Value("${storage.cloudinary.folder:creditofacil}")
    private String defaultFolder;

    @Override
    public UploadResponse upload(MultipartFile file, String folder) {
        String publicId = UUID.randomUUID().toString();
        return upload(file, folder, publicId);
    }

    @Override
    public UploadResponse upload(MultipartFile file, String folder, String publicId) {
        // Validate file
        FileValidationUtil.validateImageFile(file);

        try {
            // Sanitize filename for public ID
            String originalFilename = file.getOriginalFilename();
            String sanitizedName = FileValidationUtil.sanitizeFilename(originalFilename);
            String finalPublicId = folder + "/" + sanitizedName + "_" + publicId;

            log.debug("Uploading file to Cloudinary: {} ({})", originalFilename, finalPublicId);

            // Upload to Cloudinary
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "public_id", finalPublicId,
                    "folder", folder,
                    "resource_type", "image",
                    "transformation", new Transformation()
                        .quality("auto")
                        .fetchFormat("auto")
                )
            );

            // Build response
            UploadResponse response = UploadResponse.builder()
                .url((String) uploadResult.get("secure_url"))
                .publicId((String) uploadResult.get("public_id"))
                .originalName(originalFilename)
                .size(file.getSize())
                .format((String) uploadResult.get("format"))
                .width((Integer) uploadResult.get("width"))
                .height((Integer) uploadResult.get("height"))
                .build();

            log.info("File uploaded successfully to Cloudinary: {}", response.getPublicId());
            return response;

        } catch (IOException e) {
            log.error("Error uploading file to Cloudinary: {}", e.getMessage());
            throw new StorageException("Error al subir la imagen a Cloudinary", e);
        }
    }

    @Override
    public boolean delete(String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            log.warn("Attempted to delete with null or empty publicId");
            return false;
        }

        try {
            log.debug("Deleting file from Cloudinary: {}", publicId);
            
            Map<String, Object> result = cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.asMap("resource_type", "image")
            );

            String resultStatus = (String) result.get("result");
            boolean success = "ok".equalsIgnoreCase(resultStatus);

            if (success) {
                log.info("File deleted successfully from Cloudinary: {}", publicId);
            } else {
                log.warn("Failed to delete file from Cloudinary: {} - Result: {}", publicId, resultStatus);
            }

            return success;

        } catch (IOException e) {
            log.error("Error deleting file from Cloudinary: {}", e.getMessage());
            throw new StorageException("Error al eliminar la imagen de Cloudinary", e);
        }
    }

    @Override
    public String getTransformedUrl(String publicId, int width, int height) {
        return cloudinary.url()
            .transformation(new Transformation()
                .width(width)
                .height(height)
                .crop("fill")
                .quality("auto")
                .fetchFormat("auto"))
            .generate(publicId);
    }

    /**
     * Generates a thumbnail URL for property listings (300x200)
     */
    public String getThumbnailUrl(String publicId) {
        return getTransformedUrl(publicId, 300, 200);
    }

    /**
     * Generates a full-size URL for property detail view (800x600)
     */
    public String getDetailUrl(String publicId) {
        return getTransformedUrl(publicId, 800, 600);
    }
}
