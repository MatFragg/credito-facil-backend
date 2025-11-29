package com.matfragg.creditofacil.api.service;

import com.matfragg.creditofacil.api.dto.response.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    /**
     * Uploads a file to cloud storage
     * @param file The file to upload
     * @param folder The folder in which to store the file
     * @return UploadResponse containing URL and metadata
     */
    UploadResponse upload(MultipartFile file, String folder);

    /**
     * Uploads a file with a custom public ID
     * @param file The file to upload
     * @param folder The folder in which to store the file
     * @param publicId Custom identifier for the file
     * @return UploadResponse containing URL and metadata
     */
    UploadResponse upload(MultipartFile file, String folder, String publicId);

    /**
     * Deletes a file from cloud storage
     * @param publicId The Cloudinary public ID of the file
     * @return true if deletion was successful, false otherwise
     */
    boolean delete(String publicId);

    /**
     * Generates a transformed URL for an image
     * @param publicId The Cloudinary public ID
     * @param width Desired width
     * @param height Desired height
     * @return The transformed image URL
     */
    String getTransformedUrl(String publicId, int width, int height);
}
