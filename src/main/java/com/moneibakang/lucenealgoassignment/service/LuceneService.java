package com.moneibakang.lucenealgoassignment.service;

/*
 * @Author: Monei Bakang Mothuti
 * @Time: 0206 hours
 * @Date: 13/02/2025
 */

import com.moneibakang.lucenealgoassignment.model.SetswanaEntry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
//import lombok.Value;
import org.springframework.beans.factory.annotation.Value;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.*;
import java.util.*;

// Updated LuceneService.java
@Service
public class LuceneService {
    private static final String INDEX_DIR = "lucene-index";
    private final StandardAnalyzer analyzer;
    private final Directory directory;
    private final IndexWriter writer;

    @Value("${existing.file.path}")
    private String existingFilePath;

    public LuceneService() throws IOException {
        analyzer = new StandardAnalyzer();
        directory = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        // Set to create a new index or overwrite existing one
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        writer = new IndexWriter(directory, config);
    }

    @PostConstruct
    public void init() {
        try {
            // Index the existing file on startup
            indexExistingFile();
        } catch (IOException e) {
//            logger.error("Error indexing existing file: " + e.getMessage());
        }
    }

    public void indexExistingFile() throws IOException {
        File file = new File(existingFilePath);
        if (file.exists()) {
            indexFile(file.getAbsolutePath());
        } else {
            throw new FileNotFoundException("Existing file not found at: " + existingFilePath);
        }
    }

    public void indexFile(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split("\\|:");
                if (parts.length >= 2) {
                    String word = parts[0].trim();
                    String[] remainingParts = parts[1].split(">>");
                    String metadata = remainingParts[0];

                    Document doc = new Document();
                    // Store the word in both original and lowercase form for better searching
                    doc.add(new TextField("word", word, Field.Store.YES));
                    doc.add(new TextField("wordLower", word.toLowerCase(), Field.Store.YES));
                    doc.add(new TextField("metadata", metadata, Field.Store.YES));

                    // Add related words if they exist
                    if (remainingParts.length > 1) {
                        StringBuilder allRelated = new StringBuilder();
                        for (int i = 1; i < remainingParts.length; i++) {
                            String related = remainingParts[i].trim();
                            doc.add(new TextField("related", related, Field.Store.YES));
                            allRelated.append(related).append(" ");
                        }
                        // Add all related words in a single field for better searching
                        doc.add(new TextField("allRelated", allRelated.toString(), Field.Store.NO));
                    }

                    writer.addDocument(doc);
                }
            }
            writer.commit();
        }
    }

    public List<SetswanaEntry> searchWord(String searchTerm) throws IOException {
        List<SetswanaEntry> results = new ArrayList<>();

        try (IndexReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);

            // Create a boolean query to search in multiple fields
            BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

            // Search in word field (exact match)
            queryBuilder.add(new TermQuery(new Term("word", searchTerm)), BooleanClause.Occur.SHOULD);

            // Search in lowercase word field
            queryBuilder.add(new TermQuery(new Term("wordLower", searchTerm.toLowerCase())), BooleanClause.Occur.SHOULD);

            // Search in related words
            queryBuilder.add(new TermQuery(new Term("related", searchTerm)), BooleanClause.Occur.SHOULD);

            TopDocs docs = searcher.search(queryBuilder.build(), 10);

            for (ScoreDoc sd : docs.scoreDocs) {
                Document doc = searcher.doc(sd.doc);
                String word = doc.get("word");
                String metadata = doc.get("metadata");
                String[] related = doc.getValues("related");

                results.add(new SetswanaEntry(word, metadata, related));
            }
        }

        return results;
    }

    // Method to clear and rebuild index
    public void rebuildIndex() throws IOException {
        // Delete all documents
        writer.deleteAll();
        writer.commit();

        // Reindex the existing file
        indexExistingFile();
    }

    // Clean up resources
    @PreDestroy
    public void close() throws IOException {
        writer.close();
        directory.close();
    }
}