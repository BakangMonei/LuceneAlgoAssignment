package com.moneibakang.lucenealgoassignment.controllers;

import com.moneibakang.lucenealgoassignment.service.FileUploadService;
import com.moneibakang.lucenealgoassignment.service.LuceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/*
 * @Author: Monei Bakang Mothuti
 * @Time: 2352 hours
 * @Date: 13/02/2025
 */
// FileController.java
@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://localhost:3000")
public class FileController {

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private LuceneService luceneService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileUploadService.saveFile(file);
            luceneService.indexFile(filePath);
            return ResponseEntity.ok("File uploaded and indexed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing file: " + e.getMessage());
        }
    }
}