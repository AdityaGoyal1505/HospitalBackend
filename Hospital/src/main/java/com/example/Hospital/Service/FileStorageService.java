package com.example.Hospital.service;

import java.io.InputStream;
import org.springframework.core.io.Resource;

public interface FileStorageService {

    // Saves the file and returns its storage key (the server-made name).
    String store(InputStream data, String extension);

    // Returns the stored file, or throws a 404 if it does not exist.
    Resource load(String storageKey);

    // Removes the stored file. Does nothing if it is already gone.
    void delete(String storageKey);
}