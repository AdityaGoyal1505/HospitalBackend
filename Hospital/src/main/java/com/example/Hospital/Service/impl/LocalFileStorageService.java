package com.example.Hospital.service.impl;

import com.example.Hospital.exception.ResourceNotFoundException;
import com.example.Hospital.exception.StorageException;
import com.example.Hospital.service.FileStorageService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class LocalFileStorageService implements FileStorageService {

    // A key is always: random id + dot + one allowed extension. Nothing else is accepted.
    private static final Pattern KEY_PATTERN = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(pdf|jpg|png)$");

    private final Path root;

    public LocalFileStorageService(@Value("${app.storage.local-dir}") String directory) {
        this.root = Paths.get(directory).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new StorageException("Could not create the storage folder", e);
        }
    }

    @Override
    public String store(InputStream data, String extension) {
        String key = UUID.randomUUID() + "." + extension;
        Path target = resolve(key);
        try (InputStream in = data) {
            Files.copy(in, target); // fails if the file already exists, it never overwrites
        } catch (IOException e) {
            throw new StorageException("Could not save the file", e);
        }
        return key;
    }

    @Override
    public Resource load(String storageKey) {
        Resource resource = new FileSystemResource(resolve(storageKey));
        if (!resource.exists() || !resource.isReadable()) {
            throw new ResourceNotFoundException("File not found");
        }
        return resource;
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(resolve(storageKey));
        } catch (IOException e) {
            throw new StorageException("Could not delete the file", e);
        }
    }

    // Checks the key and makes sure the path stays inside the storage folder.
    private Path resolve(String storageKey) {
        if (storageKey == null || !KEY_PATTERN.matcher(storageKey).matches()) {
            throw new IllegalArgumentException("Invalid storage key");
        }
        Path path = root.resolve(storageKey).normalize();
        if (!path.startsWith(root)) {
            throw new IllegalArgumentException("Invalid storage key");
        }
        return path;
    }
}