package com.moneibakang.lucenealgoassignment.service;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;

import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/*
 * @Author: Monei Bakang Mothuti
 * @Time: 2352 hours
 * @Date: 13/02/2025
 */
@Service
public class FileUploadService {
    @Value("${file.upload.path}")
    private String uploadPath;

    public String saveFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String filePath = uploadPath + File.separator + fileName;

        File directory = new File(uploadPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        Path path = Paths.get(filePath);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        return filePath;
    }
}