package com.scutelnic.rutex.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) throws IOException {
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "folder", "rutex/profile-images",
                    "public_id", "profile_" + System.currentTimeMillis(),
                    "overwrite", true,
                    "resource_type", "image"
                )
            );
            
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new IOException("Eroare la încărcarea imaginii pe Cloudinary: " + e.getMessage());
        }
    }

    public String uploadChatImage(MultipartFile file) throws IOException {
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "folder", "rutex/chat-images",
                    "public_id", "chat_" + System.currentTimeMillis(),
                    "overwrite", true,
                    "resource_type", "image"
                )
            );

            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new IOException("Eroare la încărcarea imaginii pe Cloudinary: " + e.getMessage());
        }
    }

    public String uploadImageFromUrl(String imageUrl) throws IOException {
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                imageUrl,
                ObjectUtils.asMap(
                    "folder", "rutex/profile-images",
                    "public_id", "profile_google_" + System.currentTimeMillis(),
                    "overwrite", true,
                    "resource_type", "image"
                )
            );

            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new IOException("Eroare la încărcarea imaginii Google pe Cloudinary: " + e.getMessage());
        }
    }

    public void deleteImage(String publicId) throws IOException {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new IOException("Eroare la ștergerea imaginii de pe Cloudinary: " + e.getMessage());
        }
    }

    public String getImageUrl(String publicId) {
        return cloudinary.url().generate(publicId);
    }
}
