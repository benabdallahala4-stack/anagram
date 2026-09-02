package com.beyonnex.anagram.store;

import com.beyonnex.anagram.domain.AnagramSignature;
import com.beyonnex.anagram.domain.NormalizedText;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Two maps over the same texts: {@code byLetters} dedupes and keeps insertion order,
 * {@code buckets} groups texts by signature so finding anagrams is one hash lookup rather than
 * a scan of everything entered.
 *
 * <p>A read/write lock keeps the two maps consistent with each other. Not needed for a
 * single-user CLI, but a store is the part that ends up shared if this grows.
 */
public final class InMemoryAnagramHistory implements AnagramHistory {

    private final Map<String, NormalizedText> byLetters = new LinkedHashMap<>();
    private final Map<AnagramSignature, List<NormalizedText>> buckets = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void add(NormalizedText text) {
        Objects.requireNonNull(text, "text");
        lock.writeLock().lock();
        try {
            if (byLetters.putIfAbsent(text.letters(), text) == null) {
                buckets.computeIfAbsent(text.signature(), key -> new ArrayList<>()).add(text);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<NormalizedText> findAnagramsOf(NormalizedText query) {
        Objects.requireNonNull(query, "query");
        lock.readLock().lock();
        try {
            List<NormalizedText> family = buckets.get(query.signature());
            if (family == null) {
                return List.of();
            }
            List<NormalizedText> anagrams = new ArrayList<>(family.size());
            for (NormalizedText candidate : family) {
                // Same bucket means same letters; only the query's own word is left out.
                if (!query.isSameWordAs(candidate)) {
                    anagrams.add(candidate);
                }
            }
            return List.copyOf(anagrams);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<NormalizedText> all() {
        lock.readLock().lock();
        try {
            return List.copyOf(byLetters.values());
        } finally {
            lock.readLock().unlock();
        }
    }
}
