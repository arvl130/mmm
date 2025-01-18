package com.ageulin.mmm.mappers;

import java.util.ArrayList;
import java.util.List;

public class StringMapper {
    public static List<String> toChunks(String str, int maxChunkSize) {
        var chunks = new ArrayList<String>();
        var currentChunk = new StringBuilder();
        var words = str.split(" ");

        for (String word : words) {
            // Check if adding the next word would exceed the maxChunkSize
            if (currentChunk.length() + word.length() + 1 > maxChunkSize) {
                // If yes, add the current chunk to the list and start a new chunk
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder();
            }
            // Append the current word to the chunk
            if (!currentChunk.isEmpty()) {
                currentChunk.append(" ");
            }
            currentChunk.append(word);
        }

        // Add the last chunk if there's any leftover
        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }
}
