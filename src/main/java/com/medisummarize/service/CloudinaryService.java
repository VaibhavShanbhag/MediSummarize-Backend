package com.medisummarize.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
@AllArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public String uploadFile(File file) throws IOException {
        Map<?,?> uploadResult = cloudinary.uploader().upload(file,
                ObjectUtils.asMap("resource_type", "auto", "type", "upload", "folder", "medisummarize/reports"));
        return uploadResult.get("secure_url").toString();
    }
}
