package com.moneibakang.lucenealgoassignment.controllers;

/*
 * @Author: Monei Bakang Mothuti
 * @Time: 2348 hours
 * @Date: 13/02/2025
 */

import com.moneibakang.lucenealgoassignment.model.SetswanaEntry;
import com.moneibakang.lucenealgoassignment.service.LuceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "http://localhost:3000")
public class SearchController {

    @Autowired
    private LuceneService luceneService;

    // ✅ Rebuild Index with Time Measurement
    @PostMapping("/rebuild-index")
    public ResponseEntity<String> rebuildIndex() {
        long startTime = System.nanoTime(); // Start timer
        try {
            luceneService.rebuildIndex();
            long endTime = System.nanoTime(); // End timer
            double duration = (endTime - startTime) / 1_000_000_000.0; // Convert to seconds
            return ResponseEntity.ok("Index rebuilt successfully in " + duration + " seconds");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error rebuilding index: " + e.getMessage());
        }
    }

    // ✅ Standard Search by Word
    @GetMapping("/word/{term}")
    public ResponseEntity<List<SetswanaEntry>> searchWord(@PathVariable String term) {
        long startTime = System.nanoTime();
        try {
            List<SetswanaEntry> results = luceneService.searchWord(term);
            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / 1_000_000_000.0;
            System.out.println("Search completed in " + duration + " seconds.");
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ Fuzzy Search (Handles Typos and Close Matches)
    @GetMapping("/fuzzy/{term}")
    public ResponseEntity<List<SetswanaEntry>> fuzzySearch(@PathVariable String term) {
        long startTime = System.nanoTime();
        try {
            List<SetswanaEntry> results = luceneService.fuzzySearch(term);
            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / 1_000_000_000.0;
            return ResponseEntity.ok()
                    .body(results.isEmpty() ?
                            List.of(new SetswanaEntry("No results", "No fuzzy matches found", new String[]{}))
                            : results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ Prefix Search (Words starting with input)
    @GetMapping("/prefix/{term}")
    public ResponseEntity<List<SetswanaEntry>> prefixSearch(@PathVariable String term) {
        long startTime = System.nanoTime();
        try {
            List<SetswanaEntry> results = luceneService.prefixSearch(term);
            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / 1_000_000_000.0;
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ Delete Specific Word from Index
    @DeleteMapping("/delete/{term}")
    public ResponseEntity<String> deleteWord(@PathVariable String term) {
        try {
            boolean deleted = luceneService.deleteWord(term);
            return deleted ? ResponseEntity.ok("Word deleted successfully") :
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body("Word not found in index");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting word: " + e.getMessage());
        }
    }

    // ✅ Delete Entire Index
    @DeleteMapping("/delete-index")
    public ResponseEntity<String> deleteIndex() {
        try {
            luceneService.clearIndex();
            return ResponseEntity.ok("Index deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting index: " + e.getMessage());
        }
    }


}
