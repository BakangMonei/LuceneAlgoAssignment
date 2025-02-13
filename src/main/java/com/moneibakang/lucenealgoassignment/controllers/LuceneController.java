package com.moneibakang.lucenealgoassignment.controllers;

/*
 * @Author: Monei Bakang Mothuti
 * @Time: 0222 hours
 * @Date: 13/02/2025
 */

import com.moneibakang.lucenealgoassignment.service.LuceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/lucene")
public class LuceneController {

    @Autowired
    private LuceneService luceneService;

    // Index the Setswana text file
    @PostMapping("/index")
    public Map<String, Object> indexFile() {
        int totalIndexed = luceneService.indexFile();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Indexing complete!");
        response.put("total_lines_indexed", totalIndexed);
        return response;
    }

    // Search for a word
    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam String query) {
        return luceneService.search(query);
    }

    // Clear the index
    @DeleteMapping("/clear")
    public Map<String, String> clearIndex() {
        luceneService.clearIndex();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Index cleared successfully!");
        return response;
    }

    // Get index statistics
    @GetMapping("/stats")
    public Map<String, Object> getIndexStats() {
        return luceneService.getIndexStats();
    }
}
