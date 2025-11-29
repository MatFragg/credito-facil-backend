package com.matfragg.creditofacil.api.util;

import com.matfragg.creditofacil.api.exception.StorageException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class FileValidationUtil {

    private static final List<String> ALLOWED_EXTENSIONS = 
        Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    
    private static final List<String> ALLOWED_CONTENT_TYPES = 
        Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
        );
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public static void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("El archivo está vacío o no existe");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new StorageException(
                String.format("El archivo es demasiado grande. Tamaño máximo: %d MB", 
                    MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new StorageException(
                "Tipo de archivo no permitido. Solo se permiten: " + ALLOWED_EXTENSIONS
            );
        }

        // Validate file extension
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new StorageException("El nombre del archivo no es válido");
        }

        String extension = getFileExtension(filename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new StorageException(
                "Extensión de archivo no permitida. Solo se permiten: " + ALLOWED_EXTENSIONS
            );
        }
    }

    public static String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    public static String sanitizeFilename(String filename) {
        if (filename == null) return "unnamed";
        // Remove extension and special characters
        String nameWithoutExt = filename.contains(".") 
            ? filename.substring(0, filename.lastIndexOf("."))
            : filename;
        return nameWithoutExt.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}
