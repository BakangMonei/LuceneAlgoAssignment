package com.moneibakang.lucenealgoassignment.service;

/*
 * @Author: Monei Bakang Mothuti
 * @Time: 0206 hours
 * @Date: 13/02/2025
 */

import com.moneibakang.lucenealgoassignment.model.SetswanaEntry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.annotation.Value;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

@Service
public class LuceneService {
    private final StandardAnalyzer analyzer;
    private final Directory directory;
    private final IndexWriter writer;

    @Value("${existing.file.path}")
    private String existingFilePath;

    private static final int MAX_RESULTS = 10;

    public LuceneService() throws IOException {
        analyzer = new StandardAnalyzer();
        directory = FSDirectory.open(Paths.get("lucene-index"));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        writer = new IndexWriter(directory, config);
    }

    @PostConstruct
    public void init() {
        try {
            long startTime = System.nanoTime();
            indexExistingFile();
            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / 1_000_000_000.0;
            System.out.println("Indexing completed in " + duration + " seconds.");
        } catch (IOException e) {
            e.printStackTrace();
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
        long startTime = System.nanoTime();
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
                    doc.add(new TextField("word", word, Field.Store.YES));
                    doc.add(new TextField("wordLower", word.toLowerCase(), Field.Store.YES));
                    doc.add(new TextField("metadata", metadata, Field.Store.YES));

                    if (remainingParts.length > 1) {
                        StringBuilder allRelated = new StringBuilder();
                        for (int i = 1; i < remainingParts.length; i++) {
                            String related = remainingParts[i].trim();
                            doc.add(new TextField("related", related, Field.Store.YES));
                            allRelated.append(related).append(" ");
                        }
                        doc.add(new TextField("allRelated", allRelated.toString(), Field.Store.NO));
                    }

                    writer.addDocument(doc);
                }
            }
            writer.commit();
        }
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000_000.0;
        System.out.println("Indexing took " + duration + " seconds.");
    }

    public List<SetswanaEntry> searchWord(String searchTerm) throws IOException {
        long startTime = System.nanoTime();
        List<SetswanaEntry> results = new ArrayList<>();

        try (IndexReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

            queryBuilder.add(new TermQuery(new Term("word", searchTerm)), BooleanClause.Occur.SHOULD);
            queryBuilder.add(new TermQuery(new Term("wordLower", searchTerm.toLowerCase())), BooleanClause.Occur.SHOULD);
            queryBuilder.add(new TermQuery(new Term("related", searchTerm)), BooleanClause.Occur.SHOULD);

            TopDocs docs = searcher.search(queryBuilder.build(), MAX_RESULTS);
            for (ScoreDoc sd : docs.scoreDocs) {
                Document doc = searcher.doc(sd.doc);
                results.add(new SetswanaEntry(doc.get("word"), doc.get("metadata"), doc.getValues("related")));
            }
        }
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000_000.0;
        System.out.println("Search took " + duration + " seconds.");
        return results;
    }

    // Fuzzy Search (Handles typos and close matches)
    public List<SetswanaEntry> fuzzySearch(String searchTerm) throws IOException {
        long startTime = System.nanoTime();
        List<SetswanaEntry> results = new ArrayList<>();

        try (IndexReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            Query fuzzyQuery = new FuzzyQuery(new Term("word", searchTerm), 2); // Max 2 edit distances
            TopDocs docs = searcher.search(fuzzyQuery, MAX_RESULTS);

            for (ScoreDoc sd : docs.scoreDocs) {
                Document doc = searcher.doc(sd.doc);
                results.add(new SetswanaEntry(doc.get("word"), doc.get("metadata"), doc.getValues("related")));
            }
        }
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000_000.0;
        System.out.println("Fuzzy search took " + duration + " seconds.");
        return results;
    }

    // Prefix Search (Words starting with input)
    public List<SetswanaEntry> prefixSearch(String searchTerm) throws IOException {
        long startTime = System.nanoTime();
        List<SetswanaEntry> results = new ArrayList<>();

        try (IndexReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            Query prefixQuery = new PrefixQuery(new Term("word", searchTerm));
            TopDocs docs = searcher.search(prefixQuery, MAX_RESULTS);

            for (ScoreDoc sd : docs.scoreDocs) {
                Document doc = searcher.doc(sd.doc);
                results.add(new SetswanaEntry(doc.get("word"), doc.get("metadata"), doc.getValues("related")));
            }
        }
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000_000.0;
        System.out.println("Prefix search took " + duration + " seconds.");
        return results;
    }

    // ✅ Delete Specific Word from Index
    public boolean deleteWord(String searchTerm) throws IOException {
        long startTime = System.nanoTime();
        Term term = new Term("word", searchTerm);
        writer.deleteDocuments(term);
        writer.commit();
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000_000.0;
        System.out.println("Deletion took " + duration + " seconds.");
        return true;
    }

    // ✅ Delete Entire Index
    public void clearIndex() throws IOException {
        writer.deleteAll();
        writer.commit();
        System.out.println("Index cleared.");
    }

    @PreDestroy
    public void close() throws IOException {
        writer.close();
        directory.close();
    }

    // Method to clear and rebuild index
    public void rebuildIndex() throws IOException {
        writer.deleteAll();
        writer.commit();
        indexExistingFile();
    }
}
