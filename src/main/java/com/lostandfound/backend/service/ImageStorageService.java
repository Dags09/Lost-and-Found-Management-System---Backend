package com.lostandfound.backend.service;

import com.lostandfound.backend.exception.ApiException;
import com.lostandfound.backend.models.ImageFile;
import com.lostandfound.backend.models.ImageFile.Purpose;
import com.lostandfound.backend.repository.ImageFileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/** Stores uploaded pictures in PostgreSQL (table "images"). Replaces the old Cloudinary uploader. */
@Service
public class ImageStorageService {

    private final ImageFileRepository images;

    public ImageStorageService(ImageFileRepository images) {
        this.images = images;
    }

    /** Validates the upload and saves it. Returns the new image id. */
    @Transactional
    public String save(MultipartFile file, Purpose purpose) {
        if (file == null || file.isEmpty())
            throw new ApiException(HttpStatus.BAD_REQUEST, "No file uploaded");
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Could not read the uploaded file");
        }
        // Trust the file's real bytes, not the Content-Type header the client sent.
        String type = sniffType(bytes);
        if (type == null)
            throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only JPEG, PNG or WebP images are allowed");
        return images.save(new ImageFile(type, purpose, bytes)).getId();
    }

    /** Loads an image of the given purpose or throws 404. */
    @Transactional(readOnly = true)
    public ImageFile get(String id, Purpose purpose) {
        return images.findById(id)
                .filter(img -> img.getPurpose() == purpose)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Image not found"));
    }

    /** Deletes one image. Call after nothing references it any more. */
    @Transactional
    public void delete(String imageId) {
        if (imageId == null) return;
        images.deleteByIds(List.of(imageId));
    }

    @Transactional
    public void deleteAll(Collection<String> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) return;
        images.deleteByIds(imageIds);
    }

    /** Recognises JPEG, PNG and WebP from their first bytes. Returns null for anything else. */
    private static String sniffType(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF)
            return "image/jpeg";
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A)
            return "image/png";
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P')
            return "image/webp";
        return null;
    }
}