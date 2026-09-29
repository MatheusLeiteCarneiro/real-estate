package com.mlcdev.realestate.api.service;


import java.util.Map;

public interface FileStorageService {

    Map<String, String> uploadFile(byte[] file, String folderName);

    void deleteFile(String fileIdentifier);
}
