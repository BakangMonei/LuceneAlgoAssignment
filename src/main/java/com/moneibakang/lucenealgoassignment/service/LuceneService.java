package com.moneibakang.lucenealgoassignment.service;

/*
 * @Author: Monei Bakang Mothuti
 * @Time: 0206 hours
 * @Date: 13/02/2025
 */
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.*;

@Service
public class LuceneService {

    @Value("${lucene.index.path}")
    private String indexPath;

    @Value("${lucene.file.path}")
    private String filePath;

    // Index the Setswana text file
    public int indexFile() {
        try {
            File indexDir = new File(indexPath);
            if (!indexDir.exists()) indexDir.mkdirs();  // Create the directory if it doesn't exist

            Directory directory = FSDirectory.open(Paths.get(indexPath));
            StandardAnalyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter writer = new IndexWriter(directory, config);

            File file = new File(filePath);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                Document doc = new Document();
                doc.add(new StringField("lineNumber", String.valueOf(lineNumber), Field.Store.YES));
                doc.add(new TextField("content", line, Field.Store.YES));
                writer.addDocument(doc);
                lineNumber++;
            }

            br.close();
            writer.close();
            return lineNumber;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Search for a word in the indexed data
    public Map<String, Object> search(String queryStr) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, String>> resultsList = new ArrayList<>();

        try {
            Directory directory = FSDirectory.open(Paths.get(indexPath));
            IndexReader reader = DirectoryReader.open(directory);
            IndexSearcher searcher = new IndexSearcher(reader);

            StandardAnalyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("content", analyzer);
            Query query = parser.parse(queryStr);
            TopDocs results = searcher.search(query, 10);

            response.put("query", queryStr);
            response.put("total_results", results.totalHits.value);

            for (ScoreDoc sd : results.scoreDocs) {
                Document doc = searcher.doc(sd.doc);
                Map<String, String> result = new HashMap<>();
                result.put("line", doc.get("lineNumber"));
                result.put("text", doc.get("content"));
                resultsList.add(result);
            }

            response.put("results", resultsList);
            reader.close();

        } catch (Exception e) {
            response.put("error", "Error during search: " + e.getMessage());
        }

        return response;
    }

    // Clear the index
    public void clearIndex() {
        try {
            Directory directory = FSDirectory.open(Paths.get(indexPath));
            IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
            IndexWriter writer = new IndexWriter(directory, config);
            writer.deleteAll();
            writer.commit();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get index statistics
    public Map<String, Object> getIndexStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            Directory directory = FSDirectory.open(Paths.get(indexPath));
            IndexReader reader = DirectoryReader.open(directory);
            stats.put("total_lines_indexed", reader.numDocs());
            stats.put("index_size_bytes", directory.listAll().length);
            reader.close();
        } catch (Exception e) {
            stats.put("error", "Failed to retrieve index stats: " + e.getMessage());
        }
        return stats;
    }
}
