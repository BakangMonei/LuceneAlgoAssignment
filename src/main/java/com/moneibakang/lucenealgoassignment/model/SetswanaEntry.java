package com.moneibakang.lucenealgoassignment.model;

/*
 * @Author: Monei Bakang Mothuti
 * @Time: 2346 hours
 * @Date: 13/02/2025
 */

import lombok.*;

import java.util.Arrays;
@Getter
@Setter

public class SetswanaEntry {
    private String word;
    private String metadata;
    private String[] relatedWords;

    // Default constructor (required for serialization)
    public SetswanaEntry() {
    }

    // Parameterized constructor
    public SetswanaEntry(String word, String metadata, String[] relatedWords) {
        this.word = word;
        this.metadata = metadata;
        this.relatedWords = relatedWords;
    }

    // Getters and Setters
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String[] getRelatedWords() {
        return relatedWords;
    }

    public void setRelatedWords(String[] relatedWords) {
        this.relatedWords = relatedWords;
    }

    // Override toString() for easy debugging
    @Override
    public String toString() {
        return "SetswanaEntry{" +
                "word='" + word + '\'' +
                ", metadata='" + metadata + '\'' +
                ", relatedWords=" + Arrays.toString(relatedWords) +
                '}';
    }
}