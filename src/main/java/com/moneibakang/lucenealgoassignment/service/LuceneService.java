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

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;

@Service
public class LuceneService {

    private static final String INDEX_DIR = "indexDir"; // Folder for Lucene index
    private static final String FILE_PATH = "setswana_text.txt"; // Path to text file

    // Index the text file
    public String indexFile() {
        try {
            Directory directory = FSDirectory.open(Paths.get(INDEX_DIR));
            StandardAnalyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter writer = new IndexWriter(directory, config);

            File file = new File(FILE_PATH);
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
            return "Indexing complete! " + lineNumber + " lines indexed.";

        } catch (Exception e) {
            return "Error during indexing: " + e.getMessage();
        }
    }

    // Search for a word in the indexed file
    public String search(String queryStr) {
        try {
            Directory directory = FSDirectory.open(Paths.get(INDEX_DIR));
            StandardAnalyzer analyzer = new StandardAnalyzer();
            IndexReader reader = DirectoryReader.open(directory);
            IndexSearcher searcher = new IndexSearcher(reader);

            QueryParser parser = new QueryParser("content", analyzer);
            Query query = parser.parse(queryStr);

            TopDocs results = searcher.search(query, 10);
            StringBuilder response = new StringBuilder();
            response.append("Found ").append(results.totalHits).append(" results for query: ").append(queryStr).append("\n");

            for (ScoreDoc sd : results.scoreDocs) {
                Document doc = searcher.doc(sd.doc);
                response.append("Line ").append(doc.get("lineNumber")).append(": ").append(doc.get("content")).append("\n");
            }

            reader.close();
            return response.toString();

        } catch (Exception e) {
            return "Error during search: " + e.getMessage();
        }
    }
}
