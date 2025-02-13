package com.moneibakang.lucenealgoassignment.controllers;

/*
 * @Author: Monei Bakang Mothuti
 * @Time: 0222 hours
 * @Date: 13/02/2025
 */
import com.moneibakang.lucenealgoassignment.service.LuceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lucene")
public class LuceneController {

    @Autowired
    private LuceneService luceneService;

    @PostMapping("/index")
    public String indexFile() {
        return luceneService.indexFile();
    }

    @GetMapping("/search")
    public String search(@RequestParam String query) {
        return luceneService.search(query);
    }
}
