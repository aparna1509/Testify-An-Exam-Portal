package com.examportal.util;

import org.springframework.stereotype.Component;
import java.util.*;

/**
 * In-memory leaderboard. Stores one entry per userId (best score wins).
 * Resets on backend restart — persists as long as the process runs.
 */
@Component
public class LeaderboardStore {

    public static class Entry {
        public String userId;
        public String name;
        public int score;
        public int total;
        public String category;
        public java.time.LocalDateTime timestamp;

        public Entry(String userId, String name, int score, int total) {
            this.userId = userId;
            this.name   = name;
            this.score  = score;
            this.total  = total;
            this.category = "All";
            this.timestamp = java.time.LocalDateTime.now();
        }
    }

    private final Map<String, Entry> entries = new LinkedHashMap<>();

    public synchronized void record(String userId, String name, int score, int total) {
        // Keep best score
        Entry existing = entries.get(userId);
        if (existing == null || score > existing.score) {
            entries.put(userId, new Entry(userId, name, score, total));
        }
    }

    public synchronized List<Entry> getLeaderboard() {
        List<Entry> list = new ArrayList<>(entries.values());
        list.sort((a, b) -> {
            int pctA = a.total > 0 ? a.score * 100 / a.total : 0;
            int pctB = b.total > 0 ? b.score * 100 / b.total : 0;
            return Integer.compare(pctB, pctA);
        });
        return list;
    }

    public synchronized List<Entry> getAllEntries() {
        return new ArrayList<>(entries.values());
    }
}
