package com.ecommerce.api.service;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    
    private final Cloudinary cloudinary;
    
    public String uploadImage(MultipartFile file) throws IOException {
        Map result = cloudinary.uploader().upload(file.getBytes(), Map.of());
        return result.get("secure_url").toString();
    }
    
    public String uploadImage(File file) throws IOException {
        Map result = cloudinary.uploader().upload(file, Map.of());
        return result.get("secure_url").toString();
    }
}
