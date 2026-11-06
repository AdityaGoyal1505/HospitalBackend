package com.example.Hospital.dto;


import org.springframework.core.io.Resource;

public record DocumentDownloadDTO(
        Resource resource,
        String filename,
        String contentType,
        long size) {
}