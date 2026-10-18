package com.lostandfound.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lostandfound.backend.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
public class ImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(ImageStorageService.class);
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    public record StoredImage(String url, String publicId) {}

    private final Cloudinary cloudinary;

    public ImageStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public StoredImage upload(MultipartFile file) {
        return upload(file, "lostandfound/items");
    }

    public StoredImage upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty())
            throw new ApiException(HttpStatus.BAD_REQUEST, "No file uploaded");
        String type = file.getContentType();
        if (type == null || !ALLOWED_TYPES.contains(type))
            throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only JPEG, PNG or WebP images are allowed");
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", folder, "resource_type", "image"));
            return new StoredImage((String) result.get("secure_url"), (String) result.get("public_id"));
        } catch (IOException | RuntimeException e) {
            log.error("Cloudinary upload failed", e);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Image upload failed, please try again");
        }
    }

    /** Best effort: a failed cleanup is logged but never blocks the user. */
    public void delete(String publicId) {
        if (publicId == null) return;
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException | RuntimeException e) {
            log.warn("Could not delete image {} from Cloudinary", publicId, e);
        }
    }
}