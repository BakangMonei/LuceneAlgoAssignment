package com.moneibakang.lucenealgoassignment.controllers;
/*
* @Author: Monei Bakang Mothuti
* @Time: 2348 hours
* @Date: 13/02/2025
*/
import com.moneibakang.lucenealgoassignment.model.SetswanaEntry;
import com.moneibakang.lucenealgoassignment.service.LuceneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

// Updated SearchController.java
@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "http://localhost:3000")
public class SearchController {

    @Autowired
    private LuceneService luceneService;

    @PostMapping("/rebuild-index")
    public ResponseEntity<String> rebuildIndex() {
        try {
            luceneService.rebuildIndex();
            return ResponseEntity.ok("Index rebuilt successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error rebuilding index: " + e.getMessage());
        }
    }

    @GetMapping("/word/{term}")
    public ResponseEntity<List<SetswanaEntry>> searchWord(@PathVariable String term) {
        try {
            List<SetswanaEntry> results = luceneService.searchWord(term);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}