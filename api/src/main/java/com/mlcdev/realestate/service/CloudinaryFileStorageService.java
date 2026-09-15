package com.mlcdev.realestate.service;

import com.cloudinary.Cloudinary;
import com.mlcdev.realestate.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryFileStorageService implements FileStorageService {

    private final Cloudinary cloudinary;

    public CloudinaryFileStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }


    @Override
    public Map<String, String> uploadFile(byte[] file, String folderName) {
        try {
            log.info("Starting upload of the file into the folder: {}", folderName);
            Map<Object, Object> options = new HashMap<>();
            options.put("folder", folderName);
            options.put("quality", 70);
            options.put("format", "jpg");
            options.put("width", 1920);
            options.put("height", 1080);
            options.put("crop", "limit");

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadedFile = cloudinary.uploader().upload(file, options);
            String publicId = (String) uploadedFile.get("public_id");
            String url = cloudinary.url().secure(true).generate(publicId);
            log.debug("File successfully uploaded to Cloudinary. PublicId: {}", publicId);
            Map<String, String> fileInformation = new HashMap<>();
            fileInformation.put("url", url);
            fileInformation.put("fileIdentifier", publicId);
            return fileInformation;
        } catch (Exception e) {
            log.error("Failed to upload file to Cloudinary. Folder: {}", folderName, e);
            throw new FileStorageException("An error occurred on the file upload", e);
        }
    }

    @Override
    public void deleteFile(String fileIdentifier) {
        try {
            log.info("Deleting file from Cloudinary. Identifier: {}", fileIdentifier);
            cloudinary.uploader().destroy(fileIdentifier, Map.of());
            log.debug("File successfully deleted from Cloudinary. Identifier: {}", fileIdentifier);
        } catch (Exception e) {
            log.error("Failed to delete file from Cloudinary. Identifier: {}", fileIdentifier, e);
            throw new FileStorageException("An error occurred on the deletion of the file", e);
        }
    }
}
